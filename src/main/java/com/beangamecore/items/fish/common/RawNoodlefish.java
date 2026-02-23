package com.beangamecore.items.fish.common;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGSmeltableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.registry.BeangameItemRegistry;

public class RawNoodlefish extends BeangameFish implements BGSmeltableI, BeangameSoftItem {

    @Override
    public void onSmelt(FurnaceSmeltEvent event) {
        event.setResult(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cookednoodlefish")).asItem());
    }

    public RawNoodlefish() {
        setIsFishable(true);
        setWeight(120);
        setMinWaterDepth(0);
        setMinPlayerLevel(0);
        setAllowedBiomes(Set.of(Biome.RIVER, Biome.FROZEN_RIVER));
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "rawnoodlefish";
    }

    @Override
    public String getName() {
        return "§fRaw Noodlefish";
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
        return 104;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

