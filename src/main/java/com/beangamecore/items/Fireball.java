package com.beangamecore.items;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class Fireball {
    private static final int MAX_LIFETIME_TICKS = 120; // 6 seconds
    private static final double GRAVITY = -0.04;
    private static final double AIR_RESISTANCE = 0.995;
    private static final double BOUNCE_DAMPING = 0.8;
    private static final double DESTRUCTION_CHANCE = 0.20;
    private static final int PARTICLE_START_DELAY = 7;
    
    private ArmorStand base;
    private ItemDisplay fireball;
    private int ticksAlive;
    private Vector velocity;
    private static ItemStack ballItemStack = null;
    private Player owner;
    private BukkitRunnable physicsTask;

    private static CopyOnWriteArrayList<Fireball> fireballs = new CopyOnWriteArrayList<>();

    public Fireball(Location loc, Player owner, Vector initVelocity){
        World world = loc.getWorld();
        fireball = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
        if(ballItemStack == null){
            createBallItemStack();
        }
        fireball.setItemStack(ballItemStack);
        fireball.setTeleportDuration(1);
        ticksAlive = 0;
        velocity = initVelocity;

        base = world.spawn(loc, ArmorStand.class);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hideEntity(Main.getPlugin(), base);
        }
        base.setInvisible(true);
        base.setInvulnerable(true);
        base.setSmall(true);
        base.setCollidable(true);
        base.setGravity(false);
        fireballs.add(this);
        this.owner = owner;

        startPhysics();
    }

    private void createBallItemStack(){
        ballItemStack = new ItemStack(Material.MAGMA_CREAM, 1); 
    }

    private void startPhysics() {
        physicsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (shouldRemove()) {
                    cleanupAndCancel();
                    return;
                }
                
                updatePhysics();
                
                if (hasReachedMaxLifetime()) {
                    cleanupAndCancel();
                    return;
                }
                
                Location currentLoc = base.getLocation();
                Location newLoc = calculateNewLocation(currentLoc);
                
                if (handleWallCollision(currentLoc, newLoc)) {
                    newLoc = currentLoc.clone().add(velocity);
                    
                    if (shouldDestroyOnCollision(newLoc)) {
                        cleanupAndCancel();
                        return;
                    }
                }
                
                if (handleEntityCollision(newLoc)) {
                    return;
                }
                
                updateVisualEffects(newLoc);
                updatePosition(newLoc);
            }
        };
        physicsTask.runTaskTimer(Main.getPlugin(), 1L, 1L);
    }
    
    private boolean shouldRemove() {
        return !base.isValid() || !fireball.isValid();
    }
    
    private void cleanupAndCancel() {
        remove();
        physicsTask.cancel();
    }
    
    private void updatePhysics() {
        ticksAlive++;
        velocity.add(new Vector(0, GRAVITY, 0));
        velocity.multiply(AIR_RESISTANCE);
    }
    
    private boolean hasReachedMaxLifetime() {
        return ticksAlive >= MAX_LIFETIME_TICKS;
    }
    
    private Location calculateNewLocation(Location currentLoc) {
        return currentLoc.clone().add(velocity);
    }
    
    private boolean handleWallCollision(Location currentLoc, Location newLoc) {
        Vector collisionNormal = checkCollision(currentLoc, newLoc);
        if (collisionNormal != null) {
            velocity = reflect(velocity, collisionNormal).multiply(BOUNCE_DAMPING);
            currentLoc.getWorld().playSound(currentLoc, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 1.4f);
            return true;
        }
        return false;
    }
    
    private boolean shouldDestroyOnCollision(Location newLoc) {
        return Math.random() < DESTRUCTION_CHANCE || newLoc.getBlock().getType().equals(Material.WATER);
    }
    
    private boolean handleEntityCollision(Location newLoc) {
        for (Entity entity : base.getNearbyEntities(0.5, 0.5, 0.5)) {
            if (!isValidTarget(entity)) {
                continue;
            }
            
            LivingEntity target = (LivingEntity) entity;
            dealDamageAndEffects(target, newLoc);
            
            cleanupAndCancel();
            return true;
        }
        return false;
    }
    
    private boolean isValidTarget(Entity entity) {
        if (isInvalidEntity(entity)) {
            return false;
        }

        if (isInvalidPlayer(entity)) {
            return false;
        }

        return !(entity instanceof ArmorStand) && !(entity instanceof ItemDisplay);
    }

    private boolean isInvalidPlayer(Entity entity) {
        if (entity instanceof Player player) {
            return player.getGameMode().equals(GameMode.SPECTATOR) ||
                    player.getUniqueId().equals(owner.getUniqueId());
        }
        return false;
    }

    private boolean isInvalidEntity(Entity entity) {
        return !(entity instanceof LivingEntity) || entity.equals(base) || entity.equals(fireball);
    }
    
    private void dealDamageAndEffects(LivingEntity target, Location location) {
        target.damage(6, (Entity) owner);
        location.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 1.0f);
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 1);
        target.setFireTicks(20);
    }
    
    private void updateVisualEffects(Location newLoc) {
        if (ticksAlive > PARTICLE_START_DELAY) {
            newLoc.getWorld().spawnParticle(Particle.FLAME, newLoc, 2, 0, 0, 0, 0);
        }
    }
    
    private void updatePosition(Location newLoc) {
        base.teleport(newLoc);
        fireball.teleport(newLoc);
    }

    private Vector checkCollision(Location from, Location to) {
        World world = from.getWorld();
        Vector direction = to.toVector().subtract(from.toVector());
        double length = direction.length();
        direction.normalize();

        RayTraceResult rayTrace = world.rayTraceBlocks(from, direction, length);
        if (isSolidBlockCollision(rayTrace)) {
            if(rayTrace.getHitBlock().getType().equals(Material.TNT)){
                Block block = rayTrace.getHitBlock();
                block.setType(Material.AIR);

                Entity tnt = block.getWorld().spawn(block.getLocation().add(0.5, 0.5, 0.5), TNTPrimed.class);
                ((TNTPrimed) tnt).setFuseTicks(64);
            } 
            return rayTrace.getHitBlockFace().getDirection();
        }

        return null;
    }

    private boolean isSolidBlockCollision(RayTraceResult rayTrace) {
        return rayTrace != null && rayTrace.getHitBlock() != null && rayTrace.getHitBlock().getType().isSolid();
    }

    public void setVelocity(Vector v){
        this.velocity = v;
    }

    private Vector reflect(Vector velocity, Vector normal) {
        return velocity.subtract(normal.multiply(2 * velocity.dot(normal)));
    }

    public static CopyOnWriteArrayList<Fireball> getFireballs(){
        return fireballs;
    }

    public int getTicksAlive(){
        return ticksAlive;
    }

    public ArmorStand getArmorStand(){
        return base;
    }

    public ItemDisplay getDisplay(){
        return fireball;
    }

    public void remove(){
        fireballs.remove(this);
        fireball.remove();
        base.remove();
        if (physicsTask != null && !physicsTask.isCancelled()) {
            physicsTask.cancel();
        }
    }
}
