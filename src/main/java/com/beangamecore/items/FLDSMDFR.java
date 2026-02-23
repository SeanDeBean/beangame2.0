package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.items.type.talisman.BGLPTalismanI;

public class FLDSMDFR extends BeangameItem implements BGLPTalismanI, BGInvUnstackable {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        Material type = player.getLocation().getBlock().getType();
        Material type2 = player.getEyeLocation().getBlock().getType();
        World world = player.getWorld();
        Boolean raining = world.isThundering();
        if (isInWaterOrRaining(type, type2, raining)) {
            player.setFoodLevel(20);
            player.setSaturation(Math.max(player.getSaturation(), 5));
        }
    }

    private boolean isInWaterOrRaining(Material type, Material type2, Boolean raining) {
        return type.equals(Material.WATER) ||
                type.equals(Material.WATER_CAULDRON) ||
                type2.equals(Material.WATER) ||
                type2.equals(Material.WATER_CAULDRON) ||
                raining;
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
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "fldsmdfr";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§bFLDSMDFR";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Automatically restores hunger and",
            "§2saturation when in water or during rain.",
            "",
            "§2Food",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.HOPPER;
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

