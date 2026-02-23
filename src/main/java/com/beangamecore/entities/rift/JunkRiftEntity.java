package com.beangamecore.entities.rift;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class JunkRiftEntity {
    private final Plugin plugin;
    private final Player owner;
    private final World world;
    private final Location startPoint;
    private final Location endPoint;
    private final int duration;
    private final double junkDamage;
    
    private int ticks = 0;
    private boolean isOpen = false;
    private final Random random = new Random();
    private final Vector riftDirection;
    private final double riftLength;

    public JunkRiftEntity(Plugin plugin, Player owner, Location center, double baseLength, int duration, double junkDamage, Vector direction) {
        this.plugin = plugin;
        this.owner = owner;
        this.world = center.getWorld();
        this.duration = duration;
        this.junkDamage = junkDamage;

        // Create a point directly above center with slight random offset (±1 block)
        Location centerAbove = center.clone().add(
            (random.nextDouble() - 0.5) * 2.0,  // ±1 block horizontal offset
            15 + random.nextDouble() * 3,        // 15-18 blocks above
            (random.nextDouble() - 0.5) * 2.0   // ±1 block horizontal offset
        );
        
        // Generate two points that form a line passing through centerAbove
        Vector lineDirection = new Vector(direction.clone().setY(0).normalize().getZ(), 0, -direction.clone().setY(0).normalize().getX());
        double halfLength = Math.min(5.0, random.nextDouble() * 14);
        
        this.startPoint = centerAbove.clone().add(lineDirection.clone().multiply(-halfLength))
            .add(0, (random.nextDouble() - 0.5) * 5.0, 0);
    
        this.endPoint = centerAbove.clone().add(lineDirection.clone().multiply(halfLength))
            .add(0, (random.nextDouble() - 0.5) * 5.0, 0); 
        
        // Calculate rift direction and length
        this.riftDirection = endPoint.clone().subtract(startPoint).toVector();
        this.riftLength = riftDirection.length();
        this.riftDirection.normalize();
    }

    public void start() {
        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (shouldCloseRift()) {
                // Closing effects along the entire rift
                for (double t = 0; t <= 1; t += 0.1) {
                    Location point = getRiftPoint(t);
                    world.spawnParticle(Particle.SMOKE, point, 3, 0.2, 0.2, 0.2, 0.05);
                }
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
                
            // Opening animation (first 20 ticks = 1 second)
            if (ticks < 20) {
                animateOpening();
            } else {
                if (!isOpen) {
                    isOpen = true;
                    world.playSound(getRiftCenter(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                }
                junkStorm();
            }
                
            // Maintain rift visuals
            maintainRiftVisuals();
            ticks++;

        }, 0, 1).getTaskId();
    }
    
    private Location getRiftPoint(double progress) {
        return startPoint.clone().add(riftDirection.clone().multiply(progress * riftLength));
    }
    
    private Location getRiftCenter() {
        return getRiftPoint(0.5);
    }
    
    private void animateOpening() {
        double progress = (double) ticks / 20;
        
        // Animate from start to end point
        for (double t = 0; t <= progress; t += 0.05) {
            Location point = getRiftPoint(t);
            
            // Main rift line particles
            world.spawnParticle(Particle.REVERSE_PORTAL, point, 2, 0.1, 0.1, 0.1, 0.03);
            
            // Growing fractal branches during opening
            if (random.nextDouble() < 0.4) {
                spawnFractalBranch(point, progress);
            }
        }
    }
    
    private void maintainRiftVisuals() {
        // Main rift line - continuous particles along the entire length
        for (double t = 0; t <= 1; t += 0.08) {
            Location point = getRiftPoint(t);
            
            // Primary rift particles
            world.spawnParticle(Particle.PORTAL, point, 1, 0.05, 0.05, 0.05, 0.02);
            
            // Pulsing core effect
            if (ticks % 15 == 0) {
                world.spawnParticle(Particle.END_ROD, point, 1, 0.1, 0.1, 0.1, 0.01);
            }
        }
        
        // Fractal branches spawning randomly along the rift
        if (ticks % 6 == 0) {
            spawnRandomBranches();
        }
        
        // Occasional rift surge
        if (ticks % 30 == 0) {
            createRiftSurge();
        }
    }
    
    private void spawnRandomBranches() {
        int numBranches = 2 + random.nextInt(3);
        
        for (int i = 0; i < numBranches; i++) {
            double branchPos = random.nextDouble();
            Location branchStart = getRiftPoint(branchPos);
            spawnFractalBranch(branchStart, 1.0);
        }
    }
    
    private void spawnFractalBranch(Location startPoint, double intensity) {
        double branchLength = 1.5 + random.nextDouble() * 1.5;
        Vector branchDir = getRandomPerpendicularVector(riftDirection).multiply(intensity);
        
        for (double dist = 0; dist < branchLength; dist += 0.15) {
            Location branchPoint = startPoint.clone().add(branchDir.clone().multiply(dist));
            
            // Branch particles
            world.spawnParticle(Particle.ELECTRIC_SPARK, branchPoint, 1, 0.02, 0.02, 0.02, 0.005);
            
            // Secondary branching (fractal recursion)
            if (random.nextDouble() < 0.3 * intensity) {
                Vector subBranchDir = getRandomPerpendicularVector(branchDir).multiply(intensity * 0.7);
                for (double subDist = 0; subDist < branchLength * 0.6; subDist += 0.1) {
                    Location subPoint = branchPoint.clone().add(subBranchDir.clone().multiply(subDist));
                    world.spawnParticle(Particle.REVERSE_PORTAL, subPoint, 1, 0.01, 0.01, 0.01, 0.002);
                }
            }
        }
    }
    
    private Vector getRandomPerpendicularVector(Vector base) {
        // Generate a random vector perpendicular to the base vector
        Vector randomVec = new Vector(
            random.nextDouble() - 0.5,
            random.nextDouble() - 0.5,
            random.nextDouble() - 0.5
        );
        
        // Make it perpendicular to base
        Vector perpendicular = randomVec.crossProduct(base).normalize();
        
        // Randomize length slightly
        return perpendicular.multiply(0.3 + random.nextDouble() * 0.4);
    }
    
    private void createRiftSurge() {
        // Intensified particle effect along the entire rift
        for (double t = 0; t <= 1; t += 0.05) {
            Location point = getRiftPoint(t);
            world.spawnParticle(Particle.END_ROD, point, 2, 0.15, 0.15, 0.15, 0.03);
            world.spawnParticle(Particle.ELECTRIC_SPARK, point, 3, 0.1, 0.1, 0.1, 0.02);
        }
        world.playSound(getRiftCenter(), Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.5f);
    }
    
    private void junkStorm() {
        if (ticks % 3 == 0 || ticks % 5 == 1) {
            int bladeCount = calculateBladeCount();
            
            for (int i = 0; i < bladeCount; i++) {
                if(Math.random() < 0.5){
                    dropItem();
                }
            }
        }
    }
    
    private int calculateBladeCount() {
        double distance = owner.getLocation().distance(getRiftCenter());
        double logDistance = Math.log(Math.max(1, distance));
        int blades = (int) (logDistance * 10);
        return Math.max(2, Math.min(6, blades));
    }
    
    private boolean shouldCloseRift() {
        if (ticks >= duration) return true;
        if (!owner.isOnline()) return true;
        if (owner.isDead()) return true;
        if (world == null) return true;
        return !Bukkit.getWorlds().contains(world);
    }

    private void dropItem() {
        // Random position along the rift line
        double riftPos = random.nextDouble();
        Location bladeSpawn = getRiftPoint(riftPos);
        
        // Add slight random offset from the rift line
        Vector offset = getRandomPerpendicularVector(riftDirection).multiply(random.nextDouble() * 0.8);
        bladeSpawn.add(offset);
        
        // Falling direction (slightly randomized downward)
        Vector bladeDirection = new Vector(
            (random.nextDouble() - 0.5) * 0.1,
            -1.0,
            (random.nextDouble() - 0.5) * 0.1
        ).normalize();
        
        new JunkProjectile(plugin, owner, bladeSpawn, bladeDirection, junkDamage);
    }
}
