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

public class Infinicobble extends BeangameItem implements BGPlaceableB, BGBreakableB {
    @Override
    public void onBlockPlace(BlockPlaceEvent event, ItemStack stack){
        event.setCancelled(true);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> event.getBlock().setType(Material.COBBLESTONE), 1L);
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
        return "infinicobble";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CSC", "SBS", "CSC", r.mCFromMaterial(Material.COBBLESTONE), r.mCFromMaterial(Material.STONE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§7Infinicobble";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5A magical cobblestone block that",
            "§5can be placed infinitely without",
            "§5being consumed from your inventory.",
            "",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.COBBLESTONE;
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
        return 0;
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

