package com.beangamecore.items.armorsets.darkoak;

import java.util.List;
import java.util.Map;

import com.beangamecore.builders.EquipmentBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.recipes.RecipeAPI;

import de.tr7zw.nbtapi.NBT;

public class DarkOakChestplate extends BeangameItem implements BeangameSoftItem {
   
    public ItemStack asItem(){
        ItemStack stack = new ItemStack(getMaterial(), getCraftingAmount());
        Main.getConfiguration().applyMeta(stack, getKey());
        NBT.modify(stack, (nbt) -> {
            nbt.setString("beangame.itemkey", getKey().toString());
        });
        ItemMeta meta = stack.getItemMeta();
        NamespacedKey nsk = new NamespacedKey(Main.getPlugin(), "beangame.darkoakchestplate");
        meta.addAttributeModifier(Attribute.MAX_HEALTH, new AttributeModifier(nsk, 2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "darkoakchestplate";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, "W W", "WWW", "WWW", r.mCFromMaterial(Material.DARK_OAK_LOG));
    }

    @Override
    public String getName() {
        return "§fDark Oak Chestplate";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame", "§9", "§7When on Body:", "§9+" + getArmor() + " Armor", "§9+2 Max Health");
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
        return 206;
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
        return EquipmentSlotGroup.CHEST;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public EquippableComponent getEquipmentData() {
        return EquipmentBuilder.create()
                .withModel(NamespacedKey.minecraft("dark_oak"))
                .withSlot(EquipmentSlot.CHEST)
                .withDamageOnHurt(true)
                .withSwappable(true)
                .withEquipSound(Sound.BLOCK_WOOD_BREAK)
                .build();
    }
}
