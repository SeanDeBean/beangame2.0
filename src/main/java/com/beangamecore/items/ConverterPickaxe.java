package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class ConverterPickaxe extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Block clickedBlock = event.getClickedBlock();
        
        if (clickedBlock != null && clickedBlock.getType() != Material.AIR) {
            Material type = clickedBlock.getType();
            // Create a map for ore transformations
            Map<Material, Material> oreMap = new HashMap<>();
            
            oreMap.put(Material.COAL_ORE, Material.IRON_ORE);
            oreMap.put(Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE);
            oreMap.put(Material.IRON_ORE, Material.COAL_ORE);
            oreMap.put(Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COAL_ORE);
            oreMap.put(Material.COPPER_ORE, Material.GOLD_ORE);
            oreMap.put(Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE);
            oreMap.put(Material.GOLD_ORE, Material.COPPER_ORE);
            oreMap.put(Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_COPPER_ORE);
            oreMap.put(Material.REDSTONE_ORE, Material.LAPIS_ORE);
            oreMap.put(Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_LAPIS_ORE);
            oreMap.put(Material.LAPIS_ORE, Material.REDSTONE_ORE);
            oreMap.put(Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_REDSTONE_ORE);
            oreMap.put(Material.DIAMOND_ORE, Material.EMERALD_ORE);
            oreMap.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_EMERALD_ORE);
            oreMap.put(Material.EMERALD_ORE, Material.DIAMOND_ORE);
            oreMap.put(Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_DIAMOND_ORE);
            oreMap.put(Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE);
            oreMap.put(Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE);

            // Check if the block is in the map and transform it
            if (oreMap.containsKey(type)) {
                Material newType = oreMap.get(type);
                // Sound effect
                Player player = event.getPlayer();
                World world = player.getWorld();
                world.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.75F, 1.0F);

                // Schedule block transformation
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    clickedBlock.setType(newType);
                }, 1L);
                return true;
            }
        }
        return false;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "converterpickaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CEI", "RPL", "GBD", r.mCFromMaterial(Material.COAL_ORE), r.mCFromMaterial(Material.EMERALD_ORE), r.mCFromMaterial(Material.IRON_ORE), r.mCFromMaterial(Material.REDSTONE_ORE), r.mCFromMaterial(Material.DIAMOND_PICKAXE), r.mCFromMaterial(Material.LAPIS_ORE), r.mCFromMaterial(Material.NETHER_GOLD_ORE), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.DEEPSLATE_DIAMOND_ORE));
        return null;
    }

    @Override
    public String getName() {
        return "§cConverter Pickaxe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Right-click ores to transform them",
            "§5into their paired counterparts:",
            "§5Coal↔Iron, Copper↔Gold,",
            "§5Redstone↔Lapis, Diamond↔Emerald,",
            "§5Nether Gold↔Quartz.",
            "",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:efficiency", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_PICKAXE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

