package com.beangamecore.items;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

import com.beangamecore.Main;

public class BaleoutProjectile {
    private static final int MAX_LIFETIME_TICKS = 100; // 5 seconds
    private static final double GRAVITY = -0.05;
    private static final double AIR_RESISTANCE = 0.997;
    private static final double BOUNCE_DAMPING = 0.8;
    private static final double SPIN_DECAY = 0.95;
    private static final int PARTICLE_START_DELAY = 7;
    private static final int WHEAT_DROP_INTERVAL = 3;
    
    private ArmorStand base;
    private ItemDisplay bale;
    private int ticksAlive;
    private Vector velocity;
    private static ItemStack baleItemStack = null;
    private Player owner;
    private Vector residualSpin = new Vector(0, 0, 0);
    private BukkitRunnable physicsTask;

    private static CopyOnWriteArrayList<BaleoutProjectile> projectiles = new CopyOnWriteArrayList<>();

    public BaleoutProjectile(Location loc, Player owner, Vector initVelocity){
        World world = loc.getWorld();
        bale = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
        if(baleItemStack == null){
            createBaleItemStack();
        }
        bale.setItemStack(baleItemStack);
        bale.setTeleportDuration(1);
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
        projectiles.add(this);
        this.owner = owner;

        startPhysics();
    }

    private void createBaleItemStack(){
        baleItemStack = new ItemStack(Material.HAY_BLOCK, 1); 
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
                    handleLifetimeExpiration();
                    return;
                }
                
                Location currentLoc = base.getLocation();
                Location newLoc = calculateNewLocation(currentLoc);
                
