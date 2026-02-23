package com.beangamecore.items.fish.uncommon;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGSmeltableI;
import com.beangamecore.items.type.BeangameSoftItem;

public class Jellyfish extends BeangameFish implements BGSmeltableI, BeangameSoftItem {

    @Override
    public void onSmelt(FurnaceSmeltEvent event) {
        event.setResult(new ItemStack(Material.SLIME_BALL, 2));
    }

    public Jellyfish() {
        setIsFishable(true);
        setWeight(70);
        setMinWaterDepth(0);
        setMinPlayerLevel(0);
        setAllowedBiomes(Set.of(Biome.OCEAN, Biome.COLD_OCEAN, Biome.DEEP_OCEAN, Biome.DEEP_COLD_OCEAN, Biome.DEEP_FROZEN_OCEAN, Biome.FROZEN_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN));
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "jellyfish";
    }

    @Override
    public String getName() {
        return "§fJellyfish";
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
        return 119;
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

