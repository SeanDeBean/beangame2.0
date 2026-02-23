package com.beangamecore.items;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Marker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.particles.BeangameParticleManager;

public class CosmicFallingStar {
    
    protected ItemDisplay star;
    protected Marker marker;
    protected Vector direction;
    protected LivingEntity owner;
    protected int rotDir;
    protected int ticksAlive;

    public static CopyOnWriteArrayList<CosmicFallingStar> fallingStars = new CopyOnWriteArrayList<>();
    
    public static void summon(Location start, Location end, LivingEntity owner){
        fallingStars.add(new CosmicFallingStar(start, end, owner));
    }

    public CosmicFallingStar(Location start, Location end, LivingEntity owner){
        if(owner == null) return;
        Location difference = start.clone().subtract(end);
        direction = difference.toVector().multiply(-1).normalize().multiply(1.25);
        World world = difference.getWorld();
        star = (ItemDisplay) world.spawnEntity(start, EntityType.ITEM_DISPLAY);
        marker = (Marker) world.spawnEntity(start, EntityType.MARKER);
        star.getLocation().setPitch(CosmicFury.CosmicFuryRandomizer.nextInt(180) - 90);
        star.getLocation().setYaw(CosmicFury.CosmicFuryRandomizer.nextInt(360));
        ItemStack item = new ItemStack(Material.GLOWSTONE);
        item.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
        star.setItemStack(item);
        this.owner = owner;
        ticksAlive = 0;
        rotDir = CosmicFury.CosmicFuryRandomizer.nextInt(3) - 1;
        star.setTeleportDuration(1);
    }

    public void tickFallingStars(){
        ticksAlive++;
        marker.teleport(marker.getLocation().add(direction));
        Location temporary = marker.getLocation().clone();
        temporary.setYaw(nextYaw(star.getLocation().getYaw() + (3f * rotDir)));
        temporary.setPitch(nextPitch(star.getLocation().getPitch() + (2f * rotDir)));
        star.teleport(temporary);
    }

    protected float nextYaw(float input){
        float freturn = input;
        if(freturn > 180) freturn -= 360;
        if(freturn < -180) freturn += 360;
        return freturn;
    }

    protected float nextPitch(float input){
        float freturn = input;
        if(freturn > 90) freturn -= 180;
        if(freturn < -90) freturn += 180;
        return freturn;
    }

    public int getTicksAlive(){
        return ticksAlive;
    }

    public ItemDisplay getItemDisplay(){
        return star;
    }

    public LivingEntity getOwner(){
        return owner;
    }

    public void remove(){
        star.getWorld().playSound(star.getLocation(), Sound.ENTITY_ALLAY_ITEM_THROWN, 1F, 1);
        star.getWorld().spawnParticle(Particle.EXPLOSION, star.getLocation(), 1);
        star.remove();
        marker.remove();
    }

    public void starParticles(){
        Location start = marker.getLocation();
        Location end = marker.getLocation().clone().add(direction);
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        for(int i = 0; i < 3; i++){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                public void run(){
                    particleManager.particleTrail(start, end, 255, 255, 135);
                    particleManager.particleTrail(start, end, 215, 85, 255);
                }     
            }, i*1L);
        }
    }
}

