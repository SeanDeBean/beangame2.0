package com.beangamecore.blocks;

import com.beangamecore.blocks.type.BGBreakableB;
import com.beangamecore.blocks.type.BGPlaceableB;
import com.beangamecore.items.generic.BeangameItem;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;

import com.beangamecore.Main;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Infinicake extends BeangameItem implements BGPlaceableB, BGBreakableB {

    @Override
    public void onBlockPlace(BlockPlaceEvent event, ItemStack stack){
        event.setCancelled(true);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> event.getBlock().setType(Material.CAKE, true), 1L);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {

    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "infinicake";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public boolean isInFoodItemRotation(){
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "MMM", "SAS", "WWW", r.eCFromBeangame(Key.bg("infinimilk")), r.eCFromBeangame(Key.bg("speedenrichment")), r.eCFromBeangame(Key.bg("emotionalsupportanimal")), r.eCFromBeangame(Key.bg("wheatwizardswand")));
        return null;
    }

    @Override
    public String getName() {
        return "§5Infinicake";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2A magical cake that can be placed",
            "§2and eaten repeatedly without being",
            "§2consumed. Provides infinite food source.",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.CAKE;
    }

    @Override
    public void onDestroy(Block block) {

    }

    @Override
    public boolean shouldDropOnDestroy(Block block) {
        return false;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
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

