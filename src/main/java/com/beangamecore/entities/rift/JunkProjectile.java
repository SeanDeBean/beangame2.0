package com.beangamecore.entities.rift;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.registry.BeangameItemRegistry;

public class JunkProjectile {
    private final Plugin plugin;
    private final Player owner;
    private final BeangameItem item;
    private final World world;
    private final ItemDisplay itemDisplay;
    private final Vector direction;
    private final double damage;
    
    private int maxTicks = 40;
    private int ticks = 0;

    public JunkProjectile(Plugin plugin, Player owner, Location origin, Vector direction, double damage) {
        this.plugin = plugin;
        this.owner = owner;
        this.world = origin.getWorld();
        this.direction = direction.normalize().multiply(0.7);
        this.damage = damage;

        this.item = new ArrayList<>(BeangameItemRegistry.getRegistry().values())
                .get(new Random().nextInt(BeangameItemRegistry.getRegistry().size()));
        this.itemDisplay = world.spawn(origin, ItemDisplay.class, display -> {
            ItemStack drop = item.asItem();
            display.setItemStack(drop);
            
            display.setBillboard(Display.Billboard.CENTER);
            display.setBrightness(new Display.Brightness(15, 15));
            
            Transformation transformation = createFallingSwordTransformation();
            display.setTransformation(transformation);
            
            display.setPersistent(true);
            display.setGravity(false);
            display.setTeleportDuration(1);
        });

        start();
    }

    private Transformation createFallingSwordTransformation() {
        Quaternionf downwardRotation = new Quaternionf()
            .rotationXYZ(0, (float) Math.toRadians(-90), (float) Math.toRadians(-90));
        
        Random random = new Random();
        float randomSpin = (float) (random.nextFloat() * (float) Math.PI * 2);
        Quaternionf spinRotation = new Quaternionf()
            .rotationZ(randomSpin);
            
        downwardRotation.mul(spinRotation);
        
        return new Transformation(
            new Vector3f(0, 0, 0),
            downwardRotation,
            new Vector3f(0.8f, 0.8f, 0.8f),
            new Quaternionf() 
        );
    }

    private void start() {
        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (ticks++ > maxTicks || itemDisplay.isDead()) {
                itemDisplay.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Location current = itemDisplay.getLocation();
            Location next = current.clone().add(direction);

            // Add spinning rotation while falling
            updateSwordRotation();

            // Collision check
            for (Entity entity : world.getNearbyEntities(current, 0.5, 0.5, 0.5)) {
                if (entity instanceof LivingEntity hit && 
                        !(entity instanceof ArmorStand) &&
                        !(entity instanceof ItemDisplay) &&
                        !(entity instanceof Player p && p.getGameMode().equals(GameMode.SPECTATOR))) {

                    if(item instanceof BGDDealerHeldI i){
                        ItemStack stack = item.asItem();
                        DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
                        builder.withCausingEntity(owner);
                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(owner, hit, EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), damage);
                        i.attackerOnHit(event, stack);
                    } else if (item instanceof BGDDealerInvI i){
                        ItemStack stack = item.asItem();
                        DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_ATTACK);
                        builder.withCausingEntity(owner);
                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(owner, hit, EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), damage);
                        i.attackerInventoryOnHit(event, stack);
                    } else {
                        DamageSource source = DamageSource.builder(DamageType.MACE_SMASH)
                            .withCausingEntity((Entity) owner)
                            .build();
                        hit.damage(damage, source);
                    }

                    createImpactEffects(current);
                    itemDisplay.remove();
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
            }

            // Solid block collision
            if (next.getBlock().getType().isSolid()) {
                createImpactEffects(current);
                    itemDisplay.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            // Move projectile
            itemDisplay.teleport(next);

            // Falling sword trail effects
            createTrailEffects(next);
        }, 1, 1).getTaskId();
    }

    private void updateSwordRotation() {
        // Spin the sword while falling (360 degrees over 2 seconds = 40 ticks)
        float spinAngle = (float) (ticks * (2 * Math.PI / 20));
        
        Transformation currentTransformation = itemDisplay.getTransformation();
        Quaternionf spinRotation = new Quaternionf()
            .rotationZ(spinAngle);
            
        // Combine with original downward rotation
        Quaternionf newRotation = new Quaternionf()
            .rotationXYZ((float) Math.toRadians(-90), 0, 0)
            .mul(spinRotation);
            
        itemDisplay.setTransformation(new Transformation(
            currentTransformation.getTranslation(),
            newRotation,
            currentTransformation.getScale(),
            currentTransformation.getRightRotation()
        ));
    }

    private void createTrailEffects(Location location) {
        // Sword trail particles
        if (Math.random() > 0.4) {
            world.spawnParticle(Particle.CRIT, location, 2, 0.1, 0.1, 0.1, 0.02);
        }
        
        // Occasional sparkle
        if (ticks % 3 == 0) {
            world.spawnParticle(Particle.ELECTRIC_SPARK, location, 1, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private void createImpactEffects(Location location) {
        world.spawnParticle(Particle.CRIT, location, 3, 0.3, 0.3, 0.3, 0.1);
        world.spawnParticle(Particle.SWEEP_ATTACK, location, 2, 0.5, 0.5, 0.5, 0.05);
        world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.2f, 1.2f);
        world.playSound(location, Sound.ITEM_TRIDENT_HIT, 0.1f, 1.5f);
    }
}
