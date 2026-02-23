package com.beangamecore.items.fish.legendary;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BeangameSoftItem;

public class Beanfish extends BeangameFish implements BeangameSoftItem {

    public Beanfish() {
        setIsFishable(true);
        setWeight(1);
        setMinWaterDepth(0);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "beanfish";
    }

    @Override
    public String getName() {
        return "§fBeanfish";
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
        return 115;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

