package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class MasterWusStaff extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        event.setCancelled(true);

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false; // do nothing in spectator mode
        }
        applyCooldown(uuid);
        startSpinjitzu(player);
        return true;
    }

    public void startSpinjitzu(Player player) {
        new SpinjitzuEffect(player).start();
    }

    // Domain-specific types to replace primitives
    private static class OrbitConfig {
        final double yOffset;
        final float baseSize;
        final int direction;
        
        OrbitConfig(double yOffset, float baseSize, int direction) {
            this.yOffset = yOffset;
            this.baseSize = baseSize;
            this.direction = direction;
        }
    }

    private static class DisplayTransformation {
        final Vector3f translation;
        final Quaternionf rotation;
        final Vector3f scale;
        
        DisplayTransformation(Vector3f translation, Quaternionf rotation, Vector3f scale) {
            this.translation = translation;
            this.rotation = rotation;
            this.scale = scale;
        }
    }

    private static class CombatEffect {
        final double range;
        final double force;
        final double damage;
        
        CombatEffect(double range, double force, double damage) {
            this.range = range;
            this.force = force;
            this.damage = damage;
        }
    }

    private static class ParticleEffect {
        final Color color;
        final float size;
        final int count;
        
        ParticleEffect(Color color, float size, int count) {
            this.color = color;
            this.size = size;
            this.count = count;
        }
    }

    private static class SpinjitzuEffect {
        private static final List<OrbitConfig> ORBIT_CONFIGS = List.of(
            new OrbitConfig(-0.5, 0.5f, 1),
            new OrbitConfig(0.0, 0.8f, -1),
            new OrbitConfig(0.5, 1.1f, 1)
        );
        
        private static final CombatEffect PULL_EFFECT = new CombatEffect(3.5, 1.2, 2.0);
        private static final CombatEffect PUSH_EFFECT = new CombatEffect(3.5, 1.6, 0.0);
        private static final ParticleEffect GOLDEN_PARTICLES = 
            new ParticleEffect(Color.fromRGB(255, 215, 0), 1.2f, 5);
        private static final int MAX_TICKS = 100;
        private static final double TARGET_RANGE = 10.0;
        
        private final Player player;
        private final World world;
        private final List<BlockDisplay> displays = new ArrayList<>();
        private int ticks = 0;
        private BukkitRunnable task;

        public SpinjitzuEffect(Player player) {
            this.player = Objects.requireNonNull(player, "Player cannot be null");
            this.world = player.getWorld();
        }

        public void start() {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (shouldStop()) {
                        cleanupAndCancel();
                        return;
                    }

                    if (ticks == 0) {
                        spawnInitialDisplays();
                    }

                    updateDisplayPositions();
                    handlePlayerMovement();
                    handleCombatEffects();
                    spawnParticleEffects();

                    ticks++;
                }
            };
            task.runTaskTimer(Main.getPlugin(), 0L, 1L);
        }

        private boolean shouldStop() {
            return world == null || 
                !Bukkit.getWorlds().contains(player.getWorld()) ||
                !player.isOnline() || 
                ticks > MAX_TICKS || 
                player.getGameMode().equals(GameMode.SPECTATOR);
        }

        private void cleanupAndCancel() {
            cleanupDisplays();
            task.cancel();
        }

        private void spawnInitialDisplays() {
            for (OrbitConfig config : ORBIT_CONFIGS) {
                BlockDisplay display = createDisplay(config);
                displays.add(display);
            }
        }

        private BlockDisplay createDisplay(OrbitConfig config) {
            Location spawnLoc = player.getLocation().add(0, config.yOffset + 0.9, 0);
            spawnLoc.setYaw(0f);
            spawnLoc.setPitch(0f);

            BlockDisplay display = (BlockDisplay) world.spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
            configureDisplay(display, config);
            return display;
        }

        private void configureDisplay(BlockDisplay display, OrbitConfig config) {
            display.setBlock(Bukkit.createBlockData(Material.YELLOW_STAINED_GLASS));
            
            float size = config.baseSize * (float) getPlayerScale();
            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(size, size, size),
                    new Quaternionf()
            ));

            display.setViewRange(64);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setBillboard(Display.Billboard.FIXED);
            display.setInterpolationDuration(1);
            display.setTeleportDuration(1);
        }

        private void updateDisplayPositions() {
            for (int i = 0; i < displays.size(); i++) {
                BlockDisplay display = displays.get(i);
                if (!display.isValid()) continue;

                updateSingleDisplay(display, ORBIT_CONFIGS.get(i));
            }
        }

        private void updateSingleDisplay(BlockDisplay display, OrbitConfig config) {
            float orbitAngleDeg = ticks * 30f * config.direction;
            float orbitAngleRad = (float) Math.toRadians(orbitAngleDeg);

            Location orbitLoc = calculateOrbitPosition(orbitAngleRad, config);
            DisplayTransformation transformation = createDisplayTransformation(orbitAngleRad, config);

            display.setTransformation(new Transformation(
                transformation.translation,
                transformation.rotation,
                transformation.scale,
                new Quaternionf()
            ));
            display.teleport(orbitLoc.add(-0.4, 0, -0.4));
        }

        private Location calculateOrbitPosition(float orbitAngleRad, OrbitConfig config) {
            double newX = player.getLocation().getX() + (config.baseSize * 0.1f) * Math.cos(orbitAngleRad);
            double newZ = player.getLocation().getZ() + (config.baseSize * 0.1f) * Math.sin(orbitAngleRad);
            double newY = player.getLocation().getY() + config.yOffset + 0.4;

            Location orbitLoc = new Location(world, newX, newY, newZ);
            orbitLoc.setYaw(0f);
            orbitLoc.setPitch(0f);
            return orbitLoc;
        }

        private DisplayTransformation createDisplayTransformation(float orbitAngleRad, OrbitConfig config) {
            Quaternionf spinRotation = new Quaternionf().rotateY(orbitAngleRad);
            float scaleValue = config.baseSize * (float) getPlayerScale();
            Vector3f scaleVec = new Vector3f(scaleValue, scaleValue, scaleValue);
            float halfSize = scaleValue / 2.0f;
            Vector3f translation = new Vector3f(halfSize, 0, halfSize);

            return new DisplayTransformation(translation, spinRotation, scaleVec);
        }

        private void handlePlayerMovement() {
            findNearestEntity().ifPresentOrElse(
                this::movePlayerTowardTarget,
                this::spinPlayerInPlace
            );
        }

        private Optional<LivingEntity> findNearestEntity() {
            return player.getNearbyEntities(TARGET_RANGE, TARGET_RANGE, TARGET_RANGE).stream()
                    .filter(this::isValidTarget)
                    .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())))
                    .map(e -> (LivingEntity) e);
        }

        private boolean isValidTarget(Entity entity) {
            if (!(entity instanceof LivingEntity) || entity.equals(player)) {
                return false;
            }
            
            if (entity instanceof Player p && p.getGameMode().equals(GameMode.SPECTATOR)) {
                return false;
            }
            
            return true;
        }

        private void movePlayerTowardTarget(LivingEntity target) {
            Vector direction = target.getLocation().toVector()
                    .subtract(player.getLocation().toVector())
                    .normalize();

            Location current = player.getLocation();
            Location newLoc = current.clone().add(direction.multiply(0.2));
            newLoc.setYaw(current.getYaw() + 15f);
            
            player.teleport(newLoc);
        }

        private void spinPlayerInPlace() {
            Location loc = player.getLocation();
            loc.setYaw(loc.getYaw() + 15f);
            player.teleport(loc);
        }

        private void handleCombatEffects() {
            if (ticks % 10 == 0) {
                applyCombatEffect(PULL_EFFECT);
            }
            
            if (ticks % 10 == 5) {
                applyCombatEffect(PUSH_EFFECT);
            }
        }

        private void applyCombatEffect(CombatEffect effect) {
            player.getNearbyEntities(effect.range, effect.range, effect.range).stream()
                    .filter(this::isValidCombatTarget)
                    .forEach(e -> {
                        Vector direction = effect == PULL_EFFECT ?
                                player.getLocation().toVector().subtract(e.getLocation().toVector()) :
                                e.getLocation().toVector().subtract(player.getLocation().toVector());
                        
                        boolean hasKBResistance = false;
                        if(e instanceof Player){
                            Player pVictim = (Player) e;
                            hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                                    pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                        }
                        if (hasKBResistance) {
                            return;
                        }    
                        e.setVelocity(direction.normalize().multiply(effect.force));
                        if (effect.damage > 0) {
                            ((LivingEntity) e).damage(effect.damage, player);
                        }
                    });
            
            world.playSound(player.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 0.6f, 1.2f);
        }

        private boolean isValidCombatTarget(Entity entity) {
            if (entity.equals(player) || !(entity instanceof LivingEntity)) {
                return false;
            }
            
            if (entity instanceof Player p && p.getGameMode().equals(GameMode.SPECTATOR)) {
                return false;
            }
            
            return true;
        }

        private void spawnParticleEffects() {
            DustOptions dustOptions = new DustOptions(GOLDEN_PARTICLES.color, GOLDEN_PARTICLES.size);

            for (int j = 0; j < GOLDEN_PARTICLES.count; j++) {
                double offsetX = (Math.random() - 0.5) * 1.0;
                double offsetY = Math.random() * 1.5;
                double offsetZ = (Math.random() - 0.5) * 1.0;

                Location particleLoc = player.getLocation().clone().add(offsetX, offsetY, offsetZ);
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dustOptions);
            }
        }

        private void cleanupDisplays() {
            displays.stream()
                    .filter(BlockDisplay::isValid)
                    .forEach(BlockDisplay::remove);
            displays.clear();
        }

        private double getPlayerScale() {
            AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
            return (scaleAttr != null) ? scaleAttr.getValue() : 1.0;
        }
    }
        
    @Override
    public long getBaseCooldown() {
        return 16000L;
    }

    @Override
    public String getId() {
        return "masterwusstaff";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§fMaster Wu's Staff";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to enter Spinjitzu for",
            "§95 seconds. Creates orbiting golden",
            "§9displays and pulls/pushes nearby",
            "§9entities. Automatically moves toward",
            "§9the nearest enemy while spinning.",
            "",
            "§dOn Hit Extender",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

