package com.beangamecore.items.fish.legendary;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGSmeltableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.registry.BeangameItemRegistry;

public class Swordfish extends BeangameFish implements BGSmeltableI, BeangameSoftItem {

    @Override
    public void onSmelt(FurnaceSmeltEvent event) {
        event.setResult(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cookedswordfish")).asItem());
    }

    public Swordfish() {
        setIsFishable(true);
        setWeight(5);
        setMinWaterDepth(0);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "swordfish";
    }

    @Override
    public String getName() {
        return "§fSwordfish";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 107;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
    }
}

