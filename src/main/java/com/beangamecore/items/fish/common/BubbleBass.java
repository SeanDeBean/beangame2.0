package com.beangamecore.items.fish.common;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BeangameSoftItem;

public class BubbleBass extends BeangameFish implements BeangameSoftItem, BGConsumableI {

    @Override
    public void onConsume(PlayerItemConsumeEvent event){
        Player player = event.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 0));
    }

    public BubbleBass() {
        setIsFishable(true);
        setWeight(110);
        setMinWaterDepth(5);
        setMinPlayerLevel(0);
        // No biome restrictions (empty set = all biomes allowed)
        // No time restrictions (default is TimeOfDay.ANY)
        // No rain requirement (default is false)
    }

    @Override
    public String getId() {
        return "bubblebass";
    }

    @Override
    public String getName() {
        return "§fBubble Bass";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Material getMaterial() {
        return Material.SALMON;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }
}

