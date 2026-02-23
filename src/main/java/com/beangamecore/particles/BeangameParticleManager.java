package com.beangamecore.particles;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class BeangameParticleManager {
    
    private void particleSpawn(Location loc, Particle particle, double a, double b, double c, int size, double x, double y, double z, int count){
        if(particle == Particle.DUST){
            int ia = (int) Math.round(a);
            int ib = (int) Math.round(b);
            int ic = (int) Math.round(c);
            DustOptions dustOptions = new DustOptions(Color.fromRGB(ia, ib, ic), size);
            loc.getWorld().spawnParticle(particle, loc.getX()+x, loc.getY()+y, loc.getZ()+z, count, dustOptions);
        }
    }

    public void particleTrail(Location start, Location end, int r, int g, int b){
        Vector direction = end.toVector().subtract(start.toVector()).normalize().multiply(0.5);
        double distance = start.distance(end);
        Location current = start.clone();
        for(double i = 0; i < distance; i += 0.5){
            current.add(direction);
            DustOptions dustOptions = new DustOptions(Color.fromRGB(r, g, b), 1);
            current.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
    }

    public void particleTrail(Location start, Location end, int r, int g, int b, int size){
        Vector direction = end.toVector().subtract(start.toVector()).normalize().multiply(0.5);
        double distance = start.distance(end);
        Location current = start.clone();
        for(double i = 0; i < distance; i += 0.5){
            current.add(direction);
            DustOptions dustOptions = new DustOptions(Color.fromRGB(r, g, b), size);
            current.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
    }

    public void particleCube(Block corner1, Block corner2, int r, int g, int b){
        Location loc1 = corner1.getLocation().add(0.5, 0.5, 0.5);
        Location loc2 = corner2.getLocation().add(0.5, 0.5, 0.5);
        Location temp1 = corner1.getLocation().add(0.5, 0.5, 0.5);
        Location temp2 = corner2.getLocation().add(0.5, 0.5, 0.5);
        temp1.setX(loc2.getX());
        particleTrail(loc1, temp1, r, g, b);
        temp2.setZ(loc1.getZ());
        particleTrail(loc2, temp2, r, g, b);
        particleTrail(temp1, temp2, r, g, b);
        temp2.setZ(loc2.getZ());
        temp2.setY(loc1.getY());
        particleTrail(temp1, temp2, r, g, b);
        particleTrail(loc2, temp2, r, g, b);
        temp1.setX(loc1.getX());
        temp1.setZ(loc2.getZ());
        particleTrail(temp1, temp2, r, g, b);
        particleTrail(loc1, temp1, r, g, b);
        temp2.setY(loc2.getY());
        temp2.setX(loc1.getX());
        particleTrail(loc2, temp2, r, g, b);
        particleTrail(temp1, temp2, r, g, b);
        temp1.setZ(loc1.getZ());
        temp1.setY(loc2.getY());
        particleTrail(loc1, temp1, r, g, b);
        particleTrail(temp1, temp2, r, g, b);
        temp2.setX(loc2.getX());
        temp2.setZ(loc1.getZ());
        particleTrail(temp1, temp2, r, g, b);
    }

    public void particleCube(Location corner1, Location corner2, int r, int g, int b){
        Location loc1 = corner1;
        Location loc2 = corner2;
        Location temp1 = corner1.clone();
        Location temp2 = corner2.clone();
        temp1.setX(loc2.getX());
        particleTrail(loc1, temp1, r, g, b);
        temp2.setZ(loc1.getZ());
        particleTrail(loc2, temp2, r, g, b);
        particleTrail(temp1, temp2, r, g, b);
        temp2.setZ(loc2.getZ());
        temp2.setY(loc1.getY());
        particleTrail(temp1, temp2, r, g, b);
        particleTrail(loc2, temp2, r, g, b);
        temp1.setX(loc1.getX());
        temp1.setZ(loc2.getZ());
        particleTrail(temp1, temp2, r, g, b);
        particleTrail(loc1, temp1, r, g, b);
        temp2.setY(loc2.getY());
        temp2.setX(loc1.getX());
        particleTrail(loc2, temp2, r, g, b);
        particleTrail(temp1, temp2, r, g, b);
        temp1.setZ(loc1.getZ());
        temp1.setY(loc2.getY());
        particleTrail(loc1, temp1, r, g, b);
        particleTrail(temp1, temp2, r, g, b);
        temp2.setX(loc2.getX());
        temp2.setZ(loc1.getZ());
        particleTrail(temp1, temp2, r, g, b);
    }

    public void lightningEffect(Location start, Location end, int r, int g, int b) {
        DustOptions dustOptions = new DustOptions(Color.fromRGB(r, g, b), 1);
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        Location current = start.clone();
        Random random = new Random();
        double maxHeight = Math.max(start.getY(), end.getY()) + 1.7;

        for (double d = 0; d < length; d += 0.125) {
            double t = d / length;
            double parabolicHeight = (4 * maxHeight - 4 * (start.getY() + (end.getY() - start.getY()) * t)) * t * (1 - t);
            Vector offset = new Vector(
                (random.nextDouble() - 0.5) * 0.3, 
                parabolicHeight + (random.nextDouble() - 0.5) * 0.5, 
                (random.nextDouble() - 0.5) * 0.3
            );
            current = start.clone().add(direction.clone().multiply(d)).add(offset);
            current.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
    }

    public void spawnParticleSphere(Location center, double radius, Particle particle, DustOptions dust, int density) {
        World world = center.getWorld();
        for (int i = 0; i < density; i++) {
            double theta = Math.random() * 2 * Math.PI; // Angle around the vertical axis
            double phi = Math.acos(2 * Math.random() - 1); // Angle from the vertical axis

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            Location particleLocation = center.clone().add(x, y, z);
            world.spawnParticle(particle, particleLocation, 1, dust);
        }
    }

    public void beanParticle(Location loc){
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.1875, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.1875, 0, -0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.1875, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.1875, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.1875, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.1875, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.1875, 0, -0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.0625, 0, -0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.0625, 0, -0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.0625, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.0625, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.0625, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.0625, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 1.0625, 0, 0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, -0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, -0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, 0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, 0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.9375, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, 0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, 0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.8125, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, -1.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.6875, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.5625, 0, -1.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.5625, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.5625, 0, 0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.5625, 0, 0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.5625, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.5625, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.5625, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.4375, 0, -1.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.4375, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.4375, 0, 0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.4375, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.4375, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.4375, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.4375, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, -1.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.3125, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, -0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, -0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, -0.1875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.1875, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, 0.0625, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, -0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, -0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, -0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, -0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.0625, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, -0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, -0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.1875, 0, 1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.3125, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.3125, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.3125, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.3125, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.3125, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.3125, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.3125, 0, 1, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.4375, 0, -0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.4375, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.4375, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.4375, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.4375, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.4375, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.5625, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.5625, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.5625, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.5625, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.5625, 0, 0.875, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.6875, 0, -0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.6875, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.6875, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.6875, 0, 0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.6875, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.6875, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.6875, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, -0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, 0, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, 0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, 0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, 0.5, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, 0.625, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.8125, 0, 0.75, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.9375, 0, 0.125, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.9375, 0, 0.25, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.9375, 0, 0.375, 1);
        particleSpawn(loc, Particle.DUST, 245, 80, 10, 1, -0.9375, 0, 0.5, 1);
    }
}

