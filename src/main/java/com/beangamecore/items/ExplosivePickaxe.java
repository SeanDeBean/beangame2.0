package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGToolI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class ExplosivePickaxe extends BeangameItem implements BGToolI {
    
    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack stack) {
        Block block = event.getBlock();
        Material type = block.getType();

        if(!type.isSolid() || type.isAir()){
            return;
        }
        
        event.setCancelled(true);
            
        // Create explosion at the block's location
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            
        // Create explosion with player as the cause
        loc.getWorld().createExplosion(loc, 5, false, true, event.getPlayer());
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "explosivepickaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " T ", "TPT", " B ", r.mCFromMaterial(Material.TNT), r.mCFromMaterial(Material.IRON_PICKAXE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§4Explosive Pickaxe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Breaking natural blocks creates",
            "§5a 5 block radius explosion that",
            "§5instantly clears large areas.",
            "§5Useful for rapid mining and",
            "§5cave clearing operations.",
            "",
            "§5Tool",
            "§dOn Hit Applier",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_PICKAXE;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