                if (handleWallCollision(currentLoc, newLoc)) {
                    newLoc = currentLoc.clone().add(velocity);
                    
                    if (handleHazardCollision(newLoc, currentLoc)) {
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
        return !base.isValid() || !bale.isValid();
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
    
    private void handleLifetimeExpiration() {
        Location loc = bale.getLocation();
        bale.getLocation().getBlock().setType(Material.HAY_BLOCK);
        loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 0.9f, 1.4f);
        cleanupAndCancel();
    }
    
    private Location calculateNewLocation(Location currentLoc) {
        return currentLoc.clone().add(velocity);
    }
    
    private boolean handleWallCollision(Location currentLoc, Location newLoc) {
        Vector collisionNormal = checkCollision(currentLoc, newLoc);
        if (collisionNormal != null) {
            velocity = reflect(velocity, collisionNormal).multiply(BOUNCE_DAMPING);
            applyCollisionSpin();
            return true;
        }
        return false;
    }
    
    private void applyCollisionSpin() {
        float spinX = (float) (Math.random() * 10 - 5);
        float spinY = (float) (Math.random() * 10 - 5);
        float spinZ = (float) (Math.random() * 10 - 5);
        residualSpin = new Vector(spinX, spinY, spinZ);
        
        applyRotationToDisplay(spinX, spinY, spinZ);
    }
    
    private void applyRotationToDisplay(float spinX, float spinY, float spinZ) {
        Transformation t = bale.getTransformation();
        Quaternionf rotation = t.getLeftRotation();
        
        rotation.rotateXYZ(
            (float) Math.toRadians(spinX),
            (float) Math.toRadians(spinY),
            (float) Math.toRadians(spinZ)
        );
        
        Transformation old = bale.getTransformation();
        Quaternionf newLeft = new Quaternionf(old.getLeftRotation()).rotateY((float) Math.toRadians(10));
        
        Transformation updated = new Transformation(
            old.getTranslation(),
            newLeft,
            old.getScale(),
            old.getRightRotation()
        );
        
        bale.setTransformation(updated);
    }
    
    private boolean handleHazardCollision(Location newLoc, Location currentLoc) {
        if (newLoc.getBlock().getType().equals(Material.LAVA) || 
            newLoc.getBlock().getType().equals(Material.FIRE)) {
            
            currentLoc.getWorld().playSound(currentLoc, Sound.BLOCK_FIRE_EXTINGUISH, 0.1f, 1.4f);
            currentLoc.getWorld().playSound(currentLoc, Sound.BLOCK_GRASS_BREAK, 0.9f, 1.4f);
            newLoc.getBlock().setType(Material.HAY_BLOCK);
            cleanupAndCancel();
            return true;
        }
        return false;
    }
    
    private boolean handleEntityCollision(Location newLoc) {
        for (Entity entity : base.getNearbyEntities(0.5, 0.5, 0.5)) {
            if (!isValidTarget(entity)) {
                continue;
            }
            
            LivingEntity target = (LivingEntity) entity;
            target.damage(6, (Entity) owner);
            newLoc.getWorld().playSound(newLoc, Sound.BLOCK_GRASS_BREAK, 1.1f, 1.4f);
            newLoc.getBlock().setType(Material.HAY_BLOCK);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            
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
        return !(entity instanceof LivingEntity) || entity.equals(base) || entity.equals(bale);
    }
    
    private void updateVisualEffects(Location newLoc) {
        updateParticleEffects(newLoc);
        updateResidualSpin();
        createSpiralEffect(newLoc);
    }
    
    private void updateParticleEffects(Location newLoc) {
        if (ticksAlive > PARTICLE_START_DELAY) {
            if (ticksAlive % WHEAT_DROP_INTERVAL == 0) {
                dropWheatItem(newLoc);
            }
            
            DustOptions dustOptions = new DustOptions(Color.fromRGB(225, 193, 110), 0.8f);
            newLoc.getWorld().spawnParticle(Particle.DUST, newLoc, 1, dustOptions);
        }
    }
    
    private void dropWheatItem(Location location) {
        ItemStack wheat = new ItemStack(Material.WHEAT);
        ItemMeta meta = wheat.getItemMeta();
        meta.setMaxStackSize(3);
        wheat.setItemMeta(meta);
        Item item = bale.getWorld().dropItem(location, wheat);
        item.setTicksLived(5900);
    }
    
    private void updateResidualSpin() {
        if (!residualSpin.equals(new Vector(0, 0, 0))) {
            Transformation t = bale.getTransformation();
            Quaternionf rotation = t.getLeftRotation();
            
            rotation.rotateXYZ(
                (float) Math.toRadians(residualSpin.getX()),
                (float) Math.toRadians(residualSpin.getY()),
                (float) Math.toRadians(residualSpin.getZ())
            );
            
            residualSpin.multiply(SPIN_DECAY);
            
            Transformation old = bale.getTransformation();
            Quaternionf newLeft = new Quaternionf(old.getLeftRotation()).rotateY((float) Math.toRadians(10));
            
            Transformation updated = new Transformation(
                old.getTranslation(),
                newLeft,
                old.getScale(),
                old.getRightRotation()
            );
            
            bale.setTransformation(updated);
        }
    }
    
    private void createSpiralEffect(Location newLoc) {
        double angle = ticksAlive * 0.3;
        double xOffset = Math.cos(angle) * 1.0;
        double zOffset = Math.sin(angle) * 1.0;
        
        Location spiralLoc = newLoc.clone().add(xOffset, 0.4, zOffset);
        DustOptions dustOptions = new DustOptions(Color.fromRGB(225, 193, 110), 0.8f);
        bale.getWorld().spawnParticle(Particle.DUST, spiralLoc, 0, 
            0, 0, 0, 0, dustOptions);
    }
    
    private void updatePosition(Location newLoc) {
        base.teleport(newLoc);
        bale.teleport(newLoc);
    }

    private Vector checkCollision(Location from, Location to) {
        World world = from.getWorld();
        Vector direction = to.toVector().subtract(from.toVector());
        double length = direction.length();
        direction.normalize();

        RayTraceResult rayTrace = world.rayTraceBlocks(from, direction, length);
        if (isSolidBlockCollision(rayTrace)) {
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

    public static CopyOnWriteArrayList<BaleoutProjectile> getBales(){
        return projectiles;
    }

    public int getTicksAlive(){
        return ticksAlive;
    }

    public ArmorStand getArmorStand(){
        return base;
    }

    public ItemDisplay getDisplay(){
        return bale;
    }

    public void remove(){
        projectiles.remove(this);
        bale.remove();
        base.remove();
        if (physicsTask != null && !physicsTask.isCancelled()) {
            physicsTask.cancel();
        }
    }
}
