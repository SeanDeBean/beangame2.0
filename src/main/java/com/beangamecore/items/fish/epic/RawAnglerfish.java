package com.beangamecore.items.fish.epic;

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

public class RawAnglerfish extends BeangameFish implements BGSmeltableI, BeangameSoftItem {

    @Override
    public void onSmelt(FurnaceSmeltEvent event) {
        event.setResult(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cookedanglerfish")).asItem());
    }

    public RawAnglerfish() {
        setIsFishable(true);
        setWeight(90);
        setMinWaterDepth(0);
        setMinPlayerLevel(0);
        setAllowedBiomes(Set.of(Biome.OCEAN, Biome.COLD_OCEAN, Biome.DEEP_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN, Biome.FROZEN_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN));
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "rawanglerfish";
    }

    @Override
    public String getName() {
        return "§fRaw Anglerfish";
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
        return 113;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

