package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;

import java.util.List;
import java.util.Map;

import de.tr7zw.nbtapi.NBT;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class CarrotSlides extends BeangameItem implements BGArmorI {
    
    public ItemStack asItem(){
        ItemStack stack = new ItemStack(getMaterial(), getCraftingAmount());
        Main.getConfiguration().applyMeta(stack, getKey());
        NBT.modify(stack, (nbt) -> {
            nbt.setString("beangame.itemkey", getKey().toString());
        });
        ItemMeta meta = stack.getItemMeta();
        NamespacedKey nsk = new NamespacedKey(Main.getPlugin(), "beangame.carrotslides");
        meta.addAttributeModifier(Attribute.MOVEMENT_SPEED, new AttributeModifier(nsk, 0.012, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.FEET));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        player.setFoodLevel(20);
        player.setSaturation(Math.max(player.getSaturation(), 5));
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "carrotslides";
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
        // return r.bgShapedRecipe(this, "   ", "F F", "C C", r.eCFromBeangame(Key.bg("feast")), r.mCFromMaterial(Material.CARROT));
        return null;
    }

    @Override
    public String getName() {
        return "§6Carrot Slides";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Keeps you fully fed and saturated",
            "§6while worn. Provides a small speed",
            "§6boost and protection when equipped.",
            "",
            "§2Food",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Feet:", "§9+1 Armor", "§9+0.012 Speed"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_BOOTS;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DYE);
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(240, 125, 35);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.EMERALD, TrimPattern.SPIRE);
    }

    @Override
    public int getArmor(){
        return 1;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.FEET;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }


}

