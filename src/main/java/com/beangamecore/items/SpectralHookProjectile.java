package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class SpectralHookProjectile {
    private final Player shooter;
    private ArmorStand projectile;
    private int ticksLived = 0;
    private final List<Location> particleLocations = new ArrayList<>();
    private final Set<UUID> hitEntities = new HashSet<>();

    private static final double GRAVITY = 0.05;
    private static final double HORIZONTAL_VELOCITY = 1.2; 
    private static final int PROJECTILE_LIFETIME = 60; // 3 seconds
    private static final double DAMAGE_BASE = 5;
    private Vector currentVelocity;

    private static final Particle.DustOptions PURPLE_DUST = new Particle.DustOptions(
            Color.fromRGB(0, 0, 205), 1.0f);
    private static final Particle.DustOptions LIGHT_PURPLE_DUST = new Particle.DustOptions(
            Color.fromRGB(0, 191, 255), 0.7f);

    public SpectralHookProjectile(Player shooter) {
        this.shooter = shooter;
    }

    public void launch() {
        // Create invisible armor stand as our projectile
        projectile = shooter.getWorld().spawn(
            shooter.getEyeLocation(),
            ArmorStand.class,
            stand -> {
                stand.setVisible(false);
                stand.setInvulnerable(true);
                stand.setSmall(true);
                stand.setGravity(false);
                stand.setMarker(true);
            }
        );

        currentVelocity = shooter.getLocation().getDirection().normalize();
        currentVelocity.setY(currentVelocity.getY() + 0.3);
        currentVelocity.multiply(HORIZONTAL_VELOCITY);

        // Start tracking (lambda version)
        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            ticksLived++;
                
            // Apply gravity
            currentVelocity.setY(currentVelocity.getY() - GRAVITY);
            
            // Calculate new position
            Location currentLoc = projectile.getLocation();
            Location newLoc = currentLoc.clone().add(currentVelocity);
            
            // Teleport the armor stand
            projectile.teleport(newLoc);

            // Store current location for particle trail
            particleLocations.add(newLoc.clone());
            
            // Limit trail length
            if (particleLocations.size() > 10) {
                particleLocations.remove(0);
            }

            // Draw particles
            drawParticleTrail(newLoc);
            
            // Check for collisions
            checkEntityCollisions(newLoc);
                
            // Remove after lifetime expires
            if (ticksLived >= PROJECTILE_LIFETIME || projectile.isDead()) {
                projectile.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
            }
        }, 0, 1).getTaskId();
    }

    private void drawParticleTrail(Location currentLoc) {
        // Main particle at current position
        currentLoc.getWorld().spawnParticle(
            Particle.DUST, 
            currentLoc, 
            1, // Count
            0, 0, 0, // Offset
            0, // Extra
            PURPLE_DUST,
            true // Force display to distant players
        );

        // Trail particles slightly behind
        if (particleLocations.size() > 1) {
            for (int i = 0; i < particleLocations.size() - 1; i++) {
                Location trailLoc = particleLocations.get(i);
                trailLoc.getWorld().spawnParticle(
                    Particle.DUST,
                    trailLoc,
                    1,
                    0, 0, 0,
                    0,
                    LIGHT_PURPLE_DUST,
                    true
                );
            }
        }

        if (ticksLived % 5 == 0) {
            currentLoc.getWorld().spawnParticle(
                Particle.INSTANT_EFFECT,
                currentLoc,
                1,
                0.1, 0.1, 0.1,
                0.02
            );
        }
    }

    private void checkEntityCollisions(Location currentLoc) {
        // Check entities near current position
        for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 0.8, 0.8, 0.8)) {
            if (isSuitableTarget(entity)) {

                attackEntity((LivingEntity) entity);
                applyPullEffect((LivingEntity) entity);
                hitEntities.add(entity.getUniqueId());
            }
        }
    }

    private boolean isSuitableTarget(Entity entity) {
        return entity instanceof LivingEntity living &&
                entity != shooter &&
                !hitEntities.contains(living.getUniqueId());
    }

    private void attackEntity(LivingEntity entity) {
        // Damage calculation
        entity.damage(DAMAGE_BASE, shooter);

        // Play hit effects
        entity.getWorld().spawnParticle(
            Particle.INSTANT_EFFECT,
            entity.getLocation(),
            10,
            0.3, 0.3, 0.3,
            0.1
        );
        entity.getWorld().playSound(
            entity.getLocation(),
            Sound.ENTITY_PHANTOM_HURT,
            0.5f,
            1.2f
        );
    }

    private void applyPullEffect(LivingEntity entity) {
        boolean hasKBResistance = false;
        if(entity instanceof Player){
            Player pVictim = (Player) entity;
            hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
        }
        if (hasKBResistance) {
            return;
        }
        Vector direction = shooter.getLocation().toVector()
            .subtract(entity.getLocation().toVector())
            .normalize();
        entity.setVelocity(entity.getVelocity().add(direction));
    }
}

