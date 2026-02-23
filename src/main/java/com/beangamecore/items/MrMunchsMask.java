package com.beangamecore.items;


import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;

public class MrMunchsMask extends BeangameItem implements BGArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        if (Math.random() <= 0.15){
            BeangameItemRegistry.get(Key.bg("pizza")).ifPresent(pz -> {
                ItemStack pizza = pz.asItem();
                pizza.setAmount(1);
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(pizza);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), pizza);
                }
            });
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "mrmunchsmask";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public boolean isInFoodItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_PURPLE + "Mr. Munch's Mask";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Has a 15% chance every second to",
            "§2generate Pizza while in inventory.",
            "§2Pizza can be eaten or thrown at others.",
            "",
            "§6Armor",
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
        return Material.CARVED_PUMPKIN;
    }

    @Override
    public int getCustomModelData() {
        return 105;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
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

