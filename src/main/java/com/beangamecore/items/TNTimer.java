package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;

import java.util.List;
import java.util.Map;

import com.beangamecore.items.type.BGPlaceableI;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class TNTimer extends BeangameItem implements BGPlaceableI {
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event, ItemStack item) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation().add(0.5, 0, 0.5);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            loc.getWorld().spawn(loc, TNTPrimed.class, tnt -> {
                tnt.setFuseTicks(20 * 30);
                tnt.setSource(player);
            });
        }, 1L);
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "tntimer";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "TTT", "TST", "TTT", r.mCFromMaterial(Material.TNT), r.eCFromBeangame(Key.bg("stopwatch")));
        return null;
    }

    @Override
    public String getName() {
        return "§cTNTimer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Place to spawn lit TNT with",
            "§5a 30 second fuse timer.",
            "",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.TNT;
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

