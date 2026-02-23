package com.beangamecore.entities.tentacles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.beangamecore.entities.renderers.BlockDisplayRenderer;
import com.beangamecore.entities.renderers.ChainSegment;
import com.beangamecore.entities.renderers.KinematicChain;
import com.beangamecore.entities.renderers.Utils;

class Tentacle {
    private Location baseLocation;
    private LivingEntity owner;
    private BlockDisplayRenderer renderer;
    private KinematicChain chain;
    private int ticksAlive = 0;
    private boolean shouldRemove = false;
    private LivingEntity targetEntity = null;
    private Random random = new Random();
    private double swayTime = random.nextDouble() * 100;
    
    public Tentacle(Location baseLocation, Location center, LivingEntity owner, 
                   org.bukkit.plugin.Plugin plugin, BlockDisplayRenderer renderer) {
        this.baseLocation = baseLocation;
        this.owner = owner;
        this.renderer = renderer;
        this.chain = createChain();
        findTarget();
    }

    public Location getBaseLocation(){
        return baseLocation;
    }
    
    private KinematicChain createChain() {
        List<ChainSegment> segments = new ArrayList<>();
        double segmentLength = 1.0;
        int segmentCount = 5;
        
        Vector currentPos = baseLocation.toVector();
        for (int i = 0; i < segmentCount; i++) {
            Vector segmentPos = currentPos.clone().add(new Vector(0, segmentLength, 0));
            segments.add(new ChainSegment(segmentPos, segmentLength));
            currentPos = segmentPos;
        }
        
        return new KinematicChain(baseLocation.toVector(), segments);
    }
    
    private void findTarget() {
        double radius = 4.5;
        List<LivingEntity> nearbyEntities = baseLocation.getWorld().getLivingEntities().stream()
            .filter(entity -> entity.getLocation().distance(baseLocation) <= radius)
            .filter(entity -> entity != owner)
            .filter(entity -> entity.isValid() && !entity.isDead())
            .filter(entity -> !(entity instanceof Player  p &&  p.getGameMode().equals(GameMode.SPECTATOR)))
            .filter(entity -> !(entity instanceof ArmorStand))
            .toList();
        
        if (!nearbyEntities.isEmpty()) {
            targetEntity = nearbyEntities.get(new Random().nextInt(nearbyEntities.size()));
        }
    }
        
    public void update() {
        ticksAlive++;

        if (targetEntity != null && targetEntity.isValid()) {
            double distance = targetEntity.getLocation().distance(baseLocation);
            if (distance > 4.5) {
                targetEntity = null; // Target left range, stop targeting
            }
        }
        
        // Always check for targets if we don't have one
        if (targetEntity == null || !targetEntity.isValid()) {
            findTarget();
        }
        
        if (targetEntity != null && targetEntity.isValid()) {
            // Update chain to reach toward entity using FABRIK
            Vector target = targetEntity.getLocation().toVector().add(new Vector(0, 1, 0));
            if (isFiniteVector(target)) {
                chain.fabrik(target);
            }
        } else {
            // No target - SWAY
            swayTime += 0.1;
            double swayX = Math.sin(swayTime) * 2.0;
            double swayY = Math.sin(swayTime * 0.7) * 1.5 + 3.0;
            double swayZ = Math.cos(swayTime * 0.5) * 2.0;
            
            Vector swayTarget = baseLocation.toVector().add(new Vector(swayX, swayY, swayZ));
            chain.fabrik(swayTarget);
        }

        renderTentacle();
        
        // Remove after pool ends or if no valid target
        if (ticksAlive > 200) {
            shouldRemove = true;
            return;
        }
        if (targetEntity != null && !targetEntity.isValid()) {
            shouldRemove = true;
        }
    }

    private boolean isFiniteVector(Vector vector) {
        return Double.isFinite(vector.getX()) && 
            Double.isFinite(vector.getY()) && 
            Double.isFinite(vector.getZ());
    }
    
    private void renderTentacle() {
        Location root = baseLocation.clone();
        Vector position = chain.getSegments().get(chain.getSegments().size() - 1).getPosition().clone();
        Vector dir = position.subtract(root.toVector());
        Vector upVector = Utils.crossProduct(dir, new Vector(0, 1, 0));

        float maxThickness = 1.5f / 16f * 4;
        float minThickness = 1.5f / 16f * 1;

        for (int i = 0; i < chain.getSegments().size(); i++) {
            ChainSegment segment = chain.getSegments().get(i);
            float thickness = (chain.getSegments().size() - i - 1) * (maxThickness - minThickness) / chain.getSegments().size() + minThickness;
            BlockDisplayRenderer.renderTentacleSegment(root, segment, thickness, upVector); // This calls the dark prismarine version
            root = segment.getPosition().toLocation(baseLocation.getWorld());
        }
    }
    
    public boolean shouldRemove() {
        return shouldRemove;
    }
    
    public void remove() {
        // Clear all BlockDisplays for this tentacle
        for (ChainSegment segment : chain.getSegments()) {
            renderer.clear(BlockDisplayRenderer.Identifier.chainSegment(segment));
        }
        shouldRemove = true;
    }
}
