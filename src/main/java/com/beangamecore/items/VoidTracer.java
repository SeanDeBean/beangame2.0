package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.BGRClickableI;

public class VoidTracer extends BeangameItem implements BGLClickableI, BGRClickableI {
    
    // Track lattice states
    private final Map<UUID, Long> latticeEndTime = new ConcurrentHashMap<>();
    private final Map<UUID, List<Location>> latticeNodes = new ConcurrentHashMap<>();
    
    // Teleport cooldown tracking
    private final Map<UUID, Long> teleportCooldowns = new ConcurrentHashMap<>();
    
    // Constants
    private static final int LATTICE_DURATION = 8 * 20; // seconds in ticks
    private static final int LATTICE_NODES = 4;
    private static final double LATTICE_RADIUS = 6.5;
    private static final long TELEPORT_COOLDOWN = 1000; // 1 second in milliseconds
    
    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Check for lattice teleport
        if (isLatticeActive(uuid)) {
            // Check teleport cooldown
            if (isOnTeleportCooldown(uuid)) {
                return;
            }
            
            Location targetNode = getTargetedNode(player, uuid);
            if (targetNode != null) {
                teleportToNode(player, targetNode);
                setTeleportCooldown(uuid);
                return;
            }
        }
    }

    /**
     * Check if player is on teleport cooldown
     */
    private boolean isOnTeleportCooldown(UUID uuid) {
        Long cooldownEnd = teleportCooldowns.get(uuid);
        if (cooldownEnd == null) return false;
        
        if (System.currentTimeMillis() < cooldownEnd) {
            return true;
        } else {
            teleportCooldowns.remove(uuid); // Clean up expired cooldown
            return false;
        }
    }
    
    /**
     * Set teleport cooldown for player
     */
    private void setTeleportCooldown(UUID uuid) {
        teleportCooldowns.put(uuid, System.currentTimeMillis() + TELEPORT_COOLDOWN);
    }
    
    /**
     * Get remaining teleport cooldown in seconds
     */
    private double getRemainingTeleportCooldown(UUID uuid) {
        Long cooldownEnd = teleportCooldowns.get(uuid);
        if (cooldownEnd == null) return 0;
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        if (remaining <= 0) {
            teleportCooldowns.remove(uuid);
            return 0;
        }
        
        return remaining / 1000.0;
    }

    private Location getTargetedNode(Player player, UUID uuid) {
        List<Location> nodes = latticeNodes.get(uuid);
        if (nodes == null || nodes.isEmpty()) return null;
        
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        
        Location closest = null;
        double closestAngle = 0.4; 
        
        for (Location baseNode : nodes) {
            // Check multiple points along the 3-block column
            for (double y = 0; y <= 3; y += 1) {
                Location nodePoint = baseNode.clone().add(0, y, 0);
                Vector toNode = nodePoint.toVector().subtract(eyeLoc.toVector());
                double distance = toNode.length();
                
                if (distance > 24) continue;
                
                toNode.normalize();
                double angle = direction.angle(toNode);
                
                if (angle < closestAngle) {
                    closest = baseNode; // Return the base node, not the specific point
                    closestAngle = angle;
                }
            }
        }
        
        return closest;
    }

    private void teleportToNode(Player player, Location baseNode) {
        Location current = player.getLocation();
        
        // Teleport effects at origin
        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, current, 10, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(current, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.5f);
        
        // Calculate teleport location at mid-height of the column
        Location teleportLoc = baseNode.clone().add(0.5, 1.5, 0.5); // Middle of the 3-block column
        teleportLoc.setYaw(player.getLocation().getYaw());
        teleportLoc.setPitch(player.getLocation().getPitch());
        
        // Find safe ground position
        while (!teleportLoc.getBlock().isPassable() && teleportLoc.getY() > baseNode.getY()) {
            teleportLoc.add(0, -0.5, 0);
        }
        
        // Ensure headroom
        while (!teleportLoc.getBlock().getRelative(0, 1, 0).isPassable() && 
            teleportLoc.getY() < baseNode.getY() + 2.5) {
            teleportLoc.add(0, 0.5, 0);
        }
        
        // Teleport
        player.teleport(teleportLoc);
        
        // Enhanced teleport effects for the whole column
        for (double y = 0; y < 3; y += 0.5) {
            Location effectLoc = baseNode.clone().add(0.5, y, 0.5);
            player.getWorld().spawnParticle(Particle.PORTAL, effectLoc, 5, 0.3, 0.3, 0.3, 0.1);
            player.getWorld().spawnParticle(Particle.SCULK_SOUL, effectLoc, 3, 0.2, 0.2, 0.2, 0);
        }
        
        player.getWorld().playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.2f);
        player.getWorld().playSound(teleportLoc, Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.5f, 2.0f);
        
        // Apply brief invulnerability
        player.setNoDamageTicks(14);
    }
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();

        if(onCooldown(player.getUniqueId())) {
            sendCooldownMessage(player);
            return false;
        }
        
        // Check if lattice is already active
        if (isLatticeActive(player.getUniqueId())) {
            return false;
        }

        applyCooldown(player.getUniqueId());
        
        // Activate the lattice
        activateLatticeOfSilence(player);
        
        // Play activation sound
        player.playSound(player.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.9f, 0.7f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 0.6f, 1.2f);
        
        // Start particle display
        startLatticeParticleDisplay(player);
        return true;
    }
    
    private void activateLatticeOfSilence(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Set lattice end time
        latticeEndTime.put(uuid, System.currentTimeMillis() + (LATTICE_DURATION * 50L));
        
        // Create lattice nodes
        List<Location> nodes = new ArrayList<>();
        Location center = player.getLocation();
        
        for (int i = 0; i < LATTICE_NODES; i++) {
            double angle = 2 * Math.PI * i / LATTICE_NODES;
            double x = Math.cos(angle) * LATTICE_RADIUS;
            double z = Math.sin(angle) * LATTICE_RADIUS;
            
            Location node = center.clone().add(x, 0, z);
            node.setY(center.getY());
            
            // Find safe Y position (not inside blocks)
            while (!isSafeTeleportLocation(node) && node.getY() < center.getY() + 1) {
                node.add(0, 1, 0);
            }
            
            nodes.add(node);
        }
        
        latticeNodes.put(uuid, nodes);
        
        // Initial activation particles
        for (Location node : nodes) {
            player.getWorld().spawnParticle(Particle.PORTAL, node, 20, 0.5, 0.5, 0.5, 0.2);
            player.getWorld().spawnParticle(Particle.SCULK_SOUL, node, 10, 0.3, 0.3, 0.3, 0.1);
        }
    }
    
    private void startLatticeParticleDisplay(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Lambda version
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            // Check if lattice is still active
            if (!isLatticeActive(uuid)) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            
            // Show lattice nodes
            showLatticeNodes(player, uuid);
            
            // Optional: Show teleport cooldown indicator
            if (isOnTeleportCooldown(uuid)) {
                showCooldownIndicator(player, uuid);
            }
        }, 0L, 10L).getTaskId(); // Update every 0.5 seconds
    }
    
    /**
     * Show visual indicator when teleport is on cooldown
     */
    private void showCooldownIndicator(Player player, UUID uuid) {
        List<Location> nodes = latticeNodes.get(uuid);
        if (nodes == null || nodes.isEmpty()) return;
        
        double remaining = getRemainingTeleportCooldown(uuid);
        float cooldownProgress = (float)(remaining / (TELEPORT_COOLDOWN / 1000.0)); // 0 to 1
        
        // Make particles more faded when on cooldown
        for (Location baseNode : nodes) {
            for (double y = 0; y < 3; y += 1) {
                Location particleLoc = baseNode.clone().add(0, y, 0);
                
                // Dimmed particle color during cooldown
                int red = (int)(100 * cooldownProgress);
                int green = (int)(20 * cooldownProgress);
                int blue = (int)(120 * cooldownProgress);
                
                player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1,
                    new Particle.DustOptions(Color.fromRGB(red, green, blue), 1.0f));
            }
        }
    }
    
    private void showLatticeNodes(Player player, UUID uuid) {
        List<Location> nodes = latticeNodes.get(uuid);
        if (nodes == null) return;
        
        // Skip normal particles if on teleport cooldown (they'll show cooldown indicator instead)
        if (isOnTeleportCooldown(uuid)) {
            return;
        }
        
        // Time-based animation for pulsing effect
        long time = System.currentTimeMillis() / 50; // 20Hz animation
        double pulse = Math.sin(time * 0.1) * 0.5 + 0.5; // 0 to 1 pulsation
        
        for (int i = 0; i < nodes.size(); i++) {
            Location baseNode = nodes.get(i);
            
            // Create a 3-block tall column of particles
            for (double y = 0; y < 3; y += 0.5) {
                Location particleLoc = baseNode.clone().add(0, y, 0);
                
                // Base node particles with vertical variation
                float particleSize = (float)(1.5f + pulse * 0.5f);
                
                player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1,
                    new Particle.DustOptions(Color.fromRGB(100, 20, 120), particleSize));
                
                // Pulsing glow effect
                if (pulse > 0.7) {
                    player.getWorld().spawnParticle(Particle.GLOW, particleLoc, 1, 0.15, 0.15, 0.15, 0);
                }
                
                // Connecting lines between vertical points
                if (y > 0) {
                    Location prevLoc = baseNode.clone().add(0, y - 0.5, 0);
                    Main.getPlugin().getParticleManager().particleTrail(prevLoc, particleLoc, 
                        80, 15, 100);
                }
            }
            
            // Connect nodes to each other with faint lines (every other node)
            if (i % 2 == 0 && i + 1 < nodes.size()) {
                Location nextNode = nodes.get(i + 1);
                Main.getPlugin().getParticleManager().particleTrail( 
                    baseNode.clone().add(0, 1.5, 0), 
                    nextNode.clone().add(0, 1.5, 0),
                    60, 10, 80);
            }
        }
        
        // Create a faint dome/network effect above the lattice
        if (nodes.size() >= 3) {
            Location center = calculateLatticeCenter(nodes);
            for (int i = 0; i < 8; i++) {
                double angle = 2 * Math.PI * i / 8 + time * 0.05;
                double radius = LATTICE_RADIUS * 0.8;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                Location domeLoc = center.clone().add(x, 3.5, z);
                player.getWorld().spawnParticle(Particle.DUST, domeLoc, 1,
                    new Particle.DustOptions(Color.fromRGB(120, 30, 150), 0.5f));
            }
        }
    }
    
    private Location calculateLatticeCenter(List<Location> nodes) {
        if (nodes.isEmpty()) return null;
        
        double totalX = 0, totalY = 0, totalZ = 0;
        for (Location node : nodes) {
            totalX += node.getX();
            totalY += node.getY();
            totalZ += node.getZ();
        }
        
        return new Location(nodes.get(0).getWorld(),
            totalX / nodes.size(),
            totalY / nodes.size(),
            totalZ / nodes.size());
    }
    
    private boolean isSafeTeleportLocation(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.getBlock().getRelative(0, 1, 0);
        Block ground = loc.getBlock().getRelative(0, -1, 0);
        
        return feet.isPassable() && head.isPassable() && !ground.isPassable();
    }
    
    private boolean isLatticeActive(UUID uuid) {
        Long endTime = latticeEndTime.get(uuid);
        if (endTime == null) return false;
        
        boolean active = System.currentTimeMillis() < endTime;
        
        // Clean up if expired
        if (!active) {
            latticeEndTime.remove(uuid);
            latticeNodes.remove(uuid);
            teleportCooldowns.remove(uuid); // Also clean up teleport cooldown
        }
        
        return active;
    }
    
    @Override
    public long getBaseCooldown() {
        return 30000; // 30 second cooldown
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
    public String getId() {
        return "voidtracer";
    }
    
    @Override
    public String getName() {
        return "§dVoid Tracer";
    }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§9Manifests a Void Lattice, allowing",
            "§9the user to teleport between nodes",
            "§9on left click.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }
    
    @Override
    public Material getMaterial() {
        return Material.ECHO_SHARD;
    }
    
    @Override
    public int getCustomModelData() {
        return 0; // Custom model for the activator
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
    }
    
    @Override
    public int getMaxStackSize() {
        return 1;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }
    
    // Clean up methods
    public void clearPlayerData(UUID uuid) {
        latticeEndTime.remove(uuid);
        latticeNodes.remove(uuid);
        teleportCooldowns.remove(uuid);
    }
    
    public void onDisable() {
        latticeEndTime.clear();
        latticeNodes.clear();
        teleportCooldowns.clear();
    }
}
