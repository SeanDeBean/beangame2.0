package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGToolI;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class LagConjurer extends BeangameItem implements BGToolI {
    
    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack stack) {
        Block block = event.getBlock();
        Material type = block.getType();

        // Use HashSet for faster lookup if BlockCategories.ores is a list
        if (type.toString().toLowerCase().contains("_ore") && Math.random() < 0.30D) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                public void run(){
                    block.setType(type);
                    Location min = block.getLocation();
                    Location max = min.clone().add(1.0, 1.0, 1.0);
                    Main.getPlugin().getParticleManager().particleCube(min, max, 30, 144, 255);
                    block.getWorld().playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.0F);
                }     
            }, 1L);
    
        }
    }

    

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "lagconjurer";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CRH", " BP", "O A", r.eCFromBeangame(Key.bg("cheesetouch")), r.mCFromMaterial(Material.REDSTONE), r.mCFromMaterial(Material.HOPPER), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.DECORATED_POT), r.mCFromMaterial(Material.BAMBOO), r.mCFromMaterial(Material.ARMOR_STAND));
        return null;
    }

    @Override
    public String getName() {
        return "§5Lag Conjurer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Has a 30% chance to instantly",
            "§5regenerate ore blocks after mining.",
            "",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:efficiency", 3, "minecraft:fortune", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_PICKAXE;
    }

    @Override
    public int getCustomModelData() {
        return 104;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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

