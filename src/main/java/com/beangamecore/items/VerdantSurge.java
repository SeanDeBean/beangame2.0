package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.entity.Bogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class VerdantSurge extends BeangameItem implements BGRClickableI {
    
    private static final long IMMOBILIZE_DURATION = 1400L; // 1.4 seconds
    private static final double ROOT_RADIUS = 1.5;
    private static final int ROOT_TRAVEL_TICKS = 40;

    private static class SpawnConfig {
        final int xOffset;
        final int zOffset;
        final boolean hasSword;
        
        SpawnConfig(int xOffset, int zOffset, boolean hasSword) {
            this.xOffset = xOffset;
            this.zOffset = zOffset;
            this.hasSword = hasSword;
        }
    }

    private void checkForPlayersToImmobilizeEruption(World world, Location center, Player caster) {
        // Eruption has 2.5x larger radius
        double eruptionRadius = ROOT_RADIUS * 2.5; // 3.75 blocks
        
        List<Bogged> spawnedBogged = new ArrayList<>();

        // Define spawn positions and equipment configurations
        SpawnConfig[] configs = {
            new SpawnConfig(0, 0, true),    // Center with sword
            new SpawnConfig(1, 0, false),   // Right without sword
            new SpawnConfig(-1, 0, true),   // Left with sword  
        };

        for (Entity entity : world.getNearbyEntities(center, eruptionRadius, eruptionRadius, eruptionRadius)) {
            if (shouldImmobilize(entity, caster)) {
                Player target = (Player) entity;
                // Eruption immobilization lasts 1.5x longer
                long eruptionDuration = (long) (IMMOBILIZE_DURATION * 1.5); // 2100ms = 2.1 seconds
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0, true, true));
                Cooldowns.setCooldown("immobilized", target.getUniqueId(), eruptionDuration);

                
                
                // Enhanced visual feedback for eruption immobilization
                createEruptionImmobilizationEffect(target);
            }
        }

        for (SpawnConfig config : configs) {
            Location spawnLoc = center.clone().add(config.xOffset, 0, config.zOffset);
            Bogged bogged = spawnConfiguredBogged(world, spawnLoc, caster, config.hasSword);
            spawnedBogged.add(bogged);
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                for (Bogged bogged : spawnedBogged) {
                    // Check if the stray still exists and is valid before trying to kill it
                    if (bogged.isValid() && !bogged.isDead()) {
                        bogged.setHealth(0);
                    }
                }
            }
        }, 320L);

    }

    private Bogged spawnConfiguredBogged(World world, Location location, Player owner, boolean hasSword) {
        Bogged bogged = (Bogged) world.spawnEntity(location, EntityType.BOGGED);
        bogged.setCustomName(owner.getName() + "'s bogged");
        
        EntityEquipment equipment = bogged.getEquipment();
        equipment.setHelmet(new ItemStack(Material.RED_MUSHROOM));
        
        if (hasSword) {
            BeangameItem swordItem = BeangameItemRegistry.getRaw("drunicedge");
            equipment.setItemInMainHand(swordItem.asItem());
        }
        
        bogged.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));
        
        return bogged;
    }
    
    private void createEruptionImmobilizationEffect(Player target) {
        Location loc = target.getLocation();
        World world = target.getWorld();
        
        // Enhanced sound effects
        world.playSound(loc, Sound.BLOCK_ROOTS_PLACE, 1.2f, 0.4f); // Deeper pitch
        world.playSound(loc, Sound.BLOCK_BAMBOO_BREAK, 1.0f, 0.6f); // Additional cracking
        
        // More dramatic particle effect around player
        world.spawnParticle(Particle.BLOCK_CRUMBLE, loc.add(0, 1, 0), 20, 0.8, 0.8, 0.8, 0.2,
            Material.JUNGLE_LOG.createBlockData());
        
        // Additional eruption particles
        world.spawnParticle(Particle.EXPLOSION, loc, 2, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        
        applyCooldown(uuid);
        castRootTangle(player);
        return true;
    }

    private void castRootTangle(Player caster) {
        Location startLocation = caster.getLocation().add(0, 0.5, 0);
        Vector direction = startLocation.getDirection().normalize();
        World world = startLocation.getWorld();
        
        // Play casting sound
        world.playSound(startLocation, Sound.BLOCK_ROOTS_BREAK, 1.0f, 0.8f);
        
        // Root travels forward over time
        for (int i = 0; i <= ROOT_TRAVEL_TICKS; i++) {
            final int tickDelay = i;
            
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                Location currentLoc = startLocation.clone().add(direction.clone().multiply(tickDelay * 0.5));
                
                // Create visual effects
                spawnRootTrailParticles(world, currentLoc);
                spawnTwistingOakLogs(world, currentLoc, tickDelay);
                
                // Check for players to immobilize every 5 ticks
                if (tickDelay % 5 == 0) {
                    checkForPlayersToImmobilize(world, currentLoc, caster);
                }

                if(tickDelay % 10 == 0){
                    world.playSound(startLocation, Sound.BLOCK_ROOTS_BREAK, 0.7f, 0.8f);
                }
                
                // Create eruption at final location
                if (tickDelay == ROOT_TRAVEL_TICKS) {
                    createRootEruption(world, currentLoc, caster);
                }
            }, i);
        }
    }

    private void spawnRootTrailParticles(World world, Location loc) {
        // Main root line particles
        world.spawnParticle(Particle.BLOCK_CRUMBLE, loc, 3, 0.2, 0.1, 0.2, 0.1, 
            Material.ROOTED_DIRT.createBlockData());
        
        // Magical energy sparkles
        world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 2, 0.3, 0.2, 0.3, 0.1);
        
        // Underground root network
        world.spawnParticle(Particle.DUST_COLOR_TRANSITION, loc.clone().add(0, -0.3, 0), 4, 0.4, 0.1, 0.4, 0.1,
            new Particle.DustTransition(Color.fromRGB(101, 67, 33), Color.fromRGB(34, 139, 34), 1.0f));
    }
    
    private void spawnTwistingOakLogs(World world, Location center, int tick) {
        // Create 2-3 twisting oak wood pieces around the root path
        int logCount = 2 + (tick % 2); // Alternates between 2 and 3 logs
        
        for (int i = 0; i < logCount; i++) {
            // Spiral positioning around the center
            double angle = (tick * 8 + i * 120) % 360; // Rotating angle
            double radius = 0.8 + Math.sin(tick * 0.2) * 0.3; // Pulsing radius
            
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians) * radius;
            double z = Math.sin(radians) * radius;
            double y = Math.sin(tick * 0.15 + i) * 0.4; // Bobbing motion
            
            Location logLocation = center.clone().add(x, y, z);
            
            // Create item display for oak wood (not oak log during travel)
            ItemDisplay logDisplay = world.spawn(logLocation, ItemDisplay.class);
            logDisplay.setItemStack(new ItemStack(Material.OAK_WOOD));
            
            // Set transformation for twisting effect
            Transformation transform = logDisplay.getTransformation();
            
            // Rotation around Y axis (twisting)
            float rotationY = (float) Math.toRadians(angle);
            Quaternionf rotation = new Quaternionf().rotationY(rotationY)
                .rotateX((float) Math.toRadians(tick * 2)); // Additional wobble
            
            // Randomized scale for variety - much more variation
            float baseScale = 0.4f + (float) (Math.random() * 0.6); // Random base size 0.4-1.0
            float pulseScale = (float) (Math.sin(tick * 0.1 + i) * 0.2); // Pulsing effect
            float finalScale = baseScale + pulseScale;
            
            // Individual axis scaling for more organic shapes
            float scaleX = finalScale + (float) (Math.random() * 0.3 - 0.15); // ±0.15 variation
            float scaleY = finalScale + (float) (Math.random() * 0.4 - 0.2);  // ±0.2 variation
            float scaleZ = finalScale + (float) (Math.random() * 0.3 - 0.15); // ±0.15 variation
            
            Vector3f scaleVec = new Vector3f(scaleX, scaleY, scaleZ).mul(0.6f);
            
            // Apply transformation
            Transformation newTransform = new Transformation(
                transform.getTranslation(),
                transform.getLeftRotation().mul(rotation),
                scaleVec,
                transform.getRightRotation()
            );
            
            logDisplay.setTransformation(newTransform);
            logDisplay.setInterpolationDuration(5); // Smooth transitions
            logDisplay.setInterpolationDelay(0);
            
            // Remove the display after a short time
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                if (!logDisplay.isDead()) {
                    // Fade out effect
                    logDisplay.setItemStack(null);
                    logDisplay.remove();
                }
            }, 15L + i * 3L); // Staggered removal
        }
    }
    
    private void checkForPlayersToImmobilize(World world, Location center, Player caster) {
        for (Entity entity : world.getNearbyEntities(center, ROOT_RADIUS, ROOT_RADIUS, ROOT_RADIUS)) {
            if (shouldImmobilize(entity, caster)) {
                Player target = (Player) entity;
                Cooldowns.setCooldown("immobilized", target.getUniqueId(), IMMOBILIZE_DURATION);
                
                // Visual feedback for immobilization
                createImmobilizationEffect(target);
            }
        }
    }

    private boolean shouldImmobilize(Entity entity, Player caster) {
        return entity instanceof Player target
                && !target.equals(caster)
                && !target.getGameMode().equals(GameMode.SPECTATOR)
                && !Cooldowns.onCooldown("immobilized", target.getUniqueId());
    }
    
    private void createImmobilizationEffect(Player target) {
        Location loc = target.getLocation();
        World world = target.getWorld();
        
        // Sound effect
        world.playSound(loc, Sound.BLOCK_ROOTS_PLACE, 1.0f, 0.6f);
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0, true, true));
        
        // Particle effect around player
        world.spawnParticle(Particle.BLOCK_CRUMBLE, loc.add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1,
            Material.JUNGLE_LOG.createBlockData());
    }

    private void createRootEruption(World world, Location center, Player caster) {
        // Play eruption sounds
        world.playSound(center, Sound.BLOCK_ROOTS_BREAK, 1.5f, 0.5f);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.8f);
        
        // Create large eruption with oak log displays
        for (int i = 0; i < 30; i++) {
            final int delay = i;
            
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                // Particle effects
                world.spawnParticle(Particle.BLOCK_CRUMBLE, center, 20, 2.0, 1.0, 2.0, 0.3,
                    Material.ROOTED_DIRT.createBlockData());
                
                world.spawnParticle(Particle.EXPLOSION, center, 1, 0.1, 0.1, 0.1, 0.1);
                
                // Large twisting oak logs for eruption
                if (delay % 3 == 0) {
                    spawnEruptionOakLogs(world, center, delay);
                }
            }, i);
        }
        
        // Final immobilization check with increased radius and duration
        checkForPlayersToImmobilizeEruption(world, center, caster);
    }
    
    private void spawnEruptionOakLogs(World world, Location center, int tick) {
        // Create larger, more dramatic oak logs for the eruption with randomized sizes
        for (int i = 0; i < 4; i++) {
            double angle = i * 90 + tick * 5; // Rotating positions
            double radius = 1.5 + Math.random() * 1.5; // Increased spread
            
            double radians = Math.toRadians(angle);
            double x = Math.cos(radians) * radius;
            double z = Math.sin(radians) * radius;
            double y = Math.random() * 2.5; // Higher random height
            
            Location logLocation = center.clone().add(x, y, z);
            
            ItemDisplay logDisplay = world.spawn(logLocation, ItemDisplay.class);
            logDisplay.setItemStack(new ItemStack(Material.OAK_LOG)); // Use OAK_LOG for eruption
            
            // Dramatic rotation and randomized scale
            Transformation transform = logDisplay.getTransformation();
            Quaternionf rotation = new Quaternionf()
                .rotationY((float) Math.toRadians(angle))
                .rotateX((float) Math.toRadians(Math.random() * 360))
                .rotateZ((float) Math.toRadians(Math.random() * 360));
            
            // Much more randomized scaling for eruption logs
            float baseScale = 0.8f + (float) (Math.random() * 1.0); // Base 0.8-1.8
            float scaleX = baseScale + (float) (Math.random() * 0.6 - 0.3); // ±0.3 variation
            float scaleY = baseScale + (float) (Math.random() * 0.8 - 0.4); // ±0.4 variation  
            float scaleZ = baseScale + (float) (Math.random() * 0.6 - 0.3); // ±0.3 variation
            
            // Ensure minimum scale so logs don't become too tiny
            scaleX = Math.max(0.3f, scaleX);
            scaleY = Math.max(0.3f, scaleY);
            scaleZ = Math.max(0.3f, scaleZ);
            
            Vector3f scaleVec = new Vector3f(scaleX, scaleY, scaleZ);
            
            Transformation newTransform = new Transformation(
                transform.getTranslation(),
                rotation,
                scaleVec,
                transform.getRightRotation()
            );
            
            logDisplay.setTransformation(newTransform);
            logDisplay.setInterpolationDuration(10);
            logDisplay.setInterpolationDelay(0);
            
            // Remove after longer duration for eruption effect
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                if (!logDisplay.isDead()) {
                    logDisplay.remove();
                }
            }, 40L + (long) (Math.random() * 20));
        }
    }

    @Override
    public long getBaseCooldown() {
        return 18000L;
    }

    @Override
    public String getId() {
        return "verdantsurge";
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
        return "§2Verdant Surge";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon roots that travel",
            "§9forward, immobilizing enemies along the",
            "§9path. Erupts at the end with increased",
            "§9radius and duration, summoning bogged.",
            "",
            "§9Castable",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 104;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot() {
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
