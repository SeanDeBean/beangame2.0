package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
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

public class Immobilizer extends BeangameItem implements BGRClickableI{
    
    // Ice theme colors - light cyan to deep ice blue
    private static final Color ICE_LIGHT = Color.fromRGB(200, 240, 255);  // Light cyan
    private static final Color ICE_CORE = Color.fromRGB(135, 206, 250);   // Sky blue
    private static final Color ICE_DEEP = Color.fromRGB(0, 150, 200);     // Deep ice
    private static final Color ICE_WHITE = Color.fromRGB(255, 255, 255);  // Frost white
    private static final Color ICE_CRYSTAL = Color.fromRGB(180, 220, 255); // Crystal blue
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        } 

        World world = player.getWorld();
        Location loc = player.getEyeLocation();
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            performRayTrace(player, world, loc);
        });
        return true;
    }

    private void performRayTrace(Player player, World world, Location loc) {
        RayTraceResult res = world.rayTrace(loc, loc.getDirection(), 24D, FluidCollisionMode.NEVER, true, 0.25,
                entity -> entity instanceof Player &&
                        !entity.getUniqueId().equals(player.getUniqueId()) &&
                        ((Player) entity).getGameMode() != GameMode.SPECTATOR);

        if (res == null || res.getHitEntity() == null) {
            // Miss effect - ice shard that fizzles
            spawnMissEffect(loc);
            return;
        } else if (res.getHitEntity().getType() == EntityType.PLAYER) {
            handlePlayerHit(player, world, loc, (Player) res.getHitEntity());
        }
    }

    private void spawnMissEffect(Location loc) {
        World world = loc.getWorld();
        Vector direction = loc.getDirection().normalize();
        
        // Short ice trail that crystallizes and breaks
        for (int i = 0; i < 8; i++) {
            double t = i / 8.0;
            Location particleLoc = loc.clone().add(direction.clone().multiply(2 + t * 5));
            
            Color fadeColor = blendColors(ICE_LIGHT, ICE_DEEP, t);
            world.spawnParticle(Particle.DUST, particleLoc, 2, 0.08, 0.08, 0.08, 
                new Particle.DustOptions(fadeColor, 0.9f - (float)(t * 0.4)));
        }
        
        // Shatter effect at end
        Location endLoc = loc.clone().add(direction.clone().multiply(7));
        world.spawnParticle(Particle.BLOCK, endLoc, 8, 0.2, 0.2, 0.2, 
            Material.ICE.createBlockData());
        
        world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.8f);
    }

    private void handlePlayerHit(Player player, World world, Location loc, Player target) {
        applyPotionEffects(player, target);
        Location loc2 = target.getEyeLocation();

        createIceConnection(loc, loc2, world, player, target);
        playEnhancedSounds(world, loc, loc2);

        applyCooldown(player.getUniqueId());
        setImmobilizedCooldowns(player.getUniqueId(), target.getUniqueId());
    }

    private void applyPotionEffects(Player player, Player target) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));
    }

    private void createIceConnection(Location from, Location to, World world, Player player, Player target) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();
        
        // Spawn the persistent ice bridge immediately
        spawnPersistentIceBridge(from, to, direction, distance, world);
        
        // Ice crystal formations at both ends
        spawnIceFormation(from, ICE_LIGHT);
        spawnIceFormation(to, ICE_CORE);
        
        // Ambient frost particles around both players for the full duration
        spawnAmbientFrost(player, 100); // 100 ticks = 5 seconds
        spawnAmbientFrost(target, 100);
    }

    private void spawnPersistentIceBridge(Location from, Location to, Vector direction, double distance, World world) {
        int particles = (int) (distance * 3);
        
        // Create the main ice beam that persists for 5 seconds (100 ticks)
        for (int tick = 0; tick < 100; tick += 5) { // Update every 5 ticks for performance
            final int currentTick = tick;
            
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                // Gradually fade the beam over time
                float intensity = 1.0f - (currentTick / 100.0f);
                int particleCount = Math.max(1, (int)(3 * intensity));
                float size = 1.3f * intensity;
                
                for (int i = 0; i <= particles; i++) {
                    double t = (double) i / particles;
                    Location loc = from.clone().add(direction.clone().multiply(distance * t));
                    
                    // Gradient from light cyan to deep ice blue
                    Color beamColor = blendColors(ICE_LIGHT, ICE_DEEP, t);
                    
                    // Core beam
                    world.spawnParticle(Particle.DUST, loc, particleCount, 0.12, 0.12, 0.12, 
                        new Particle.DustOptions(beamColor, size));
                    
                    // Inner white core - fades slower
                    if (i % 2 == 0 && intensity > 0.3f) {
                        world.spawnParticle(Particle.DUST, loc, Math.max(1, particleCount - 1), 0.08, 0.08, 0.08, 
                            new Particle.DustOptions(ICE_WHITE, size * 0.7f));
                    }
                    
                    // Outer crystal shimmer
                    if (i % 3 == 0 && intensity > 0.5f) {
                        spawnCrystalShimmer(loc, direction, t);
                    }
                }
                
                // Traveling pulse effect that runs back and forth during the duration
                double pulseT = (currentTick % 20) / 20.0; // 0 to 1 every second
                Location pulseLoc = from.clone().add(direction.clone().multiply(distance * pulseT));
                
                world.spawnParticle(Particle.DUST, pulseLoc, 5, 0.2, 0.2, 0.2, 
                    new Particle.DustOptions(ICE_WHITE, 1.5f * intensity));
                world.spawnParticle(Particle.DUST, pulseLoc, 3, 0.15, 0.15, 0.15, 
                    new Particle.DustOptions(ICE_CORE, 1.2f * intensity));
                
            }, tick);
        }
        
        // Initial flash
        for (int i = 0; i <= particles; i++) {
            double t = (double) i / particles;
            Location loc = from.clone().add(direction.clone().multiply(distance * t));
            Color beamColor = blendColors(ICE_LIGHT, ICE_DEEP, t);
            
            world.spawnParticle(Particle.DUST, loc, 6, 0.15, 0.15, 0.15, 
                new Particle.DustOptions(beamColor, 1.5f));
            world.spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, 
                new Particle.DustOptions(ICE_WHITE, 1.0f));
        }
    }

    private void spawnCrystalShimmer(Location loc, Vector direction, double t) {
        World world = loc.getWorld();
        Vector perp = getPerpendicular(direction);
        Vector perp2 = direction.clone().crossProduct(perp).normalize();
        
        for (int j = 0; j < 4; j++) {
            double angle = (Math.PI / 2) * j + (t * Math.PI * 2);
            double radius = 0.3 + Math.sin(t * Math.PI * 4) * 0.2;
            
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;
            
            Vector offset = perp.clone().multiply(x).add(perp2.clone().multiply(y));
            Location shimmerLoc = loc.clone().add(offset);
            
            world.spawnParticle(Particle.DUST, shimmerLoc, 1, 
                new Particle.DustOptions(ICE_CRYSTAL, 0.6f));
        }
    }

    private void spawnAmbientFrost(Player player, int durationTicks) {
        World world = player.getWorld();
        
        for (int tick = 0; tick < durationTicks; tick += 10) {
            final int currentTick = tick;
            
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (!player.isOnline()) return;
                
                Location loc = player.getLocation();
                float intensity = 1.0f - (currentTick / (float)durationTicks);
                
                // Frost aura around player
                for (int i = 0; i < 12; i++) {
                    double angle = 2 * Math.PI * i / 12 + (currentTick * 0.1);
                    double radius = 0.8;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location frostLoc = loc.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.DUST, frostLoc, 1, 
                        new Particle.DustOptions(ICE_CRYSTAL, 0.8f * intensity));
                }
                
                // Floating snowflakes above
                world.spawnParticle(Particle.SNOWFLAKE, loc.clone().add(0, 2, 0), 
                    (int)(4 * intensity), 0.4, 0.3, 0.4, 0.01);
                    
                // Ground frost
                if (currentTick % 20 == 0) {
                    world.spawnParticle(Particle.BLOCK, loc, 3, 0.3, 0.1, 0.3, 
                        Material.SNOW.createBlockData());
                }
            }, tick);
        }
    }

    private void spawnIceFormation(Location loc, Color color) {
        World world = loc.getWorld();
        Location center = loc.clone().add(0, 0.5, 0);
        
        // Rising ice crystals
        for (int layer = 0; layer < 5; layer++) {
            double y = layer * 0.4;
            double radius = 0.8 - (layer * 0.15);
            int points = 6 + layer;
            
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points + (layer * 0.5);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                Location crystalLoc = center.clone().add(x, y, z);
                
                Color layerColor = blendColors(color, ICE_WHITE, layer / 5.0);
                world.spawnParticle(Particle.DUST, crystalLoc, 2, 0.05, 0.05, 0.05, 
                    new Particle.DustOptions(layerColor, 1.0f));
            }
        }
        
        // Central ice spike
        for (int i = 0; i < 8; i++) {
            double height = i * 0.3;
            world.spawnParticle(Particle.DUST, center.clone().add(0, height, 0), 3, 
                0.1, 0.1, 0.1, new Particle.DustOptions(ICE_WHITE, 1.2f - (float)(i * 0.1)));
        }
        
        // Snowflake particles
        world.spawnParticle(Particle.SNOWFLAKE, center, 15, 0.5, 0.8, 0.5, 0.02);
    }

    private void playEnhancedSounds(World world, Location loc, Location loc2) {
        // Ice formation sounds
        world.playSound(loc, Sound.BLOCK_GLASS_PLACE, 1.0f, 1.5f);
        world.playSound(loc2, Sound.BLOCK_GLASS_PLACE, 1.0f, 1.5f);
        
        // Freezing crackle
        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.8f);
        world.playSound(loc2, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.8f);
        
        // Deep ice sound
        world.playSound(loc, Sound.BLOCK_STONE_PLACE, 0.6f, 0.8f);
        world.playSound(loc2, Sound.BLOCK_STONE_PLACE, 0.6f, 0.8f);
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

    private void setImmobilizedCooldowns(UUID playerUuid, UUID targetUuid) {
        Cooldowns.setCooldown("immobilized", playerUuid, 5000L);
        Cooldowns.setCooldown("immobilized", targetUuid, 5000L);
    }

    @Override
    public long getBaseCooldown() {
        return 35000L;
    }

    @Override
    public String getId() {
        return "immobilizer";
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
        return "§eImmobilizer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to shoot a ray that",
            "§9immobilizes both you and the target",
            "§9for 5 seconds. Grants resistance to",
            "§9both you and your target.",
            "",
            "§aSupport",
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
        return Material.SLIME_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 105;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}