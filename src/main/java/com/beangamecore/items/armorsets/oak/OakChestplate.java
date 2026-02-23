package com.beangamecore.items.armorsets.oak;

import java.util.List;
import java.util.Map;

import com.beangamecore.builders.EquipmentBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.recipes.RecipeAPI;

public class OakChestplate extends BeangameItem implements BeangameSoftItem {
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "oakchestplate";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, "W W", "WWW", "WWW", r.mCFromMaterial(Material.OAK_LOG));
    }

    @Override
    public String getName() {
        return "§fOak Chestplate";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame", "§9", "§7When on Body:", "§9+" + getArmor() + " Armor");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public int getCustomModelData() {
        return 201;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ATTRIBUTES);
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
        return 3;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.CHEST;
    }

    @Override
    public EquippableComponent getEquipmentData() {
        return EquipmentBuilder.create()
                .withModel(NamespacedKey.minecraft("oak"))
                .withSlot(EquipmentSlot.CHEST)
                .withDamageOnHurt(true)
                .withSwappable(true)
                .withEquipSound(Sound.BLOCK_WOOD_BREAK)
                .build();
    }
}

