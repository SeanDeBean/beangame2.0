package com.beangamecore.entities.tentacles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.entities.renderers.BlockDisplayRenderer;

public class WaterPool {
    private Location center;
    private LivingEntity owner;
    private org.bukkit.plugin.Plugin plugin;
    private int ticksAlive = 0;
    private List<Tentacle> tentacles = new ArrayList<>();
    private List<LivingEntity> affectedEntities = new ArrayList<>();
    private BlockDisplayRenderer renderer = new BlockDisplayRenderer();
    
    public WaterPool(Location center, LivingEntity owner, org.bukkit.plugin.Plugin plugin) {
        this.center = center;
        this.owner = owner;
        this.plugin = plugin;
        startPool();
    }
    
    private void startPool() {

        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ticksAlive++;
                
            // Spawn pool particles (spirals)
            spawnPoolParticles();
                
            // After 1 second (20 ticks), start spawning tentacles
            if (ticksAlive >= 20 && ticksAlive < 220) { // 1s delay, 10s duration
                if (ticksAlive % 40 == 0 && tentacles.size() < 5) { // Every 2 seconds
                    spawnTentacle();
                }
            }
                
            // Update tentacles
            updateTentacles();
                
            // Apply effects to entities in radius
            applyEffectsToEntities();
                
            // End after 11 seconds (220 ticks)
            if (ticksAlive >= 220) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                cleanup();
            }
        }, 0, 1).getTaskId();
    }
    
    private void spawnPoolParticles() {
        double radius = 6.0;
        int points = 50;
        double spiralFactor = 0.1;
        
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            // Create spiral instead of rings - angle increases with time
            double spiralAngle = angle + (ticksAlive * spiralFactor);
            double spiralRadius = radius * (0.2 + 0.8 * ((spiralAngle % (2 * Math.PI)) / (2 * Math.PI)));
            
            double x = spiralRadius * Math.cos(angle);
            double z = spiralRadius * Math.sin(angle);
            
            Location particleLoc = center.clone().add(x, 0.1, z);
            
            // Multiple blue colors - lighter shades
            org.bukkit.Color particleColor;
            int colorChoice = (i + ticksAlive) % 4;
            switch (colorChoice) {
                case 0: particleColor = org.bukkit.Color.fromRGB(100, 150, 255); break; // Light blue
                case 1: particleColor = org.bukkit.Color.fromRGB(150, 200, 255); break; // Very light blue
                case 2: particleColor = org.bukkit.Color.fromRGB(80, 180, 255); break; // Sky blue
                case 3: particleColor = org.bukkit.Color.fromRGB(120, 220, 255); break; // Pale blue
                default: particleColor = org.bukkit.Color.fromRGB(100, 150, 255);
            }
            
            center.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 
                new Particle.DustOptions(particleColor, 1.0f));
            
            // Speckle in some green algae particles (10% chance)
            if (Math.random() < 0.1) {
                Location algaeLoc = center.clone().add(
                    (Math.random() - 0.5) * radius * 2,
                    0.1,
                    (Math.random() - 0.5) * radius * 2
                );
                center.getWorld().spawnParticle(Particle.DUST, algaeLoc, 1,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(50, 150, 50), 0.8f));
            }
        }
        
        // Add cloud particles around tentacle bases
        spawnTentacleCloudParticles();
    }

    private void spawnTentacleCloudParticles() {
        for (Tentacle tentacle : tentacles) {
            Location tentacleBase = tentacle.getBaseLocation();
            
            // Spawn cloud particles around the tentacle base
            for (int i = 0; i < 3; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double distance = Math.random() * 1.5;
                double x = Math.cos(angle) * distance;
                double z = Math.sin(angle) * distance;
                double y = 0.1 + Math.random() * 1;
                
                Location cloudLoc = tentacleBase.clone().add(x, y, z);
                
                // Light blue/white cloud particles
                center.getWorld().spawnParticle(Particle.CLOUD, cloudLoc, 1, 0, 0, 0, 0);
            }
            
            // Dense particles at the very bottom of the tentacle
            for (int i = 0; i < 5; i++) {
                Location bottomLoc = tentacleBase.clone().add(
                    (Math.random() - 0.5) * 0.3,
                    0.1,
                    (Math.random() - 0.5) * 0.3
                );
                center.getWorld().spawnParticle(Particle.DUST, bottomLoc, 1,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(150, 200, 255), 1.2f));
            }
        }
    }
    
    private void spawnTentacle() {
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * 6.0;
        
        Location baseLocation = center.clone().add(
            Math.cos(angle) * distance,
            0,
            Math.sin(angle) * distance
        );
        
        Tentacle tentacle = new Tentacle(baseLocation, center, owner, plugin, renderer);
        tentacles.add(tentacle);
    }
    
    private void updateTentacles() {
        Iterator<Tentacle> iterator = tentacles.iterator();
        while (iterator.hasNext()) {
            Tentacle tentacle = iterator.next();
            tentacle.update();
            
            // Remove dead tentacles
            if (tentacle.shouldRemove()) {
                tentacle.remove();
                iterator.remove();
            }
        }
    }
    
    private void applyEffectsToEntities() {
        double radius = 6.0;
        List<LivingEntity> nearbyEntities = center.getWorld().getLivingEntities().stream()
            .filter(entity -> entity.getLocation().distance(center) <= radius)
            .filter(entity -> entity != owner)
            .toList();
        
        // Update affected entities
        affectedEntities.clear();
        affectedEntities.addAll(nearbyEntities);
        
        // Apply effects every 10 ticks (0.5 seconds)
        if (ticksAlive >= 20 && ticksAlive % 10 == 0) { // Start after 1 second delay
            for (LivingEntity entity : affectedEntities) {
                
                // Apply slowness
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0)); // 2 seconds
                
                // Apply pull effect (gradual with bursts)
                if (ticksAlive % 20 == 0) { // Burst every second
                    pullEntityTowardCenter(entity, 0.8);
                } else {
                    pullEntityTowardCenter(entity, 0.2);
                }
            }
        }

        if(ticksAlive % 35 == 0){
            for (LivingEntity entity : affectedEntities) {
                entity.damage(1.0, owner);
            }
        }
    }
    
    private void pullEntityTowardCenter(LivingEntity entity, double strength) {
        Vector toCenter = center.toVector().subtract(entity.getLocation().toVector());
        toCenter.setY(0); // Only horizontal pull
        
        // Check if vector is valid before normalizing
        if (toCenter.lengthSquared() < 0.0001) {
            return; // Entity is at center, no pull needed
        }
        
        toCenter.normalize().multiply(strength);
        
        // Check if the resulting vector is finite before applying
            if (isFiniteVector(toCenter)) {
                boolean hasKBResistance = false;
                if(entity instanceof Player){
                    Player pVictim = (Player) entity;
                    if(pVictim.getGameMode() == GameMode.SPECTATOR) return;
                    hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                            pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                }
                if(!hasKBResistance){
                    entity.setVelocity(entity.getVelocity().add(toCenter));
                }
        }
    }

    private boolean isFiniteVector(Vector vector) {
        return Double.isFinite(vector.getX()) && 
            Double.isFinite(vector.getY()) && 
            Double.isFinite(vector.getZ());
    }
        
    private void cleanup() {
        for (Tentacle tentacle : tentacles) {
            tentacle.remove();
        }
        tentacles.clear();
        affectedEntities.clear();
    }
}
