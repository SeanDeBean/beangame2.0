package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.Sound;
import org.bukkit.World;
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
import com.beangamecore.items.type.BGLClickableI;

public class TornadoTosser extends BeangameItem implements BGLClickableI {
    
    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }

        if (onCooldown(uuid)){
            return;
        }
        
        applyCooldown(uuid);
        launchTornado(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 1.0f, 0.8f);
    }

    public void launchTornado(Player player) {
        new TornadoEffect(player).start();
    }

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

    private static class TornadoEffect {
        private static final List<OrbitConfig> ORBIT_CONFIGS = List.of(
            new OrbitConfig(-0.5, 0.5f, 1),
            new OrbitConfig(0.0, 0.7f, -1),
            new OrbitConfig(0.5, 0.9f, 1)
        );
        
        private static final ParticleEffect WIND_PARTICLES = 
            new ParticleEffect(Color.fromRGB(200, 200, 255), 1.0f, 8);
        private static final int MAX_TICKS = 60;
        private static final double BASE_SPEED = 0.1;
        private static final double MAX_SPEED = 0.6; 
        private static final float BASE_SIZE = 0.5f;
        private static final float MAX_SIZE = 2.0f;
        private static final double BASE_PULL_RANGE = 1.0;
        private static final double MAX_PULL_RANGE = 3.0;
        
        private final Player launcher;
        private final World world;
        private final Vector direction;
        private final List<BlockDisplay> displays = new ArrayList<>();
        private Location tornadoLocation;
        private int ticks = 0;
        private BukkitRunnable task;

        public TornadoEffect(Player launcher) {
            this.launcher = Objects.requireNonNull(launcher, "Launcher cannot be null");
            this.world = launcher.getWorld();
            this.direction = launcher.getLocation().getDirection().normalize();
            this.tornadoLocation = launcher.getLocation().clone().add(0, 1, 0);
        }

        private final Set<UUID> affectedEntities = new HashSet<>();

        public void start() {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {

                    affectedEntities.clear();

                    if (shouldStop()) {
                        spawnExplosionEffect();
                        cleanupAndCancel();
                        return;
                    }

                    if (ticks == 0) {
                        spawnInitialDisplays();
                    }

                    updateTornadoMovement();
                    updateDisplayPositions();
                    handleEntityPull();
                    spawnParticleEffects();

                    ticks++;
                }
            };
            task.runTaskTimer(Main.getPlugin(), 0L, 1L);
        }

        private boolean shouldStop() {
            return world == null || 
                !launcher.isOnline() || 
                ticks >= MAX_TICKS ||
                tornadoLocation.getY() < 0 || tornadoLocation.getY() > 320 ||
                hasHitSolidBlock(); // Add block collision check
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
            Location spawnLoc = tornadoLocation.clone().add(0, config.yOffset, 0);
            spawnLoc.setYaw(0f);
            spawnLoc.setPitch(0f);

            BlockDisplay display = (BlockDisplay) world.spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
            configureDisplay(display, config);
            return display;
        }

        private void configureDisplay(BlockDisplay display, OrbitConfig config) {
            display.setBlock(Bukkit.createBlockData(Material.WHITE_STAINED_GLASS));
            
            float currentSize = getCurrentSize() * config.baseSize;
            display.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(currentSize, currentSize, currentSize),
                    new Quaternionf()
            ));

            display.setViewRange(64);
            display.setBrightness(new Display.Brightness(10, 10));
            display.setBillboard(Display.Billboard.FIXED);
            display.setInterpolationDuration(1);
            display.setTeleportDuration(1);
        }

        private void updateTornadoMovement() {
            // Calculate current speed (accelerates over time)
            double currentSpeed = getCurrentSpeed();
            
            // Move tornado forward
            Vector movement = direction.clone().multiply(currentSpeed);
            tornadoLocation.add(movement);
        }

        private void updateDisplayPositions() {
            for (int i = 0; i < displays.size(); i++) {
                BlockDisplay display = displays.get(i);
                if (!display.isValid()) continue;

                updateSingleDisplay(display, ORBIT_CONFIGS.get(i));
            }
        }

        private void updateSingleDisplay(BlockDisplay display, OrbitConfig config) {
            float orbitAngleDeg = ticks * 40f * config.direction;
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
            double orbitRadius = getCurrentSize() * config.baseSize * 0.3;
            
            double newX = tornadoLocation.getX() + orbitRadius * Math.cos(orbitAngleRad);
            double newZ = tornadoLocation.getZ() + orbitRadius * Math.sin(orbitAngleRad);
            double newY = tornadoLocation.getY() + config.yOffset;

            Location orbitLoc = new Location(world, newX, newY, newZ);
            orbitLoc.setYaw(0f);
            orbitLoc.setPitch(0f);
            return orbitLoc;
        }

        private DisplayTransformation createDisplayTransformation(float orbitAngleRad, OrbitConfig config) {
            Quaternionf spinRotation = new Quaternionf().rotateY(orbitAngleRad);
            float scaleValue = getCurrentSize() * config.baseSize;
            Vector3f scaleVec = new Vector3f(scaleValue, scaleValue, scaleValue);
            float halfSize = scaleValue / 2.0f;
            Vector3f translation = new Vector3f(halfSize, 0, halfSize);

            return new DisplayTransformation(translation, spinRotation, scaleVec);
        }

        private float getCurrentSize() {
            double progress = (double) ticks / MAX_TICKS;
            return BASE_SIZE + (MAX_SIZE - BASE_SIZE) * (float) progress;
        }

        private double getCurrentSpeed() {
            double progress = (double) ticks / MAX_TICKS;
            return BASE_SPEED + (MAX_SPEED - BASE_SPEED) * progress;
        }

        private double getCurrentPullRange() {
            double progress = (double) ticks / MAX_TICKS;
            return BASE_PULL_RANGE + (MAX_PULL_RANGE - BASE_PULL_RANGE) * progress;
        }

        private boolean hasHitSolidBlock() {
            // Get current tornado dimensions
            float currentSize = getCurrentSize();
            double radius = currentSize * 0.5;
            double height = currentSize * 1.5; // Height of the tornado effect

            if (checkVerticalLevels(radius, height)) {
                return true;
            }

            if (checkAheadForSolidBlock(height)) {
                return true;
            }

            return false;
        }

        private boolean checkVerticalLevels(double radius, double height) {
            // Check multiple vertical levels
            for (double yOffset = 0; yOffset <= height; yOffset += 0.5) {
                Location checkLoc = tornadoLocation.clone().add(0, yOffset, 0);

                // Check center at this height
                if (isSolidBlock(checkLoc)) {
                    return true;
                }

                // Check points around the tornado in a circle at this height
                if (checkPointsAroundTornado(radius, checkLoc)) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkPointsAroundTornado(double radius, Location checkLoc) {
            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);
                double offsetX = Math.cos(radians) * radius;
                double offsetZ = Math.sin(radians) * radius;

                Location edgeLoc = checkLoc.clone().add(offsetX, 0, offsetZ);
                if (isSolidBlock(edgeLoc)) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkAheadForSolidBlock(double height) {
            // Check ahead in the direction of movement
            Vector futureMovement = direction.clone().multiply(getCurrentSpeed() * 2);
            for (double yOffset = 0; yOffset <= height; yOffset += 0.5) {
                Location futureLoc = tornadoLocation.clone()
                        .add(futureMovement)
                        .add(0, yOffset, 0);
                if (isSolidBlock(futureLoc)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSolidBlock(Location loc) {
            Block block = loc.getBlock();
            return !block.isPassable();
        }

        private void handleEntityPull() {
            double pullRange = getCurrentPullRange();
            
            // Find entities within pull range
            tornadoLocation.getWorld().getNearbyEntities(tornadoLocation, pullRange, pullRange, pullRange).stream()
                .filter(this::isValidTarget)
                .filter(this::hasNotBeenAffected)
                .forEach(this::pullAndThrowEntity);
        }

        private boolean hasNotBeenAffected(Entity entity) {
            return !affectedEntities.contains(entity.getUniqueId());
        }

        private boolean isValidTarget(Entity entity) {
            if (!(entity instanceof LivingEntity) || entity.equals(launcher)) {
                return false;
            }
            
            if (entity instanceof Player p && p.getGameMode().equals(GameMode.SPECTATOR)) {
                return false;
            }
            
            return true;
        }

        private void pullAndThrowEntity(Entity entity) {
            // Mark entity as affected
            UUID entityId = entity.getUniqueId();
            
            affectedEntities.add(entityId);
            
            // Schedule removal after 10 ticks
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                affectedEntities.remove(entityId);
            }, 10L);
            
            Location entityLoc = entity.getLocation();
            Location tornadoCenter = tornadoLocation.clone().add(0, getCurrentSize()/2, 0);
            
            createLightningEffect(tornadoCenter, entityLoc);
            
            Vector toTornado = tornadoLocation.toVector().subtract(entityLoc.toVector()).normalize();
            
            double pullStrength = getCurrentSize() * 0.8;
            double upwardStrength = getCurrentSize() * 0.2;
            
            Vector pullVector = toTornado.multiply(pullStrength).add(new Vector(0, upwardStrength, 0));

            if(ticks % 10 == 0){
                world.playSound(entityLoc, Sound.ENTITY_BREEZE_WIND_BURST, 0.8f, 1.5f);
            }

            boolean hasKBResistance = false;
            if(entity instanceof Player){
                Player pVictim = (Player) entity;
                hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
            }
            if (hasKBResistance) {
                return;
            }
            entity.setVelocity(pullVector);
        
        }

        // Dust options for lightning
        private final Particle.DustOptions primaryDust = new Particle.DustOptions(Color.fromRGB(50, 100, 255), 1.5f);
        private final Particle.DustOptions secondaryDust = new Particle.DustOptions(Color.fromRGB(100, 180, 255), 1.2f);
        private final Particle.DustOptions branchPrimaryDust = new Particle.DustOptions(Color.fromRGB(200, 220, 255), 0.8f);
        private final Particle.DustOptions branchSecondaryDust = new Particle.DustOptions(Color.fromRGB(80, 120, 255), 0.5f);

        private void createLightningEffect(Location from, Location to) {
            // Main lightning path
            List<Location> mainPath = generateLightningPath(from, to, 10, 0.5);
            
            // Spawn particles along main path
            for (Location loc : mainPath) {
                world.spawnParticle(Particle.DUST, loc, 1, primaryDust);
                world.spawnParticle(Particle.DUST, loc, 1, secondaryDust);
            }
            
            // Create some random branches
            Random random = new Random();
            int branchCount = 3 + random.nextInt(3);
            for (int i = 0; i < branchCount; i++) {
                // Random point along main path to branch from
                int branchPoint = random.nextInt(mainPath.size() - 2) + 1;
                Location branchStart = mainPath.get(branchPoint);
                
                // Random direction for branch
                Vector branchDir = new Vector(
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1
                ).normalize();
                
                // Random branch length
                double branchLength = 0.5 + random.nextDouble() * 1.5;
                Location branchEnd = branchStart.clone().add(branchDir.multiply(branchLength));
                
                // Generate and draw branch
                List<Location> branchPath = generateLightningPath(branchStart, branchEnd, 5, 0.3);
                for (Location loc : branchPath) {
                    world.spawnParticle(Particle.DUST, loc, 1, branchPrimaryDust);
                    world.spawnParticle(Particle.DUST, loc, 1, branchSecondaryDust);
                }
            }
            
            // Play lightning sound
            world.playSound(from, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 1.8f);
        }

        private List<Location> generateLightningPath(Location from, Location to, int segments, double jaggedness) {
            List<Location> path = new ArrayList<>();
            Random random = new Random();
            
            Vector start = from.toVector();
            Vector end = to.toVector();
            Vector direction = end.clone().subtract(start);
            double length = direction.length();
            direction.normalize();
            
            // Add start point
            path.add(from);
            
            // Generate intermediate points
            for (int i = 1; i < segments; i++) {
                double progress = (double)i / segments;
                Vector basePoint = start.clone().add(direction.clone().multiply(length * progress));
                
                // Add randomness perpendicular to direction
                Vector perpendicular = getPerpendicularVector(direction);
                perpendicular.multiply((random.nextDouble() * 2 - 1) * jaggedness);
                
                Vector jaggedPoint = basePoint.clone().add(perpendicular);
                path.add(new Location(from.getWorld(), jaggedPoint.getX(), jaggedPoint.getY(), jaggedPoint.getZ()));
            }
            
            // Add end point
            path.add(to);
            
            return path;
        }

        private Vector getPerpendicularVector(Vector direction) {
            // Find a vector perpendicular to the direction
            Vector temp = new Vector(1, 0, 0);
            if (Math.abs(direction.dot(temp)) > 0.9) {
                temp = new Vector(0, 1, 0);
            }
            return direction.clone().crossProduct(temp).normalize();
        }

        private void spawnParticleEffects() {
            DustOptions dustOptions = new DustOptions(WIND_PARTICLES.color, WIND_PARTICLES.size);

            // Spawn particles in a spiral around the tornado
            for (int j = 0; j < WIND_PARTICLES.count; j++) {
                double angle = (ticks * 20 + j * 45) % 360;
                double radius = getCurrentSize() * 1.5;
                
                double offsetX = Math.cos(Math.toRadians(angle)) * radius;
                double offsetZ = Math.sin(Math.toRadians(angle)) * radius;
                double offsetY = (Math.random() - 0.5) * getCurrentSize() * 2;

                Location particleLoc = tornadoLocation.clone().add(offsetX, offsetY, offsetZ);
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dustOptions);
            }
        }

        private void cleanupDisplays() {
            displays.stream()
                    .filter(BlockDisplay::isValid)
                    .forEach(BlockDisplay::remove);
            displays.clear();
        }

        private void spawnExplosionEffect() {
            world.spawnParticle(Particle.EXPLOSION, tornadoLocation, 3, 0.5, 0.5, 0.5, 0);
            
            // Add white dust particles spreading outward
            DustOptions whiteDust = new DustOptions(Color.WHITE, 2.0f);
            for (int i = 0; i < 15; i++) {
                double angle = (360.0 / 15) * i;
                double radius = getCurrentSize() * 2.0;
                
                double offsetX = Math.cos(Math.toRadians(angle)) * radius;
                double offsetZ = Math.sin(Math.toRadians(angle)) * radius;
                double offsetY = (Math.random() - 0.5) * 2.0;
                
                Location particleLoc = tornadoLocation.clone().add(offsetX, offsetY, offsetZ);
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, whiteDust);
            }
            
            // Play explosion sound
            world.playSound(tornadoLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 5000L;
    }

    @Override
    public String getId() {
        return "tornadotosser";
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
        return "§7Tornado Tosser";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Left-click to throw a tornado that",
            "§9travels forward, pulling in nearby",
            "§9entities. Explodes upon hitting blocks.",
            "",
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
        return Material.BREEZE_ROD;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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
