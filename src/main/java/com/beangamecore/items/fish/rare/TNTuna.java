package com.beangamecore.items.fish.rare;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BeangameSoftItem;

public class TNTuna extends BeangameFish implements BeangameSoftItem, BGConsumableI {

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        player.getLocation().getWorld().spawn(player.getLocation(), TNTPrimed.class, tnt -> {
            tnt.setFuseTicks(20 * 5);
            tnt.setSource(player);
        });
    }

    public TNTuna() {
        setIsFishable(true);
        setWeight(40);
        setMinWaterDepth(7);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "tntuna";
    }

    @Override
    public String getName() {
        return "§fTNTuna";
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
        return 109;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}
