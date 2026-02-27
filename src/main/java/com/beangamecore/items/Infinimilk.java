package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.util.Cooldowns;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.util.PotionCategories;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Infinimilk extends BeangameItem implements BGConsumableI {

    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        PotionCategories.getHarmfulPotions().forEach(player::removePotionEffect);

        // Set fire ticks to 0
        player.setFireTicks(0);

        UUID uuid = player.getUniqueId();
        for (String harmfulCustomPotions : PotionCategories.getHarmfulCustomPotions()) {
            Cooldowns.setCooldown(harmfulCustomPotions, uuid, 0);
        }

        // Optionally cancel event after handling
        event.setCancelled(true);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "infinimilk";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " F ", " B ", "   ", r.mCFromMaterial(Material.FERMENTED_SPIDER_EYE), r.eCFromBeangame(Key.bg("bleach")));
        return null;
    }

    @Override
    public String getName() {
        return "§fInfinimilk";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Consume to clear all harmful potion",
            "§2effects, extinguish fire, and remove",
            "§2all negative status cooldowns.",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.MILK_BUCKET;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

