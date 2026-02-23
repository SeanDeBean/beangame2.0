package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameBow;
import com.beangamecore.util.Cooldowns;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class ApollosBow extends BeangameBow {
    
    public void apollosbowParticles(){
        for(Projectile projectile : getArrows()){
            if(projectile.isOnGround() || projectile.isDead()){
                removeArrow(projectile);
            } else {
                projectile.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, projectile.getLocation(), 1);
            }
        }
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();
        if (!(projectileSource instanceof LivingEntity shooter)) {
            return;
        }
        UUID uuid = shooter.getUniqueId();
        if (onCooldown(uuid)) {
            return;
        }
        applyCooldown(uuid);
        removeArrow(projectile);
        Location loc = projectile.getLocation();
        World world = loc.getWorld();

        createGoldenSummoningCircleEffect(loc, world);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (Entity entity : world.getNearbyEntities(loc, 6, 3, 6)) {
                if (entity instanceof Player player) {
                    // stun players in range when the effect ends
                    Cooldowns.setCooldown("immobilized", player.getUniqueId(), 2500L);
                }
            }
        }, 5 * 20);

    }

    private void createGoldenSummoningCircleEffect(Location loc, World world) {
        int duration = 5; // seconds
        int radius = 6; // This is now the radius of both the circle and star points

        double[] time = {0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            time[0] += 0.1;
            if (time[0] > duration) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            // Draw the outer circle
            for (int i = 0; i < 360; i += 10) {
                double angle = i * Math.PI / 180;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location particleLoc = loc.clone().add(x, 0.1, z);
                world.spawnParticle(Particle.DUST, particleLoc, 1,
                        new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1));
            }
            // Draw the star pattern that touches the circle
            if (time[0] % 0.5 < 0.1) { // Animate the star
                drawTouchingStar(world, loc, radius, Color.fromRGB(255, 215, 0));
            }

            // Affect players in radius
            for (Entity entity : world.getNearbyEntities(loc, radius, 3, radius)) {
                if (entity instanceof Player player) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 4 * 20, 1)); // 4 seconds
                    player.giveExp(2);
                }
            }
        }, 0, 2).getTaskId();
    }

    // Modified star drawing method that touches the outer circle
    private void drawTouchingStar(World world, Location center, double radius, Color color) {
        int points = 5;
        double angleIncrement = 2 * Math.PI / points;
        double innerRadius = radius * 0.382; // Golden ratio for inner points
        
        for (int i = 0; i < points; i++) {
            // Calculate angles for this point
            double outerAngle1 = i * angleIncrement;
            double innerAngle = outerAngle1 + angleIncrement / 2;
            double outerAngle2 = (i + 1) % points * angleIncrement;
            
            // Outer point 1 (on the circle)
            double outerX1 = radius * Math.cos(outerAngle1);
            double outerZ1 = radius * Math.sin(outerAngle1);
            Location outerLoc1 = center.clone().add(outerX1, 0.1, outerZ1);
            
            // Inner point (indented)
            double innerX = innerRadius * Math.cos(innerAngle);
            double innerZ = innerRadius * Math.sin(innerAngle);
            Location innerLoc = center.clone().add(innerX, 0.1, innerZ);
            
            // Outer point 2 (next point on the circle)
            double outerX2 = radius * Math.cos(outerAngle2);
            double outerZ2 = radius * Math.sin(outerAngle2);
            Location outerLoc2 = center.clone().add(outerX2, 0.1, outerZ2);
            
            // Draw lines to create the star
            drawLine(outerLoc1, innerLoc, world, color);
            drawLine(innerLoc, outerLoc2, world, color);
        }
    }

    // Helper method to draw a line between two points
    private void drawLine(Location from, Location to, World world, Color color) {
        Vector direction = to.toVector().subtract(from.toVector());
        double length = direction.length();
        direction.normalize();
        
        for (double d = 0; d < length; d += 0.2) {
            Vector v = direction.clone().multiply(d);
            Location particleLoc = from.clone().add(v);
            world.spawnParticle(Particle.DUST, particleLoc, 1, 
                new Particle.DustOptions(color, 1));
        }
    }

    @Override
    public long getBaseCooldown() {
        return 16500;
    }

    @Override
    public String getId() {
        return "apollosbow";
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
        return "§6Apollo's Bow";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§eCreates a golden zone where arrows land",
            "§ethat applies weakness to enemies and",
            "§egrants experience. Stuns players when",
            "§ethe zone expires.",
            "",
            "§eRanged",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BOW;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
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


