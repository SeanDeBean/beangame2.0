package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import com.beangamecore.Main;
import com.beangamecore.items.type.damage.BGCancelFallDmgArmorI;
import com.beangamecore.recipes.RecipeAPI;
import com.beangamecore.util.Key;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;

public class ParachutePants extends BeangameItem implements BGArmorI, BGCancelFallDmgArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false));
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "parachutepants";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, " H ", "FlF", "F F", r.eCFromBeangame(Key.bg("luckyhorseshoe")), r.mCFromMaterial(Material.FEATHER), r.mCFromMaterial(Material.IRON_LEGGINGS));
    }

    @Override
    public String getName() {
        return "§fParachute Pants";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Grants constant Slow Falling effect",
            "§6while worn. Provides complete fall",
            "§6damage immunity and controlled",
            "§6descent.",
            "",
            "§6Armor",
            "§fMovement",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 3, "minecraft:feather_falling", 5);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_LEGGINGS;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.IRON, TrimPattern.TIDE);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 5;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.LEGS;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

