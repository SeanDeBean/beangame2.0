package com.beangamecore.items.fish.rare;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;

public class Creeperfish extends BeangameFish implements BeangameSoftItem, BGConsumableI, BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            return false;
        }
        applyCooldown(uuid);
        World world = player.getWorld();
        Location loc = player.getLocation();
        world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 1F, 0);
        world.spawnParticle(Particle.FLAME, loc, 3);
        return true;
    }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        player.getWorld().createExplosion(player.getLocation(), 2.5F, false, true, player);
    }

    public Creeperfish() {
        setIsFishable(true);
        setWeight(50);
        setMinWaterDepth(10);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        setPreferredTime(TimeOfDay.NIGHT);
        // No rain requirement (default is false)
    }

    @Override
    public long getBaseCooldown() {
        return 200L;
    }

    @Override
    public String getId() {
        return "creeperfish";
    }

    @Override
    public String getName() {
        return "§fCreeperfish";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Material getMaterial() {
        return Material.COD;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

