package com.beangamecore.items.armorsets.acacia;

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

public class AcaciaLeggings extends BeangameItem implements BeangameSoftItem {
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "acacialeggings";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, "WWW", "W W", "W W", r.mCFromMaterial(Material.ACACIA_LOG));
    }

    @Override
    public String getName() {
        return "§fAcacia Leggings";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame", "§9", "§7When on Legs:", "§9+" + getArmor() + " Armor");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_LEGGINGS;
    }

    @Override
    public int getCustomModelData() {
        return 205;
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
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.LEGS;
    }

    @Override
    public EquippableComponent getEquipmentData() {
        return EquipmentBuilder.create()
                .withModel(NamespacedKey.minecraft("acacia"))
                .withSlot(EquipmentSlot.LEGS)
                .withDamageOnHurt(true)
                .withSwappable(true)
                .withEquipSound(Sound.BLOCK_WOOD_BREAK)
                .build();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
