package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGToolI;
import com.beangamecore.util.BlockCategories;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class AppleCutters extends BeangameItem implements BGToolI {
    
    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        Block block = event.getBlock();
        Material type = block.getType();

        // Use a Set for BlockCategories.leaves for faster lookup if not already a Set
        if (BlockCategories.leaves.contains(type)) {
            event.setDropItems(false);
            if (ThreadLocalRandom.current().nextDouble() <= 0.4) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.APPLE));
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "applecutters";
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
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " AA", " SA", "B A", r.mCFromMaterial(Material.APPLE), r.mCFromMaterial(Material.SHEARS), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§2Apple Cutters";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Breaking leaves has a 40% chance",
            "§2to drop apples.",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.SHEARS;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
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

