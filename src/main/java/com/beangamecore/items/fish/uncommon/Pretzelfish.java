package com.beangamecore.items.fish.uncommon;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BeangameSoftItem;

public class Pretzelfish extends BeangameFish implements BeangameSoftItem {

    public Pretzelfish() {
        setIsFishable(false);
    }

    @Override
    public String getId() {
        return "pretzelfish";
    }

    @Override
    public String getName() {
        return "§fPretzelfish";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Material getMaterial() {
        return Material.COOKED_COD;
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


