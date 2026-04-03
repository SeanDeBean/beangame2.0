package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SayCheese extends BeangameItem implements BGRClickableI {
    
    // ============ CONFIGURABLE SETTINGS ============
    private static final int SNAPSHOT_RANGE = 48;           // Blocks
    private static final int SNAPSHOT_DURATION_TICKS = 30;
    private static final double SNAPSHOT_VIEW_ANGLE = Math.toRadians(45); // 45 degrees in radians
    private static final Color BORDER_COLOR = Color.fromRGB(255, 255, 200);
    private static final Color CONE_OUTLINE_COLOR = Color.fromRGB(255, 255, 150);
    
    // Immobilization settings
    private static final int PLAYER_SLOWNESS_LEVEL = 1;     // Slowness II for players (Immobilizer style)
    private static final int MOB_SLOWNESS_LEVEL = 255;      // Complete immobilization for mobs
    private static final int RESISTANCE_LEVEL = 0;          // Resistance I
    private static final String IMMOBILIZED_COOLDOWN_KEY = "immobilized";
    
    private final Map<UUID, SnapshotSession> activeSessions = new ConcurrentHashMap<>();
    
    private static class SnapshotSession {
        final UUID playerUuid;
        final Location center;
        final long endTime;
        final Set<UUID> frozenEntities = ConcurrentHashMap.newKeySet();
        final Map<UUID, Location> originalLocations = new ConcurrentHashMap<>();
        final Map<UUID, Double> entityPlayerDistances = new ConcurrentHashMap<>();
        final Map<UUID, Integer> originalTntFuse = new ConcurrentHashMap<>();
        int taskId = -1;
        int coneTaskId = -1;
        
        SnapshotSession(UUID playerUuid, Location center, long endTime) {
            this.playerUuid = playerUuid;
            this.center = center;
            this.endTime = endTime;
        }
    }
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (activeSessions.containsKey(uuid)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cAlready have an active snapshot!"));
            return false;
        }
        
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        
        applyCooldown(uuid);
        takeSnapshot(player);
        return true;
    }

    /**
     * Check if entity is within player's view cone
     */
    private boolean isInViewCone(SnapshotSession session, Entity entity, double maxAngle) {
        Vector direction = session.center.getDirection().normalize();
        
        Location targetLoc = entity instanceof LivingEntity ? 
            ((LivingEntity) entity).getEyeLocation() : 
            entity.getLocation().add(0, 1, 0);
        
        Vector toEntity = targetLoc.toVector().subtract(session.center.toVector());
        double distance = toEntity.length();
        toEntity.normalize();
        
        double angle = direction.angle(toEntity);
        return angle < maxAngle && distance <= SNAPSHOT_RANGE;
    }

    private boolean isExcludedEntity(Entity entity) {
        return entity instanceof ArmorStand || 
            entity instanceof Item || 
            entity instanceof ItemDisplay || 
            entity instanceof BlockDisplay;
    }
        
    private void takeSnapshot(Player player) {
        UUID uuid = player.getUniqueId();
        Location center = player.getLocation();
        World world = player.getWorld();
        
        long endTime = System.currentTimeMillis() + (SNAPSHOT_DURATION_TICKS * 50L);
        SnapshotSession session = new SnapshotSession(uuid, center, endTime);

        long immobilizeDurationMs = SNAPSHOT_DURATION_TICKS * 50L;
        
        for (Entity entity : world.getNearbyEntities(center, SNAPSHOT_RANGE, SNAPSHOT_RANGE, SNAPSHOT_RANGE)) {
            if (entity instanceof Player p && p.getUniqueId().equals(uuid)) continue;
            if (entity instanceof Player p && p.getGameMode() == GameMode.SPECTATOR) continue;
            if (isExcludedEntity(entity)) continue;
            if (!isInViewCone(session, entity, SNAPSHOT_VIEW_ANGLE)) continue;
            
            double dist = entity.getLocation().distance(center);
            if (dist > SNAPSHOT_RANGE) continue;
            
            session.frozenEntities.add(entity.getUniqueId());
            session.originalLocations.put(entity.getUniqueId(), entity.getLocation().clone());
            session.entityPlayerDistances.put(entity.getUniqueId(), entity.getLocation().distance(center));
            
            immobilizeEntity(entity, immobilizeDurationMs, center);
        }
        
        for (Entity entity : world.getEntitiesByClass(TNTPrimed.class)) {
            if (entity.getLocation().distance(center) <= SNAPSHOT_RANGE && isInViewCone(session, entity, SNAPSHOT_VIEW_ANGLE)) {
                TNTPrimed tnt = (TNTPrimed) entity;
                tnt.setGravity(false);
                session.originalTntFuse.put(entity.getUniqueId(), tnt.getFuseTicks());
                tnt.setFuseTicks(Integer.MAX_VALUE);
                session.frozenEntities.add(entity.getUniqueId());
                session.entityPlayerDistances.put(entity.getUniqueId(), entity.getLocation().distance(center));
            }
        }
        
        activeSessions.put(uuid, session);
        applyCooldown(uuid);
        
        player.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);
        player.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        world.spawnParticle(Particle.FLASH, player.getEyeLocation(), 1);
        
        showBoundingBox(player, center, SNAPSHOT_RANGE);
        startConeDisplay(player, session);
        startSessionTicker(player, session);
    }

    /**
     * Start persistent filled cone display during session
     */
    private void startConeDisplay(Player player, SnapshotSession session) {
        // Show immediately
        showFilledCone(player, session);
        
        // Then repeat every 0.5 seconds
        session.coneTaskId = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!activeSessions.containsKey(session.playerUuid)) {
                Bukkit.getScheduler().cancelTask(session.coneTaskId);
                return;
            }
            showFilledCone(player, session);
        }, 10L, 10L).getTaskId();
    }

    /**
     * Show filled particle cone
     */
    private void showFilledCone(Player player, SnapshotSession session) {
        World world = player.getWorld();
        Location center = session.center;
        Vector direction = session.center.getDirection().normalize();
        
        int layers = 8; // Layers from center to edge (thickness)
        int rings = 10; // Rings along the cone length
        int pointsPerRing = 20;
        
        // Draw filled volume
        for (int r = 1; r <= rings; r++) {
            double distance = (SNAPSHOT_RANGE / (double) rings) * r;
            double maxRadiusAtDistance = distance * Math.tan(SNAPSHOT_VIEW_ANGLE);
            
            // Draw multiple rings at this distance for filled effect
            for (int layer = 0; layer < layers; layer++) {
                double t = layer / (double) layers;
                double radiusAtLayer = maxRadiusAtDistance * t;
                
                for (int i = 0; i < pointsPerRing; i++) {
                    double angle = 2 * Math.PI * i / pointsPerRing;
                    
                    Vector perpendicular = getPerpendicularVector(direction);
                    Vector perpendicular2 = direction.clone().crossProduct(perpendicular).normalize();
                    
                    double x = Math.cos(angle) * radiusAtLayer;
                    double y = Math.sin(angle) * radiusAtLayer;
                    
                    Vector offset = perpendicular.clone().multiply(x).add(perpendicular2.clone().multiply(y));
                    Location particleLoc = center.clone().add(direction.clone().multiply(distance)).add(offset);
                    
                    // Fade color based on distance from center
                    float size = 1.0f - (float)(t * 0.5f);
                    world.spawnParticle(Particle.DUST, particleLoc, 1, 
                        new Particle.DustOptions(CONE_OUTLINE_COLOR, size));
                }
            }
        }
        
        // Draw outer cone surface (brighter)
        for (int r = 1; r <= rings; r++) {
            double distance = (SNAPSHOT_RANGE / (double) rings) * r;
            double radiusAtDistance = distance * Math.tan(SNAPSHOT_VIEW_ANGLE);
            
            for (int i = 0; i < pointsPerRing; i++) {
                double angle = 2 * Math.PI * i / pointsPerRing;
                
                Vector perpendicular = getPerpendicularVector(direction);
                Vector perpendicular2 = direction.clone().crossProduct(perpendicular).normalize();
                
                double x = Math.cos(angle) * radiusAtDistance;
                double y = Math.sin(angle) * radiusAtDistance;
                
                Vector offset = perpendicular.clone().multiply(x).add(perpendicular2.clone().multiply(y));
                Location particleLoc = center.clone().add(direction.clone().multiply(distance)).add(offset);
                
                world.spawnParticle(Particle.DUST, particleLoc, 1, 
                    new Particle.DustOptions(CONE_OUTLINE_COLOR, 1.2f));
            }
        }
        
        // Draw center line
        for (int i = 0; i <= 20; i++) {
            double t = i / 20.0;
            Location lineLoc = center.clone().add(direction.clone().multiply(SNAPSHOT_RANGE * t));
            world.spawnParticle(Particle.DUST, lineLoc, 1, 
                new Particle.DustOptions(Color.WHITE, 1.0f));
        }
    }

    private Vector getPerpendicularVector(Vector v) {
        Vector perpendicular = new Vector(1, 0, 0);
        if (Math.abs(v.getX()) > 0.9) {
            perpendicular = new Vector(0, 1, 0);
        }
        perpendicular = v.clone().crossProduct(perpendicular).normalize();
        if (perpendicular.length() < 0.001) {
            perpendicular = new Vector(0, 0, 1);
            perpendicular = v.clone().crossProduct(perpendicular).normalize();
        }
        return perpendicular;
    }
    
    private void immobilizeEntity(Entity entity, long durationMs, Location playerLoc) {
        if (entity instanceof LivingEntity living) {
            boolean isPlayer = entity instanceof Player;
            int slownessLevel = isPlayer ? PLAYER_SLOWNESS_LEVEL : MOB_SLOWNESS_LEVEL;
            
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SNAPSHOT_DURATION_TICKS, slownessLevel));
            living.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, SNAPSHOT_DURATION_TICKS, RESISTANCE_LEVEL));
            
            if (isPlayer) {
                Cooldowns.setCooldown(IMMOBILIZED_COOLDOWN_KEY, entity.getUniqueId(), durationMs);
            } else {
                living.setGravity(false);
            }
        }
        
        entity.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, entity.getLocation().add(0, 1, 0), 10, 
            Material.WHITE_CONCRETE.createBlockData());
    }
    
    private void showBoundingBox(Player player, Location center, int radius) {
        World world = center.getWorld();
        double minX = center.getX() - radius;
        double maxX = center.getX() + radius;
        double minY = Math.max(center.getWorld().getMinHeight(), center.getY() - radius);
        double maxY = Math.min(center.getWorld().getMaxHeight(), center.getY() + radius);
        double minZ = center.getZ() - radius;
        double maxZ = center.getZ() + radius;
        
        int particlesPerEdge = 20;
        
        for (int i = 0; i <= particlesPerEdge; i++) {
            double t = (double) i / particlesPerEdge;
            double y = minY + (maxY - minY) * t;
            
            world.spawnParticle(Particle.DUST, new Location(world, minX, y, minZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, maxX, y, minZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, minX, y, maxZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, maxX, y, maxZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
        }
        
        for (int i = 0; i <= particlesPerEdge; i++) {
            double t = (double) i / particlesPerEdge;
            
            double xBottom = minX + (maxX - minX) * t;
            double zBottom = minZ + (maxZ - minZ) * t;
            world.spawnParticle(Particle.DUST, new Location(world, xBottom, minY, minZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, xBottom, minY, maxZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, minX, minY, zBottom), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, maxX, minY, zBottom), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            
            double xTop = minX + (maxX - minX) * t;
            double zTop = minZ + (maxZ - minZ) * t;
            world.spawnParticle(Particle.DUST, new Location(world, xTop, maxY, minZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, xTop, maxY, maxZ), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, minX, maxY, zTop), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
            world.spawnParticle(Particle.DUST, new Location(world, maxX, maxY, zTop), 1, new Particle.DustOptions(BORDER_COLOR, 1.0f));
        }
    }
    
    private void startSessionTicker(Player player, SnapshotSession session) {
        session.taskId = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (System.currentTimeMillis() >= session.endTime) {
                endSession(player, session, false);
                return;
            }
        }, 0L, 5L).getTaskId();
    }
    
    private void endSession(Player player, SnapshotSession session, boolean manual) {
        if (session.taskId != -1) {
            Bukkit.getScheduler().cancelTask(session.taskId);
        }
        if (session.coneTaskId != -1) {
            Bukkit.getScheduler().cancelTask(session.coneTaskId);
        }
                
        World world = player.getWorld();
        
        for (UUID entityUuid : session.frozenEntities) {
            Entity entity = Bukkit.getEntity(entityUuid);
            if (entity == null || !entity.isValid()) continue;
            
            
            if (entity instanceof LivingEntity living) {
                living.removePotionEffect(PotionEffectType.SLOWNESS);
                living.removePotionEffect(PotionEffectType.RESISTANCE);
                if(!(living instanceof Player)) living.setGravity(true);
            }
            
            if (entity instanceof TNTPrimed tnt && session.originalTntFuse.containsKey(entityUuid)) {
                tnt.setGravity(true);
                int originalFuse = session.originalTntFuse.get(entityUuid);
                int newFuse = Math.max(0, originalFuse);
                tnt.setFuseTicks(newFuse);
            }
        }
        
        if (!manual) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 0.5f);
            world.spawnParticle(Particle.WHITE_SMOKE, player.getLocation(), 30, 2, 2, 2, 0.1);
        }
        
        activeSessions.remove(player.getUniqueId());
    }
    
    @Override
    public long getBaseCooldown() {
        return 40000L;
    }
    
    @Override
    public String getId() {
        return "saycheese";
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
        return "§fSay Cheese!";
    }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to take a snapshot of the",
            "§9area, freezing all entities in place.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }
    
    @Override
    public Material getMaterial() {
        return Material.CLOCK;
    }
    
    @Override
    public int getCustomModelData() {
        return 0;
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
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
    
    public void clearPlayerData(UUID uuid) {
        SnapshotSession session = activeSessions.get(uuid);
        if (session != null) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                endSession(player, session, true);
            } else {
                activeSessions.remove(uuid);
            }
        }
    }
    
    public void onDisable() {
        for (Map.Entry<UUID, SnapshotSession> entry : activeSessions.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            SnapshotSession session = entry.getValue();
            if (session.coneTaskId != -1) {
                Bukkit.getScheduler().cancelTask(session.coneTaskId);
            }
            if (player != null) {
                endSession(player, session, true);
            }
        }
        activeSessions.clear();
    }
}