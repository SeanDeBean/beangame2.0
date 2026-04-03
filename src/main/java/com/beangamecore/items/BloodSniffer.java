package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGMPTalismanI;

public class BloodSniffer extends BeangameItem implements BGMPTalismanI {

    private static final int TRACKING_RANGE = 24;
    private static final double LOW_HEALTH_THRESHOLD = 0.3;
    private static final double PATH_STEP = 0.4;
    private static final double CURVE_STRENGTH = 0.15; // How much the path curves
    
    // Blood color palette
    private static final Color BLOOD_FRESH = Color.fromRGB(200, 0, 0);
    private static final Color BLOOD_DARK = Color.fromRGB(100, 0, 0);
    private static final Color BLOOD_BLACK = Color.fromRGB(60, 0, 0);
    private static final Color BLOOD_SPLASH = Color.fromRGB(220, 20, 20);

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        int range = TRACKING_RANGE;
        
        for (Player target : player.getWorld().getPlayers()) {
            if (target != player && 
                target.getGameMode() != GameMode.SPECTATOR &&
                player.getLocation().distance(target.getLocation()) <= range &&
                isLowHealth(target)) {
                
                createCurvedBloodTrail(player, target);
                applyPredatoryEffects(player, target);
            }
        }
    }

    private void createCurvedBloodTrail(Player hunter, Player target) {
        List<Location> path = findCurvedPath(hunter, target);
        
        if (path == null || path.size() < 2) {
            // Fallback to simple line if pathfinding fails
            createFallbackTrail(hunter, target);
            return;
        }
        
        renderCurvedBloodTrail(path, hunter, target);
    }

    private List<Location> findCurvedPath(Player hunter, Player target) {
        Location start = hunter.getLocation().add(0, 0.5, 0);
        Location end = target.getLocation().add(0, 0.5, 0);
        
        Vector startForward = hunter.getLocation().getDirection().setY(0).normalize();
        Vector toTarget = end.toVector().subtract(start.toVector()).setY(0).normalize();
        
        double totalDistance = start.distance(end);
        int steps = (int) (totalDistance / PATH_STEP);
        
        // Limit steps to prevent infinite loops
        if (steps > 60) steps = 60;
        
        List<Location> path = new ArrayList<>();
        path.add(start.clone());
        
        Location current = start.clone();
        Vector currentDir = startForward.clone();
        
        for (int i = 0; i < steps; i++) {
            double progress = (double) i / steps;
            
            // Blend between forward direction and target direction based on progress
            // 0.0-0.3: mostly forward, 0.3-1.0: increasingly toward target
            double targetInfluence = Math.max(0, (progress - 0.2) / 0.8);
            
            // Smooth curve using cubic interpolation
            double curveT = smoothStep(targetInfluence);
            
            Vector blendedDir = blendDirections(currentDir, toTarget, curveT);
            blendedDir.normalize();
            
            // Try to move in the desired direction
            Location next = current.clone().add(blendedDir.clone().multiply(PATH_STEP));
            
            // Check if we need to step up or down
            next = adjustForTerrain(current, next, blendedDir);
            
            if (next == null) {
                // Blocked, try to find alternative
                next = findAlternativeStep(current, blendedDir);
                
                if (next == null) {
                    // Truly blocked, end path here
                    break;
                }
            }
            
            // Only add if we actually moved
            if (next.distance(current) > 0.1) {
                path.add(next.clone());
                current = next;
                
                // Update current direction for smoother curves
                currentDir = next.toVector().subtract(path.get(path.size() - 2).toVector()).normalize();
            }
            
            // Check if we reached target
            if (current.distance(end) < 1.5) {
                path.add(end.clone());
                break;
            }
        }
        
        return path.size() > 1 ? path : null;
    }

    private Vector blendDirections(Vector from, Vector to, double t) {
        // Smooth direction blending with curve
        double angle = from.angle(to);
        
        if (angle < 0.01) {
            return to.clone();
        }
        
        // Use slerp-like interpolation for smooth turning
        double sinAngle = Math.sin(angle);
        if (sinAngle < 0.001) {
            return to.clone();
        }
        
        double factorA = Math.sin((1 - t) * angle) / sinAngle;
        double factorB = Math.sin(t * angle) / sinAngle;
        
        Vector result = from.clone().multiply(factorA).add(to.clone().multiply(factorB));
        
        // Add slight curve bias for more organic blood trail
        Vector perpendicular = new Vector(-from.getZ(), 0, from.getX()).normalize();
        double curveBias = Math.sin(t * Math.PI) * CURVE_STRENGTH;
        result.add(perpendicular.multiply(curveBias));
        
        return result.normalize();
    }

    private double smoothStep(double t) {
        // Smooth cubic interpolation: 3t² - 2t³
        return t * t * (3 - 2 * t);
    }

    private Location adjustForTerrain(Location from, Location desired, Vector direction) {
        Location check = desired.clone();
        
        // Check if desired location is walkable
        if (isWalkable(check)) {
            return check;
        }
        
        // Try stepping up (slopes, stairs)
        Location up = check.clone().add(0, 1, 0);
        if (isWalkable(up)) {
            return up;
        }
        
        // Try stepping down (ledges)
        Location down = check.clone().add(0, -1, 0);
        if (isWalkable(down)) {
            return down;
        }
        
        // Try stepping up 2 blocks (jump)
        Location up2 = check.clone().add(0, 2, 0);
        if (isWalkable(up2)) {
            return up2;
        }
        
        return null; // Blocked
    }

    private Location findAlternativeStep(Location current, Vector preferredDir) {
        // Try directions around the preferred direction
        double[] angleOffsets = {Math.PI/4, -Math.PI/4, Math.PI/2, -Math.PI/2, Math.PI*3/4, -Math.PI*3/4};
        
        for (double offset : angleOffsets) {
            Vector altDir = rotateY(preferredDir, offset);
            Location alt = current.clone().add(altDir.multiply(PATH_STEP));
            
            alt = adjustForTerrain(current, alt, altDir);
            if (alt != null) {
                return alt;
            }
        }
        
        return null;
    }

    private Vector rotateY(Vector v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector(
            v.getX() * cos - v.getZ() * sin,
            v.getY(),
            v.getX() * sin + v.getZ() * cos
        );
    }

    private boolean isWalkable(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block ground = loc.clone().add(0, -1, 0).getBlock();
        
        // Must have air at feet and head, solid ground below
        return !feet.getType().isSolid() && 
               !head.getType().isSolid() && 
               ground.getType().isSolid();
    }

    private void renderCurvedBloodTrail(List<Location> path, Player hunter, Player target) {
        World world = hunter.getWorld();
        long time = System.currentTimeMillis();
        double pulse = (time % 800) / 800.0;
        
        for (int i = 0; i < path.size(); i++) {
            double pathProgress = (double) i / path.size();
            Location loc = path.get(i);
            
            // Pulsing effect moves along path
            double pulsePos = (pulse + pathProgress) % 1.0;
            float size = (float) (0.5 + Math.sin(pulsePos * Math.PI) * 0.4);
            
            // Color gradient: dark near hunter, fresh near target
            Color bloodColor = blendColors(BLOOD_DARK, BLOOD_FRESH, pathProgress);
            
            // Main blood droplet
            world.spawnParticle(Particle.DUST, loc, 1, 0.05, 0.05, 0.05, 
                new DustOptions(bloodColor, size));
            
            // Larger droplets at curve points
            if (i > 0 && i < path.size() - 1) {
                Vector prev = path.get(i - 1).toVector();
                Vector curr = loc.toVector();
                Vector next = path.get(i + 1).toVector();
                
                Vector toCurr = curr.clone().subtract(prev).normalize();
                Vector toNext = next.clone().subtract(curr).normalize();
                
                // Sharp turn = more blood splatter
                double turnSharpness = toCurr.distance(toNext);
                if (turnSharpness > 0.3) {
                    world.spawnParticle(Particle.DUST, loc.clone().add(0, 0.2, 0), 3, 0.2, 0.1, 0.2, 
                        new DustOptions(BLOOD_SPLASH, 1.0f));
                }
            }
            
            // Drip effect below path
            if (Math.random() < 0.1 && pathProgress > 0.2) {
                Location drip = loc.clone().add(0, -0.3 - Math.random() * 0.5, 0);
                world.spawnParticle(Particle.DUST, drip, 1, 
                    new DustOptions(BLOOD_BLACK, 0.4f));
            }
        }
        
        // Blood pool at target
        if (Math.random() < 0.3) {
            Location poolLoc = target.getLocation().add(
                (Math.random() - 0.5) * 1.5, 0.1, (Math.random() - 0.5) * 1.5
            );
            world.spawnParticle(Particle.BLOCK, poolLoc, 2, 0.3, 0.1, 0.3, 
                Material.REDSTONE_BLOCK.createBlockData());
        }
    }

    private void createFallbackTrail(Player hunter, Player target) {
        // Direct line with fade when blocked
        Location start = hunter.getLocation().add(0, 0.5, 0);
        Location end = target.getLocation().add(0, 0.5, 0);
        Vector dir = end.toVector().subtract(start.toVector()).normalize();
        
        for (double d = 0; d < start.distance(end) && d < 8; d += 0.5) {
            Location loc = start.clone().add(dir.clone().multiply(d));
            
            if (!isWalkable(loc)) {
                // Splatter at obstruction
                loc.getWorld().spawnParticle(Particle.BLOCK, loc, 5, 0.2, 0.2, 0.2, 
                    Material.REDSTONE_BLOCK.createBlockData());
                break;
            }
            
            loc.getWorld().spawnParticle(Particle.DUST, loc, 1, 
                new DustOptions(BLOOD_DARK, 0.5f));
        }
    }

    private void applyPredatoryEffects(Player hunter, Player target) {
        Vector toTarget = target.getLocation().toVector().subtract(hunter.getLocation().toVector());
        
        if (hunter.isSprinting() && isFacingTarget(hunter, toTarget)) {
            hunter.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 45, 0, false, false, false));
            
            if (toTarget.length() < 10 && System.currentTimeMillis() % 800 < 100) {
                hunter.playSound(hunter.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.3f, 1.2f);
            }
        }
    }

    private boolean isFacingTarget(Player player, Vector toTarget) {
        Vector playerDir = player.getLocation().getDirection();
        return playerDir.normalize().dot(toTarget.normalize()) > 0.6;
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

    private boolean isLowHealth(LivingEntity entity) {
        double currentHealth = entity.getHealth();
        double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
        return (currentHealth / maxHealth) <= LOW_HEALTH_THRESHOLD;
    }

    // Required methods (getId, getName, etc.) remain the same...
    @Override
    public long getBaseCooldown() { return 0; }
    
    @Override
    public String getId() { return "bloodsniffer"; }
    
    @Override
    public boolean isInItemRotation() { return true; }
    
    @Override
    public CraftingRecipe getCraftingRecipe() { return null; }
    
    @Override
    public String getName() { return "§4Blood Sniffer"; }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§3Reveals blood trails to nearby players",
            "§3with low health (30% or less). Grants",
            "§3speed when moving toward wounded targets",
            "§3and creates blood effects on hitting them.",
            "",
            "§3Talisman",
            "§9§obeangame"
        );
    }
    
    @Override
    public Map<String, Integer> getEnchantments() { 
        return Map.of("minecraft:luck_of_the_sea", 1); 
    }
    
    @Override
    public Material getMaterial() { return Material.SNIFFER_EGG; }
    
    @Override
    public int getCustomModelData() { return 0; }
    
    @Override
    public List<ItemFlag> getItemFlags() { 
        return List.of(ItemFlag.HIDE_ENCHANTS); 
    }
    
    @Override
    public ArmorTrim getArmorTrim() { return null; }
    
    @Override
    public Color getColor() { return null; }
    
    @Override
    public int getArmor() { return 0; }
    
    @Override
    public EquipmentSlotGroup getSlot() { return null; }
    
    @Override
    public int getMaxStackSize() { return 1; }
}