package com.beangamecore.items.fish.rare;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGSmeltableI;
import com.beangamecore.items.type.BeangameSoftItem;

public class PhantomFish extends BeangameFish implements BGSmeltableI, BeangameSoftItem {

    @Override
    public void onSmelt(FurnaceSmeltEvent event) {
        event.setResult(new ItemStack(Material.PHANTOM_MEMBRANE, 2));
    }

    public PhantomFish() {
        setIsFishable(true);
        setWeight(30);
        setMinWaterDepth(10);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        setPreferredTime(TimeOfDay.NIGHT);
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "phantomfish";
    }

    @Override
    public String getName() {
        return "§fPhantom Fish";
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
        return 114;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

