package com.beangamecore.items;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDReceiverInvI;

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

public class CrossNecklace extends BeangameItem implements BGDReceiverInvI {
    
    @Override
    public void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item){
        if(event.getDamage() >= 1){
            event.setDamage(event.getDamage() - 1);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "crossnecklace";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "C C", " C ", " H ", r.eCFromBeangame(Key.bg("cosmicingot")), r.eCFromBeangame(Key.bg("heartofiron")));
        return null;
    }

    @Override
    public String getName() {
        return "§6Cross Necklace";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Reduces all incoming damage by 0.5 hearts.",
            "§3Provides consistent damage reduction",
            "§3against all attacks when carried.",
            "",
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
        return Material.CHAIN;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

