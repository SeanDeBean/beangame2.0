package com.beangamecore.items.fish.epic;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BeangameSoftItem;

public class Mimefish extends BeangameFish implements BeangameSoftItem {

    public Mimefish() {
        setIsFishable(true);
        setWeight(130);
        setMinWaterDepth(0);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "mimefish";
    }

    @Override
    public String getName() {
        return "§fMimefish";
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
        return 111;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

