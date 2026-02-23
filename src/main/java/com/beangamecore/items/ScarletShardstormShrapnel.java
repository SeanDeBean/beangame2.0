package com.beangamecore.items;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ScarletShardstormShrapnel {

    private final Player owner;
    private final Vector relativeOffset;
    private final World world;
    private final double range;
    private final double speed;
    private final int travelTicks;
    private final Plugin plugin;

    private final ItemDisplay display;

    public ScarletShardstormShrapnel(Player owner, Vector relativeOffset, double range, double speed, Plugin plugin) {
        this.owner = owner;
        this.relativeOffset = relativeOffset;
        this.range = range;
        this.plugin = plugin;
        this.world = owner.getWorld();
        this.speed = 1.5;
        this.travelTicks = range == 12 ? 10 : 6;

        Location spawnLoc = owner.getEyeLocation().add(relativeOffset);
        this.display = spawnShrapnel(spawnLoc);
        followPlayerThenShoot();
    }

    private ItemDisplay spawnShrapnel(Location location) {
        ItemDisplay display = (ItemDisplay) world.spawnEntity(location, EntityType.ITEM_DISPLAY);
        display.setItemStack(new ItemStack(Material.RED_CONCRETE));

        Vector direction = getDirection(owner.getEyeLocation(), location).normalize();
        updateDisplayRotation(display, direction);

        display.setBillboard(Display.Billboard.FIXED);
        display.setPersistent(false);
        display.setGravity(false);
        display.setTeleportDuration(1);
        display.setBrightness(new Display.Brightness(15, 15));

        location.getWorld().playSound(location, Sound.ENTITY_GUARDIAN_HURT, 0.3f, 0.7f);

        return display;
    }

    private void updateDisplayRotation(ItemDisplay display, Vector direction) {
        Vector defaultForward = new Vector(0, 0, 1);
        Quaternionf rotation = quaternionFromTo(defaultForward, direction);

        Transformation transformation = new Transformation(
            new Vector3f(0, 0, 0),
            rotation,
            new Vector3f(0.11f, 0.11f, 0.11f),
            new Quaternionf()
        );

        display.setTransformation(transformation);
    }

    private Quaternionf quaternionFromTo(Vector from, Vector to) {
        Vector f = from.clone().normalize();
        Vector t = to.clone().normalize();

        float dot = (float) f.dot(t);
        Vector cross = f.clone().crossProduct(t);
        float crossMag = (float) cross.length();

        if (crossMag == 0) {
            if (dot > 0) {
                return new Quaternionf(); // identity
            } else {
                return new Quaternionf().rotateAxis((float) Math.PI, 0, 1, 0);
            }
        }

        float angle = (float) Math.acos(dot);
        cross.normalize();
        return new Quaternionf().rotateAxis(angle, (float) cross.getX(), (float) cross.getY(), (float) cross.getZ());
    }

    private void particle(Location to, Location from) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize().multiply(0.4);
        double distance = from.distance(to);
        Location current = from.clone();
        for (double i = 0; i < distance; i += 0.5) {
            current.add(direction);
            DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 0.75f);
            current.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
        if(Math.random() <= 0.1)
        to.getWorld().playSound(to, Sound.BLOCK_WOOL_BREAK, 0.3f, 1.2f);
    }

    private void followPlayerThenShoot() {
        int[] taskId = new int[1];
        int[] ticks = {0};
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (shouldCancelTask(owner, world, display)) {
                display.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            if (ticks[0] < 16) {
                Location eye = owner.getEyeLocation();
                Location newLoc = eye.add(relativeOffset);
                Location oldLoc = display.getLocation();
                display.teleport(newLoc);

                Vector dir = getDirection(owner.getEyeLocation(), newLoc).normalize();
                updateDisplayRotation(display, dir);
                if (ticks[0] % 2 == 0) {
                    particle(oldLoc, newLoc);
                }
                ticks[0]++;
            } else {
                shoot(display.getLocation());
                Bukkit.getScheduler().cancelTask(taskId[0]);
            }
        }, 0, 1).getTaskId();
    }

    private boolean shouldCancelTask(Player owner, World world, Display display) {
        return owner == null || !owner.isOnline() || owner.getWorld() != world || world == null;
    }

    private Vector getDirection(Location eye, Location start) {
        Vector eyeDir = eye.getDirection().normalize();
        Location target = eye.clone().add(eyeDir.multiply(range));
        return target.toVector().subtract(start.toVector()).normalize();
    }

    private void shoot(Location start) {
        if (shouldTerminateTask()) {
            display.remove();
            return;
        }

        Location eye = owner.getEyeLocation();
        Location current = start.clone();
        Vector shootDir = getDirection(eye, current).normalize();
        Vector velocity = shootDir.multiply(speed);
        Vector initialDirection = shootDir.clone();

        int[] taskId = new int[1];
        int[] traveledTicks = {0};
        boolean[] stopAfterThisTick = {false};
        Set<UUID> hitRecently = new HashSet<>();
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (shouldTerminateTask()) {
                display.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            if (traveledTicks[0]++ >= travelTicks || stopAfterThisTick[0]) {
                display.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Location oldLoc = current.clone();
            current.add(velocity);

            if (isBlockInPath(oldLoc, current)) {
                display.teleport(current); // one last teleport
                stopAfterThisTick[0] = true;
                return;
            }

            display.teleport(current);

            if (traveledTicks[0] == 1) {
                updateDisplayRotation(display, initialDirection);
            }

            handleEntityHits(current, hitRecently);

            if (traveledTicks[0] % 2 == 0) {
                particle(oldLoc, current);
                hitRecently.clear();
            }
        }, 0, 1).getTaskId();
    }

    private boolean shouldTerminateTask() {
        return owner == null || !owner.isOnline() || world == null || display.isDead();
    }

    // Extracted method
    private void handleEntityHits(Location current, Set<UUID> hitRecently) {
        for (Entity e : world.getNearbyEntities(current, 1, 1, 1)) {
            if (!(e instanceof LivingEntity entity))
                continue;
            if (entity.getUniqueId().equals(owner.getUniqueId()))
                continue;
            if (hitRecently.contains(entity.getUniqueId()))
                continue;

            hitRecently.add(entity.getUniqueId());
            entity.damage(Math.random() >= 0.4 ? 1.5 : 2.0, owner);
            world.playSound(entity.getLocation(), Sound.BLOCK_WOOL_STEP, 0.8f, 1.2f);
        }
    }

    private boolean isBlockInPath(Location start, Location end) {
        Vector startVec = start.toVector();
        Vector endVec = end.toVector();
        Vector direction = endVec.clone().subtract(startVec).normalize();

        double distance = startVec.distance(endVec);
        double stepSize = 0.1;

        for (double i = 0; i <= distance; i += stepSize) {
            Vector point = startVec.clone().add(direction.clone().multiply(i));
            Location checkLoc = point.toLocation(start.getWorld());
            if (checkLoc.getBlock().getType().isSolid() || checkLoc.getBlock().getType().equals(Material.COBWEB)) {
                return true;
            }
        }
        return false;
    }
}
