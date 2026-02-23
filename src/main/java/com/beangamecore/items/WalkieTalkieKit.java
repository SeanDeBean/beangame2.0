package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.registry.BeangameItemRegistry;

import de.tr7zw.nbtapi.NBT;

public class WalkieTalkieKit extends BeangameItem implements BeangameSoftItem {

    @Override
    public ItemStack asItem(){
        ItemStack stack = new ItemStack(getMaterial(), 1);
        BundleMeta meta = (BundleMeta) stack.getItemMeta();
        ItemStack walkietalkie = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:walkietalkie")).asItem();
        walkietalkie.setAmount(4);   
        meta.addItem(walkietalkie);
        stack.setItemMeta(meta);
        Main.getConfiguration().applyMeta(stack, getKey());
        NBT.modify(stack, (nbt) -> {
            nbt.setString("beangame.itemkey", getKey().toString());
        });
        return stack;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "walkietalkiekit";
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
        return "§bWalkie Talkie Kit";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aContains 4 Walkie Talkies for",
            "§along-range voice communication.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.BUNDLE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

