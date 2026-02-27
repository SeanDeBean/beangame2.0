package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.BlockCategories;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Protato extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // cooldown system
        if (onCooldown(uuid)){
            return false;
        }
        applyCooldown(uuid);
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock != null && clickedBlock.getType() != Material.AIR){
            Material type = clickedBlock.getType();
            for (Material functionalblocks : BlockCategories.getFunctionalBlocks()){
                if(type == functionalblocks){
                    return false;
                }
            }
            clickedBlock.setType(Material.AIR);
            World world = clickedBlock.getWorld();
            Location loc = clickedBlock.getLocation();
            world.dropItemNaturally(loc, new ItemStack(Material.BAKED_POTATO, 1));
            world.playSound(loc, Sound.BLOCK_CROP_BREAK, 0.7F, 1.0F);
        }
        return true;
    }
    
    @Override
    public long getBaseCooldown() {
        return 250L;
    }

    @Override
    public String getId() {
        return "protato";
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
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " NP", "NFB", "BB ", r.mCFromMaterial(Material.POTATO), r.mCFromMaterial(Material.POISONOUS_POTATO), r.eCFromBeangame(Key.bg("feast")), r.mCFromMaterial(Material.BAKED_POTATO));
        return null;
    }

    @Override
    public String getName() {
        return "§6Protato";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Right-click blocks to convert them",
            "§2to baked potatoes. Destroys the",
            "§2block and drops a baked potato.",
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
        return Material.POTATO;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

