package com.beangamecore.items;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;

import java.util.*;

public class PhantomPiercer extends BeangameItem implements BGRClickableI {

    private static final double DASH_DISTANCE = 7.0;
    private static final double HITBOX_RADIUS = 1.0;
    private static final int SLOWNESS_DURATION = 10; // ticks
    private static final double DAMAGE = 5.0;

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        applyCooldown(uuid);
        startDash(player);
        return true;
    }

    private void startDash(Player player) {
        Location startLoc = player.getLocation().add(0, 1, 0).clone();
        Vector direction = startLoc.getDirection().normalize();
        
        // Calculate end location in full 3D space
        Location endLoc = startLoc.clone().add(direction.clone().multiply(DASH_DISTANCE)).subtract(0, 1, 0);
        
        // Ray trace for wall collision
        Location finalLoc = traceToWall(startLoc, endLoc);
        
        // Track entities we've already hit during this dash
        Set<UUID> hitEntities = new HashSet<>();
        Set<LivingEntity> finalTargets = new HashSet<>();
        
        // Collect all entities along the dash path
        double step = 0.5;
        for (double d = 0; d <= startLoc.distance(finalLoc); d += step) {
            Location checkLoc = startLoc.clone().add(direction.clone().multiply(d));
            
            for (Entity entity : player.getWorld().getNearbyEntities(checkLoc, HITBOX_RADIUS, 1.0, HITBOX_RADIUS)) {
                if (!(entity instanceof LivingEntity) || entity.equals(player)) continue;
                if (hitEntities.contains(entity.getUniqueId())) continue;
                
                LivingEntity target = (LivingEntity) entity;
                hitEntities.add(target.getUniqueId());
                finalTargets.add(target);
                
                // Apply slowness 255 for 1 second
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOWNESS_DURATION, 254, false, false, true));
                
                // Apply immobilization for players
                if (target instanceof Player targetPlayer) {
                    Cooldowns.setCooldown("immobilized", targetPlayer.getUniqueId(), SLOWNESS_DURATION * 50);
                }
                
                // Quiet visual feedback
                target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0.05);
            }
        }
        
        // Spawn trail effect from start to end
        spawnDashTrail(startLoc, finalLoc);
        
        // Instant teleport
        player.teleport(finalLoc.setDirection(direction));
        
        // Very quiet sound
        player.getWorld().playSound(finalLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.8f);
        
        // Schedule finish
        new BukkitRunnable() {
            @Override
            public void run() {
                finishDash(player, finalTargets);
            }
        }.runTaskLater(Main.getPlugin(), 2L); // Small delay for visual clarity
    }

    private Location traceToWall(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);
        
        for (double d = 0; d <= distance; d += 0.5) {
            Location checkLoc = start.clone().add(direction.clone().multiply(d));
            
            if (isSolid(checkLoc)) {
                // Return location just before the wall
                return start.clone().add(direction.clone().multiply(Math.max(0, d - 0.5)));
            }
        }
        return end;
    }

    private void spawnDashTrail(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);
        
        Particle.DustOptions darkRedDust = new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.0f);
        
        for (double d = 0; d <= distance; d += 0.3) {
            Location point = start.clone().add(direction.clone().multiply(d));
            start.getWorld().spawnParticle(Particle.DUST, point, 3, 0.1, 0.3, 0.1, 0, darkRedDust);
        }
    }

    private boolean isSolid(Location loc) {
        Block block = loc.getBlock();
        return block.getType().isSolid() && !block.isPassable();
    }

    private void finishDash(Player player, Set<LivingEntity> targets) {
        // Delay for the X animation and damage until slowness ends
        new BukkitRunnable() {
            @Override
            public void run() {
                performXAnimationAndDamage(player, targets);
            }
        }.runTaskLater(Main.getPlugin(), SLOWNESS_DURATION);
    }

    private void performXAnimationAndDamage(Player player, Set<LivingEntity> targets) {

        if(targets.isEmpty()){
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 8*20, 1, false, false, true));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_BITE, 0.15f, 1.2f);
            return;
        }

        // Deep red dust for X animation
        Particle.DustOptions deepRedDust = new Particle.DustOptions(Color.fromRGB(139, 0, 0), 1.5f);

        // X animation over all targets
        for (LivingEntity target : targets) {
            if (target.isDead() || !target.isValid()) continue;
            
            Location loc = target.getLocation().add(0, 1, 0);
            World world = target.getWorld();
            
            // X shape particles using deep red DUST
            double size = 0.8;
            for (double t = -size; t <= size; t += 0.15) {
                // First diagonal of X
                world.spawnParticle(Particle.DUST, loc.clone().add(t, t, 0), 1, 0, 0, 0, 0, deepRedDust);
                world.spawnParticle(Particle.DUST, loc.clone().add(t, -t, 0), 1, 0, 0, 0, 0, deepRedDust);
                // Add some depth
                world.spawnParticle(Particle.DUST, loc.clone().add(0, t, t), 1, 0, 0, 0, 0, deepRedDust);
                world.spawnParticle(Particle.DUST, loc.clone().add(0, -t, t), 1, 0, 0, 0, 0, deepRedDust);
            }
            
            // Deal 5 damage
            target.damage(DAMAGE, player);
            
            // Quiet sound effect
            world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.2f, 0.9f);
        }
        
        // Quiet sound for the player
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_BITE, 0.15f, 0.6f);
    }

    @Override
    public long getBaseCooldown() {
        return 5000; // milliseconds
    }

    @Override
    public String getId() {
        return "phantompiercer";
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
        return "§5Phantom Piercer";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList(
            "§9Right-click to dash forward 7 blocks.",
            "§9Passing through entities slows & damages them.",
            "",
            "§fMovement",
            "§9Castable",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return Arrays.asList(ItemFlag.HIDE_UNBREAKABLE);
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public org.bukkit.Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot() {
        return EquipmentSlotGroup.HAND;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean getRightClickAnimation() {
        return true;
    }

}