package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf.Variant;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class TennisBall {
    private ArmorStand base;
    private ItemDisplay ballDisplay;
    private int ticksAlive;
    private Vector velocity;
    private static ItemStack ballItemStack = null;
    private Player owner;
    private List<Wolf> dogs = new ArrayList<>();
    private long lastEntityHitTime = -100; // Tick count of last hit

    private static final CopyOnWriteArrayList<TennisBall> tennisBalls = new CopyOnWriteArrayList<>();

    public TennisBall(Location loc, Player owner, Vector initVelocity) {
        World world = loc.getWorld();
        ballDisplay = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
        if (ballItemStack == null) createBallItemStack();
        ballDisplay.setItemStack(ballItemStack);
        ballDisplay.setTeleportDuration(1);
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

        this.owner = owner;
        tennisBalls.add(this);

        spawnDogs();
        startPhysics();
    }

    private void createBallItemStack() {
        ballItemStack = new ItemStack(Material.SLIME_BALL); // Tennis ball item
        ItemMeta ballItemStackMeta = ballItemStack.getItemMeta();
        ballItemStackMeta.setCustomModelData(104);
        ballItemStack.setItemMeta(ballItemStackMeta);
    }

    private Variant[] types = new Variant[]{ Variant.ASHEN, Variant.BLACK, Variant.CHESTNUT, Variant.PALE, Variant.RUSTY, Variant.SNOWY, Variant.SPOTTED, Variant.STRIPED, Variant.WOODS };

    private void spawnDogs() {
        for (int i = 0; i < 4; i++) {
            Wolf dog = (Wolf) base.getWorld().spawnEntity(base.getLocation(), EntityType.WOLF);
            dog.setCollarColor(DyeColor.LIME);
            dog.setCustomName(owner.getName() + "'s tennis ball fiend");
            dog.setCustomNameVisible(false);
            dog.setTarget(null);
            int j = ThreadLocalRandom.current().nextInt(types.length);
            dog.setVariant(types[j]);
            dog.setAdult();
            dog.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, 0, true, false));
            dogs.add(dog);
        }
    }

    private void startPhysics() {
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!base.isValid() || !ballDisplay.isValid()) {
                remove(false);
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            ticksAlive++;
            if (ticksAlive >= 320) {
                remove(true);
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            velocity.add(new Vector(0, -0.03, 0));
            velocity.multiply(0.995);

            Location currentLoc = base.getLocation();
            Location newLoc = currentLoc.clone().add(velocity);

            Vector collisionNormal = checkCollision(currentLoc, newLoc);
            if (collisionNormal != null) {
                velocity = reflect(velocity, collisionNormal).multiply(0.87);
                newLoc = currentLoc.clone().add(velocity);
                currentLoc.getWorld().playSound(currentLoc, Sound.BLOCK_SLIME_BLOCK_HIT, 0.2f, 1.2f);
            }

            boolean hit = false;
            if (ticksAlive - lastEntityHitTime > 20) { // 1 second cooldown
                for (Entity entity : base.getNearbyEntities(0.5, 0.5, 0.5)) {
                    if (!(entity instanceof LivingEntity) || entity.equals(base) || entity.equals(ballDisplay)) continue;
                    if (entity instanceof Player player && player.getUniqueId().equals(owner.getUniqueId())) continue;
                    if (entity instanceof ArmorStand || entity instanceof ItemDisplay || entity instanceof Wolf) continue;

                    LivingEntity target = (LivingEntity) entity;
                    newLoc.getWorld().playSound(newLoc, Sound.ENTITY_WOLF_GROWL, 0.8f, 1.0f);
                    currentLoc.getWorld().spawnParticle(Particle.CRIT, newLoc, 5);

                    for (Wolf dog : dogs) {
                        dog.setTarget(target);
                        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                            if (dog.isValid() && dog.getTarget() != null && dog.getTarget().equals(target) && !dog.isTamed()) {
                                dog.remove();
                            }
                        }, 200L); // 10 seconds
                    }

                    hit = true;
                    lastEntityHitTime = ticksAlive;
                    break;
                }
            }

            if (hit) {
                Vector normal = base.getLocation().toVector().subtract(newLoc.toVector()).normalize();
                velocity = reflect(velocity, normal).multiply(0.9);
                newLoc = base.getLocation().clone().add(velocity);
            }

            for (Wolf dog : dogs) {
                if (!dog.isValid()) continue;

                Player nearestPlayer = getNearestPlayer(dog.getLocation(), 3.5);
                if (nearestPlayer != null) {
                    dog.setTarget(nearestPlayer);
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                        if (dog.isValid() && dog.getTarget() != null && dog.getTarget().equals(nearestPlayer) && dog.isTamed()) {
                            dog.remove();
                        }
                    }, 200L); // 10 seconds
                } else {
                    dog.setTarget(base);
                }
            }

            base.teleport(newLoc);
            ballDisplay.teleport(newLoc);
        }, 1L, 1L).getTaskId();
    }

    private Player getNearestPlayer(Location loc, double radius) {
        double closestDist = radius * radius;
        Player closest = null;
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.equals(owner)) continue;
            if (player.getGameMode() == GameMode.SPECTATOR) continue;

            double dist = loc.distanceSquared(player.getLocation());
            if (dist <= closestDist) {
                closestDist = dist;
                closest = player;
            }
        }
        return closest;
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
        // Check if the ray trace result indicates a collision with a solid block
        return rayTrace != null && rayTrace.getHitBlock() != null && rayTrace.getHitBlock().getType().isSolid();
    }

    private Vector reflect(Vector velocity, Vector normal) {
        return velocity.subtract(normal.multiply(2 * velocity.dot(normal)));
    }

    public void remove(boolean removeDogs) {
        tennisBalls.remove(this);
        ballDisplay.remove();
        base.remove();
        if(removeDogs){
            for (Wolf dog : dogs) {
                if (dog.isValid() && !dog.isTamed()) dog.remove();
            }
        }
    }

    public static CopyOnWriteArrayList<TennisBall> getTennisBalls() {
        return tennisBalls;
    }

    public int getTicksAlive() {
        return ticksAlive;
    }

    public ArmorStand getArmorStand() {
        return base;
    }

    public ItemDisplay getDisplay() {
        return ballDisplay;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }
}
