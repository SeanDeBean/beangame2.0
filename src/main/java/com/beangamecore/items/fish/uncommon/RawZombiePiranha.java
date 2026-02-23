package com.beangamecore.items.fish.uncommon;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemFlag;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGSmeltableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.registry.BeangameItemRegistry;

public class RawZombiePiranha extends BeangameFish implements BGSmeltableI, BeangameSoftItem {

    @Override
    public void onSmelt(FurnaceSmeltEvent event) {
        event.setResult(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cookedzombiepiranha")).asItem());
    }

    public RawZombiePiranha() {
        setIsFishable(true);
        setWeight(60);
        setMinWaterDepth(0);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        setPreferredTime(TimeOfDay.NIGHT);
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "rawzombiepiranha";
    }

    @Override
    public String getName() {
        return "§fRaw Zombie Piranha";
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
        return 106;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

