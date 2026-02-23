package com.beangamecore.items.fish.common;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BeangameSoftItem;

public class CookedNoodlefish extends BeangameFish implements BeangameSoftItem {

    public CookedNoodlefish() {
        setIsFishable(false);
    }

    @Override
    public String getId() {
        return "cookednoodlefish";
    }

    @Override
    public String getName() {
        return "§fCooked Noodlefish";
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
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

