package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

public class EarthPiercer extends BeangameItem implements BGLClickableI, BGDDealerHeldI {
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if(!(event.getDamager() instanceof Player)){
            return;
        }
        Player player = (Player) event.getDamager();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }

        if (onCooldown(uuid)){
            return;
        }

        // Get targets before applying cooldown
        List<LivingEntity> targets = getTargetsInCone(player);
        
        // Only apply cooldown if there are targets to hit
        if (targets.isEmpty()) {
            return;
        }
        
        applyCooldown(uuid);

        // Trigger spike attacks on all targets in cone
        targets.forEach(target -> new SpikeEffect(target, player).start());
        
        // Play sound when successfully activating
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
    }

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

        // Get targets before applying cooldown
        List<LivingEntity> targets = getTargetsInCone(player);
        
        // Only apply cooldown if there are targets to hit
        if (targets.isEmpty()) {
            return;
        }
        
        applyCooldown(uuid);

        // Trigger spike attacks on all targets in cone
        targets.forEach(target -> new SpikeEffect(target, player).start());
        
        // Play sound when successfully activating
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.8f);
    }

    private static class SpikeConfig {
        final double maxHeight;
        final float maxSize;
        final double damage;
        final int maxTicks;
        
        SpikeConfig(double maxHeight, float maxSize, double damage, int maxTicks) {
            this.maxHeight = maxHeight;
            this.maxSize = maxSize;
            this.damage = damage;
            this.maxTicks = maxTicks;
        }
    }

    private static class SpikeEffect {
        // Increased max size and duration as requested
        private static final SpikeConfig SPIKE_CONFIG = new SpikeConfig(2.0, 2.0f, 3.0, 16);
        private static final double CONE_RANGE = 6.0;
        private static final double CONE_ANGLE = 60.0; // degrees
        
        private final LivingEntity target;
        private final Player attacker;
        private final World world;
        private final List<ItemDisplay> displays = new ArrayList<>();
        private final List<SpikeData> spikeDataList = new ArrayList<>();
        private int ticks = 0;
        private BukkitRunnable task;
        
        // Individual spike data for different orientations
        private static class SpikeData {
            final Quaternionf rotation;
            final float sizeMultiplier; // Random scaling factor
            final float speedMultiplier; // Slightly different animation speed
            
            SpikeData(Quaternionf rotation, float sizeMultiplier, float speedMultiplier) {
                this.rotation = rotation;
                this.sizeMultiplier = sizeMultiplier;
                this.speedMultiplier = speedMultiplier;
            }
        }

        public SpikeEffect(LivingEntity target, Player attacker) {
            this.target = Objects.requireNonNull(target, "Target cannot be null");
            this.attacker = Objects.requireNonNull(attacker, "Attacker cannot be null");
            this.world = target.getWorld();
        }

        public void start() {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (shouldStop()) {
                        cleanupAndCancel();
                        return;
                    }

                    // Spawn spikes close to the same time with slight offset
                    if (ticks == 0) {
                        spawnSpike(0); // First spike (North-facing)
                    } else if (ticks == 2) { // Small delay for variety
                        spawnSpike(1); // Second spike (East-facing)
                    }

                    updateSpikeAnimation();
                    
                    if (ticks == SPIKE_CONFIG.maxTicks / 2) {
                        dealDamage();
                    }

                    ticks++;
                }
            };
            task.runTaskTimer(Main.getPlugin(), 0L, 1L);
        }

        private boolean shouldStop() {
            return world == null || 
                !target.isValid() || 
                !attacker.isOnline() || 
                ticks >= SPIKE_CONFIG.maxTicks;
        }

        private void cleanupAndCancel() {
            cleanupDisplays();
            task.cancel();
        }

        private void spawnSpike(int spikeIndex) {
            Location targetLoc = target.getLocation();
            Random random = new Random();
            
            // Center the spike on the target (no random offset)
            Location spikeLoc = targetLoc.clone().add(0, 0.1, 0);
            spikeLoc.setYaw(0f);
            spikeLoc.setPitch(0f);
            
            // Create rotation based on spike index
            Quaternionf rotation;
            
            if (spikeIndex == 0) {
                // North-facing spike (flip vertically only)
                rotation = new Quaternionf().rotateX((float) Math.PI);
            } else {
                // East-facing spike (flip vertically + rotate 90 degrees around Y axis)
                rotation = new Quaternionf()
                    .rotateX((float) Math.PI)  // Flip vertically first
                    .rotateY((float) Math.PI / 2); // Then rotate 90 degrees to face east
            }
            
            // Add slight random variation to size and animation speed
            float sizeMultiplier = 0.85f + random.nextFloat() * 0.3f; // 0.85 to 1.15
            float speedMultiplier = 0.9f + random.nextFloat() * 0.2f; // 0.9 to 1.1
            
            // Store the spike data
            SpikeData spikeData = new SpikeData(rotation, sizeMultiplier, speedMultiplier);
            spikeDataList.add(spikeData);
            
            ItemDisplay display = createSpikeDisplay(spikeLoc, rotation);
            displays.add(display);
        }

        private ItemDisplay createSpikeDisplay(Location location, Quaternionf rotation) {
            ItemDisplay display = (ItemDisplay) world.spawnEntity(location, EntityType.ITEM_DISPLAY);
            configureSpikeDisplay(display, rotation);
            return display;
        }

        private void configureSpikeDisplay(ItemDisplay display, Quaternionf rotation) {
            ItemStack spikeItem = new ItemStack(Material.POINTED_DRIPSTONE);
            display.setItemStack(spikeItem);
            
            // Start very small - almost invisible
            display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                rotation,
                new Vector3f(0.01f, 0.01f, 0.01f),
                new Quaternionf()
            ));

            display.setViewRange(32);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setBillboard(Display.Billboard.FIXED);
            display.setInterpolationDuration(1);
            display.setTeleportDuration(1);
        }

        private void updateSpikeAnimation() {
            for (int i = 0; i < displays.size(); i++) {
                ItemDisplay display = displays.get(i);
                if (!display.isValid()) continue;

                // Calculate individual progress for each spike based on when it was spawned
                int spikeStartTick = i * 2; // Spike 0 starts at tick 0, spike 1 at tick 2
                int spikeAge = ticks - spikeStartTick;
                
                // Only animate if the spike has been spawned and is within its animation time
                if (spikeAge >= 0 && spikeAge < SPIKE_CONFIG.maxTicks) {
                    // Apply individual speed multiplier for slight variation
                    SpikeData spikeData = spikeDataList.get(i);
                    float adjustedAge = spikeAge * spikeData.speedMultiplier;
                    float progress = Math.min(adjustedAge / SPIKE_CONFIG.maxTicks, 1.0f);
                    
                    float scale = calculateSpikeScale(progress, spikeData.sizeMultiplier);
                    float height = calculateSpikeHeight(progress, spikeData.sizeMultiplier);
                    
                    updateSpikePosition(display, height);
                    updateSpikeScale(display, i, scale, height);
                }
            }
        }

        private float calculateSpikeScale(float progress, float sizeMultiplier) {
            // Grow to max at halfway point, then shrink
            float baseScale;
            if (progress <= 0.5f) {
                baseScale = (progress * 2.0f) * SPIKE_CONFIG.maxSize;
            } else {
                baseScale = ((1.0f - progress) * 2.0f) * SPIKE_CONFIG.maxSize;
            }
            return baseScale * sizeMultiplier;
        }

        private float calculateSpikeHeight(float progress, float sizeMultiplier) {
            // Same scaling pattern for height
            float baseHeight;
            if (progress <= 0.5f) {
                baseHeight = (float) ((progress * 2.0f) * SPIKE_CONFIG.maxHeight);
            } else {
                baseHeight = (float) (((1.0f - progress) * 2.0f) * SPIKE_CONFIG.maxHeight);
            }
            return baseHeight * sizeMultiplier;
        }

        private void updateSpikePosition(ItemDisplay display, float height) {
            Location targetLoc = target.getLocation();
            
            // Keep spike centered on target, only adjust height
            Location newLoc = targetLoc.clone().add(0, height / 2.0, 0);
            newLoc.setYaw(0f);
            newLoc.setPitch(0f);
            
            display.teleport(newLoc);
        }

        private void updateSpikeScale(ItemDisplay display, int spikeIndex, float scale, float height) {
            float heightScale = Math.max(height, 0.01f); // Minimum scale to prevent invisibility
            
            // Use the stored rotation for this spike
            Quaternionf rotation = spikeDataList.get(spikeIndex).rotation;
            
            display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                rotation,
                new Vector3f(scale, heightScale, scale),
                new Quaternionf()
            ));
        }

        private void dealDamage() {
            if (target.isValid()) {
                target.damage(SPIKE_CONFIG.damage, attacker);
                world.playSound(target.getLocation(), Sound.BLOCK_POINTED_DRIPSTONE_BREAK, 1.0f, 0.8f);
                
                // Spawn some particles for effect
                world.spawnParticle(Particle.BLOCK, target.getLocation().add(0, 0.5, 0), 
                    10, 0.5, 0.5, 0.5, 0.1, Material.POINTED_DRIPSTONE.createBlockData());
            }
        }

        private void cleanupDisplays() {
            displays.stream()
                .filter(ItemDisplay::isValid)
                .forEach(ItemDisplay::remove);
            displays.clear();
        }
    }

    // Fix the cone detection method to work with the EarthPiercer class
    private List<LivingEntity> getTargetsInCone(Player player) {
        Vector playerDirection = player.getLocation().getDirection().normalize();
        Location playerLoc = player.getLocation();
        
        List<LivingEntity> targets = player.getNearbyEntities(SpikeEffect.CONE_RANGE, SpikeEffect.CONE_RANGE, SpikeEffect.CONE_RANGE).stream()
            .filter(entity -> entity instanceof LivingEntity)
            .map(e -> (LivingEntity) e)
            .filter(entity -> !entity.equals(player)) // Exclude the attacking player
            .filter(entity -> {
                if (entity instanceof Player p && p.getGameMode().equals(GameMode.SPECTATOR)) {
                    return false;
                }
                return true;
            })
            .filter(entity -> {
                Vector toEntity = entity.getLocation().toVector().subtract(playerLoc.toVector()).normalize();
                double dotProduct = playerDirection.dot(toEntity);
                // Clamp dot product to avoid NaN from acos
                dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));
                double angle = Math.toDegrees(Math.acos(dotProduct));
                return angle <= SpikeEffect.CONE_ANGLE / 2.0;
            })
            .collect(Collectors.toList());
        
        return targets;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 1200L;
    }

    @Override
    public String getId() {
        return "earthpiercer";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public String getName() {
        return "§7Earth Piercer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cLeft-click or hit enemies to summon",
            "§cspikes on all targets in a 60-degree cone.",
            "§cSpikes grow from the ground, dealing",
            "§cdamage and creating area denial.",
            "§cWorks within 6 blocks range.",
            "",
            "§cOn Hit",
            "§dOn Hit Applier",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 104;
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
