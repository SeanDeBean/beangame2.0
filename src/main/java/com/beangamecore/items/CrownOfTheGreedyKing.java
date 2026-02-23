package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerArmorI;
import com.beangamecore.items.type.damage.entity.BGDReceiverArmorI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class CrownOfTheGreedyKing extends BeangameItem implements BGDReceiverArmorI, BGDDealerArmorI {
    
    @Override
    public void victimOnHitArmor(EntityDamageByEntityEvent event, ItemStack armor) {
        event.setDamage(event.getDamage() + 2);
    }

    @Override
    public void attackerOnHitArmor(EntityDamageByEntityEvent event, ItemStack armor) {
        event.setDamage(event.getDamage() + 2);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "crownofthegreedyking";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "G G", "BEB", "   ", r.mCFromMaterial(Material.GOLD_INGOT), r.mCFromMaterial(Material.GOLD_BLOCK), r.eCFromBeangame(Key.bg("berserkersessence")));
        return null;
    }

    @Override
    public String getName() {
        return "§6Crown of the Greedy King";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Increases all damage dealt and taken",
            "§6by 1 heart. Creates a high-risk combat",
            "§6style with amplified offensive and",
            "§6defensive exchanges.",
            "",
            "§6Armor",
            "§cOn Hit",
            "§9§obeangame",
            "§9", "§7When on Head:", "§9+2 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_HELMET;
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
        return new ArmorTrim(TrimMaterial.GOLD, TrimPattern.WAYFINDER);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 2;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.HEAD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

