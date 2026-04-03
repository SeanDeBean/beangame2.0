package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class Swapper extends BeangameItem implements BGRClickableI {
    
    // Animation colors - light blue to light red
    private static final Color COLOR_START = Color.fromRGB(135, 206, 250); // Light Sky Blue
    private static final Color COLOR_END = Color.fromRGB(255, 160, 160);   // Light Red/Pink
    private static final Color COLOR_CENTER = Color.fromRGB(255, 255, 255); // White center flash

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        handleItemEvent(player, uuid);
        return true;
    }

    private void handleItemEvent(Player player, UUID uuid) {
        World world = player.getWorld();
        Location loc = player.getEyeLocation();
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            RayTraceResult res = performRayTrace(world, loc, player);
            if (res != null && res.getHitEntity() instanceof LivingEntity target) {
                swapLocationsAndEffects(player, target, world, uuid);
            } else {
                // Miss effect - show a short blue trail that fizzles out
                showMissEffect(player);
            }
        });
    }

    private void showMissEffect(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        World world = player.getWorld();
        
        // Short fizzle trail
        for (int i = 0; i < 10; i++) {
            double t = i / 10.0;
            Location particleLoc = eyeLoc.clone().add(direction.clone().multiply(2 + t * 6));
            world.spawnParticle(Particle.DUST, particleLoc, 2, 0.1, 0.1, 0.1, 
                new Particle.DustOptions(COLOR_START, 1.0f - (float)(t * 0.5)));
        }
        
        world.playSound(eyeLoc, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1.5f);
    }

    private RayTraceResult performRayTrace(World world, Location loc, Player player) {
        return world.rayTrace(
                loc,
                loc.getDirection(),
                48D,
                FluidCollisionMode.NEVER,
                true,
                0.25,
                entity -> entity instanceof LivingEntity &&
                        !(entity instanceof Player p && p.getGameMode() == GameMode.SPECTATOR) &&
                        !entity.getUniqueId().equals(player.getUniqueId()));
    }

    private void swapLocationsAndEffects(Player player, LivingEntity target, World world, UUID uuid) {
        Location loc1 = player.getLocation();
        Location loc2 = target.getLocation();
        Location eye1 = player.getEyeLocation();
        Location eye2 = target.getEyeLocation();

        // Enhanced particle trail with color fade
        spawnSwapTrail(eye1, eye2);
        
        // Burst effects at both locations
        spawnSwapBurst(loc1, COLOR_START);
        spawnSwapBurst(loc2, COLOR_END);
        
        // Central flash effect
        Location midPoint = loc1.clone().add(loc2.clone().subtract(loc1).multiply(0.5));
        world.spawnParticle(Particle.FLASH, midPoint.add(0, 1, 0), 1);
        
        // Enhanced sound design
        world.playSound(loc1, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        world.playSound(loc2, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        world.playSound(loc1, Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.5f);
        world.playSound(loc2, Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.5f);
        world.playSound(midPoint, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.8f, 1.0f);

        // Better potion effects - brief resistance + absorption
        target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 0, false, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 15, 4, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 15, 4, false, false, false));

        // Teleport with preserved rotation
        float yaw1 = loc1.getYaw();
        float pitch1 = loc1.getPitch();
        float yaw2 = loc2.getYaw();
        float pitch2 = loc2.getPitch();
        
        Location newLoc1 = loc2.clone();
        newLoc1.setYaw(yaw1);
        newLoc1.setPitch(pitch1);
        
        Location newLoc2 = loc1.clone();
        newLoc2.setYaw(yaw2);
        newLoc2.setPitch(pitch2);
        
        player.teleport(newLoc1);
        target.teleport(newLoc2);
        
        applyCooldown(uuid);
    }

    /**
     * Spawns a beautiful color-fade trail between two points
     */
    private void spawnSwapTrail(Location from, Location to) {
        World world = from.getWorld();
        if (!to.getWorld().equals(world)) return;
        
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();
        
        int particles = (int) (distance * 3); // More particles for smoother trail
        
        for (int i = 0; i <= particles; i++) {
            double t = (double) i / particles;
            Location loc = from.clone().add(direction.clone().multiply(distance * t));
            
            // Blend from light blue to light red
            Color blended = blendColors(COLOR_START, COLOR_END, t);
            
            // Main trail particles
            world.spawnParticle(Particle.DUST, loc.add(0, 0.5, 0), 3, 
                0.15, 0.15, 0.15, new Particle.DustOptions(blended, 1.2f));
            
            // Secondary sparkle trail
            if (i % 2 == 0) {
                world.spawnParticle(Particle.END_ROD, loc, 1, 0.05, 0.05, 0.05, 0);
            }
            
            // Center white highlight
            if (i % 4 == 0) {
                world.spawnParticle(Particle.DUST, loc, 1, 
                    new Particle.DustOptions(COLOR_CENTER, 0.8f));
            }
        }
        
        // Spiral effect around the main trail
        spawnSpiralEffect(from, to, direction, distance);
    }

    /**
     * Adds a spiral particle effect around the swap trail
     */
    private void spawnSpiralEffect(Location from, Location to, Vector direction, double distance) {
        World world = from.getWorld();
        Vector perpendicular = getPerpendicular(direction);
        Vector perpendicular2 = direction.clone().crossProduct(perpendicular).normalize();
        
        int spirals = 2;
        int pointsPerSpiral = 20;
        
        for (int s = 0; s < spirals; s++) {
            double offsetAngle = (2 * Math.PI * s) / spirals;
            
            for (int i = 0; i <= pointsPerSpiral; i++) {
                double t = (double) i / pointsPerSpiral;
                double spiralProgress = t * 4 * Math.PI + offsetAngle; // 2 full rotations
                
                double radius = 0.3 + Math.sin(t * Math.PI) * 0.4; // Varying radius
                
                double x = Math.cos(spiralProgress) * radius;
                double y = Math.sin(spiralProgress) * radius;
                
                Vector spiralOffset = perpendicular.clone().multiply(x)
                    .add(perpendicular2.clone().multiply(y));
                
                Location spiralLoc = from.clone()
                    .add(direction.clone().multiply(distance * t))
                    .add(spiralOffset);
                
                Color spiralColor = blendColors(COLOR_START, COLOR_END, t);
                world.spawnParticle(Particle.DUST, spiralLoc, 1, 
                    new Particle.DustOptions(spiralColor, 0.6f));
            }
        }
    }

    /**
     * Spawns a burst effect at a location
     */
    private void spawnSwapBurst(Location loc, Color primaryColor) {
        World world = loc.getWorld();
        Location center = loc.clone().add(0, 1, 0);
        
        // Expanding ring
        for (int ring = 0; ring < 3; ring++) {
            double radius = 0.5 + ring * 0.4;
            int points = 8 + ring * 4;
            
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                Location ringLoc = center.clone().add(x, ring * 0.3, z);
                world.spawnParticle(Particle.DUST, ringLoc, 1, 
                    new Particle.DustOptions(primaryColor, 1.0f));
            }
        }
        
        // Vertical burst
        world.spawnParticle(Particle.END_ROD, center, 15, 0.3, 0.5, 0.3, 0.1);
        
        // Ground indicator
        world.spawnParticle(Particle.DUST, loc, 8, 0.5, 0.1, 0.5, 
            new Particle.DustOptions(primaryColor, 1.5f));
    }

    private Vector getPerpendicular(Vector v) {
        Vector perp = new Vector(1, 0, 0);
        if (Math.abs(v.getX()) > 0.9) {
            perp = new Vector(0, 1, 0);
        }
        perp = v.clone().crossProduct(perp).normalize();
        if (perp.length() < 0.001) {
            perp = new Vector(0, 0, 1);
            perp = v.clone().crossProduct(perp).normalize();
        }
        return perp;
    }

    private Color blendColors(Color a, Color b, double t) {
        int red = (int) (a.getRed() * (1-t) + b.getRed() * t);
        int green = (int) (a.getGreen() * (1-t) + b.getGreen() * t);
        int blue = (int) (a.getBlue() * (1-t) + b.getBlue() * t);
        return Color.fromRGB(
            Math.min(255, Math.max(0, red)),
            Math.min(255, Math.max(0, green)),
            Math.min(255, Math.max(0, blue))
        );
    }

    @Override
    public long getBaseCooldown() {
        return 30000L;
    }

    @Override
    public String getId() {
        return "swapper";
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
        return "§bSwapper";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to swap locations with",
            "§9the targeted entity up to 48 blocks away.",
            "§9Grants Absorption to the target.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BLAZE_ROD;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
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