package com.beangamecore.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.Main;
import com.beangamecore.particles.BeangameParticleManager;

public class CosmicCirclingStar extends CosmicFallingStar {
    
    boolean circling;

    private CosmicCirclingStar(Location start, Location end, Player owner) {
        super(start, end, null);
        throw new UnsupportedOperationException("Don't use the 3 param constructor for CosmicCirclingStar!");
    }

    Location prevLocation1 = null;
    Location prevLocation2 = null;

    public CosmicCirclingStar(Player owner){
        super(null, null, null);
        World world = owner.getWorld();
        star = (ItemDisplay) world.spawnEntity(owner.getEyeLocation(), EntityType.ITEM_DISPLAY);
        marker = (Marker) world.spawnEntity(owner.getEyeLocation(), EntityType.MARKER);
        star.getLocation().setPitch(CosmicFury.CosmicFuryRandomizer.nextInt(180) - 90);
        star.getLocation().setYaw(CosmicFury.CosmicFuryRandomizer.nextInt(360));
        ItemStack item = new ItemStack(Material.GLOWSTONE);
        item.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
        star.setItemStack(item);
        this.owner = owner;
        ticksAlive = 0;
        rotDir = 1;
        circling = true;
        star.setTeleportDuration(1);
    }

    @Override
    public void tickFallingStars(){
        ticksAlive++;
        if(prevLocation1 != null){
            prevLocation2 = prevLocation1.clone();
        }
        double a = Math.toRadians(ticksAlive);
        if(circling){
            Location loc = owner.getEyeLocation();
            double x = loc.getX() + 6 * Math.cos(a);
            double y = loc.getY();
            double z = loc.getZ() + 6 * Math.sin(a);
            Location newLoc = new Location(owner.getWorld(), x, y, z);
            prevLocation1 = newLoc;
            marker.teleport(newLoc);
        }
        Location temporary = marker.getLocation().clone();
        temporary.setYaw(nextYaw(star.getLocation().getYaw() + (3f * rotDir)));
        temporary.setPitch(nextPitch(star.getLocation().getPitch() + (2f * rotDir)));
        star.teleport(temporary);
        starParticles(marker.getLocation().clone());
    }

    public void starParticles(Location current){
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        if(prevLocation2 != null && prevLocation1 != null && prevLocation1.getWorld().equals(prevLocation2.getWorld())){
            particleManager.particleTrail(prevLocation2, prevLocation1, 255, 255, 135);
            particleManager.particleTrail(prevLocation2, prevLocation1, 215, 85, 255);
        }
        if(prevLocation1 != null && current != null && prevLocation1.getWorld().equals(current.getWorld())){
            particleManager.particleTrail(current, prevLocation1, 255, 255, 135);
            particleManager.particleTrail(current, prevLocation1, 215, 85, 255);
        }
    }
    
}

