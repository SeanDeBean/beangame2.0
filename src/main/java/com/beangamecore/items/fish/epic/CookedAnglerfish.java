package com.beangamecore.items.fish.epic;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BeangameSoftItem;

public class CookedAnglerfish extends BeangameFish implements BeangameSoftItem {

    public CookedAnglerfish() {
        setIsFishable(false);
    }

    @Override
    public String getId() {
        return "cookedanglerfish";
    }

    @Override
    public String getName() {
        return "§fCooked Anglerfish";
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
        return 108;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

