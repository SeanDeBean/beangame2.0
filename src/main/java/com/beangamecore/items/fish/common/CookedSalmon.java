package com.beangamecore.items.fish.common;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BeangameSoftItem;

public class CookedSalmon extends BeangameFish implements BeangameSoftItem {

    public CookedSalmon() {
        setIsFishable(false);
    }

    @Override
    public String getId() {
        return "cookedsalmon";
    }

    @Override
    public String getName() {
        return "§fCooked Salmon";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Material getMaterial() {
        return Material.COOKED_SALMON;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}


