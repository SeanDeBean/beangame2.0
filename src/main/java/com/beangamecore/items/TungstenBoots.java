package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;

public class TungstenBoots extends BeangameItem {

    @Override
    public ItemStack asItem(){
        ItemStack stack = super.asItem();
        ItemMeta meta = stack.getItemMeta();
        NamespacedKey nsk1 = new NamespacedKey(Main.getPlugin(), "beangame.tungstenboots1");
        NamespacedKey nsk2 = new NamespacedKey(Main.getPlugin(), "beangame.tungstenboots2");
        NamespacedKey nsk3 = new NamespacedKey(Main.getPlugin(), "beangame.tungstenboots3");
        meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(nsk1, 5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET));
        meta.addAttributeModifier(Attribute.GRAVITY, new AttributeModifier(nsk2, 0.1, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.FEET));
        meta.addAttributeModifier(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, new AttributeModifier(nsk3, 5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "tungstenboots";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§7Tungsten Boots";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Grants knockback immunity when worn.",
            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Feet:", "§9+" + getArmor() + " Armor", "§9+5 Knockback Resistance"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_BOOTS;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SNOUT);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 4;
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

