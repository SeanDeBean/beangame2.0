package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class InstrumentsOfTheEnchanter extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        Location loc = player.getLocation();
        World world = loc.getWorld();
        // reduces the number of items by 1
        stack.setAmount(stack.getAmount() - 1);
        // item event
        // enchantment table
        if (player.getInventory().firstEmpty() == -1){
            world.dropItemNaturally(loc, new ItemStack(Material.ENCHANTING_TABLE, 1));
        } else {
            inventory.addItem(new ItemStack(Material.ENCHANTING_TABLE, 1));
        }
        // lapis
        if (player.getInventory().firstEmpty() == -1){
            world.dropItemNaturally(loc, new ItemStack(Material.LAPIS_LAZULI, 32));
        } else {
            inventory.addItem(new ItemStack(Material.LAPIS_LAZULI, 32));
        }
        // bookshelves
        if (player.getInventory().firstEmpty() == -1){
            world.dropItemNaturally(loc, new ItemStack(Material.BOOKSHELF, 15));
        } else {
            inventory.addItem(new ItemStack(Material.BOOKSHELF, 15));
        }
        // xp bottles
        if (player.getInventory().firstEmpty() == -1){
            world.dropItemNaturally(loc, new ItemStack(Material.EXPERIENCE_BOTTLE, 36));
        } else {
            inventory.addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 36));
        }
        return false;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "instrumentsoftheenchanter";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§dInstruments of The Enchanter";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Right-click to consume and receive",
            "§5enchanting supplies: 1 enchanting table,",
            "§532 lapis, 15 bookshelves, and 36",
            "§5experience bottles.",
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
        return Material.ENCHANTING_TABLE;
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

