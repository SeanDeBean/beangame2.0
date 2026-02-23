package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGLPTalismanI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class PortableCarrotFarm extends BeangameItem implements BGLPTalismanI {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        if (Math.random() <= 0.24){
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(new ItemStack(Material.CARROT, 1));
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.CARROT, 1));
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "portablecarrotfarm";
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
        return null;
    }

    @Override
    public String getName() {
        return "§6Portable Carrot Farm";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Has a 24% chance every second to",
            "§3generate a carrot while carried.",
            "§3Provides a steady supply of food",
            "§3without needing farmland.",
            "§3Automatically adds to inventory.",
            "",
            "§3Talisman",
            "§5Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.COMPOSTER;
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

