package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import net.md_5.bungee.api.ChatColor;

public class BeangameGuidebook extends BeangameItem implements BGRClickableI, BeangameSoftItem {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookmeta = (BookMeta) book.getItemMeta();

        String nl = "\n";
        bookmeta.setAuthor(ChatColor.GOLD + "SeanDeBean");
        bookmeta.setTitle(ChatColor.GOLD + "Beangame Guidebook");
        bookmeta.addPage(ChatColor.GOLD + "Welcome to beangame!" + nl + nl +
                ChatColor.DARK_AQUA + "This book contains basic information regarding custom item types, combination and special beangame crafting recipes." + nl + nl +
                ChatColor.GOLD + "Hope you enjoy your time on beangame!" + nl + nl +
                "- SeanDeBean");
        bookmeta.addPage(ChatColor.GOLD + "Item Type One:" + nl + ChatColor.LIGHT_PURPLE + "Castables" + nl + nl +
                ChatColor.DARK_AQUA + "The castable item class describes items that activate on right click and have a cooldown associated with them." + nl + nl +
                ChatColor.RED + "The cooldown of castables is tracked per item type, per player, not per item!");
        bookmeta.addPage(ChatColor.GOLD + "Item Type Two:" + nl + ChatColor.GREEN + "Kits" + nl + nl +
                ChatColor.DARK_AQUA + "Kits are a class of item that when used, are consumed and grant you subitems." + nl + nl +
                ChatColor.DARK_AQUA + "These subitems are often vanillia items that are supportive to your beangame build.");
        bookmeta.addPage(ChatColor.GOLD + "Item Type Three:" + nl + ChatColor.BLACK + "Armor" + nl + nl +
                ChatColor.DARK_AQUA + "The beangame armor class describes all armor peices made by the beangame plugin." + nl + nl +
                ChatColor.DARK_AQUA + "Armor often contribute passive effects to the wearer, providing new abilities and enhancing their strength.");
        bookmeta.addPage(ChatColor.GOLD + "Item Type Four:" + nl + ChatColor.BLUE + "Talismans" + nl + nl +
                ChatColor.DARK_AQUA + "The talisman class provides the user passive effects when talismans are in users inventory." + nl + nl +
                ChatColor.GREEN + "They can be activated on hit, on a 1 tick, 1 second or 3 second interval, depending on the specific talisman.");
        bookmeta.addPage(ChatColor.GOLD + "Item Type Five:" + nl + ChatColor.RED + "On Hit" + nl + nl +
                ChatColor.DARK_AQUA + "The on hit class describes all items that are activated apon dealing damage to an enemy." + nl + nl +
                ChatColor.RED + "Note: many abillities in beangame will force on hit events to happen, activating the users on hit items.");
        bookmeta.addPage(ChatColor.DARK_AQUA + "There are plenty of items that do not fit squarley in any of these 5 categories." + nl + nl +
                ChatColor.DARK_AQUA + "But in general, every item's functionality may be traced back to one of these categories.");
        bookmeta.addPage(ChatColor.GOLD + "Special Crafting:" + nl + nl +
                ChatColor.BLACK + "Gravel" + ChatColor.GOLD + " -> " + ChatColor.BLACK + "Flint" + nl +
                ChatColor.BLACK + "Stick + Flint" + ChatColor.GOLD + " -> " + ChatColor.BLACK + "Arrow" + nl +
                ChatColor.BLACK + "8 Raw Ore + Coal" + nl + " " + ChatColor.GOLD + " -> " + ChatColor.BLACK + "6 Ingots" + nl +
                ChatColor.BLACK + "Logs of any type" + nl + " " + ChatColor.GOLD + " -> " + ChatColor.BLACK + "Armor of type");
        // credits page 
        bookmeta.addPage(ChatColor.GOLD + "beangame credits:" + nl + nl +
            ChatColor.BLACK + "AlienPepsi" + nl +
            ChatColor.BLACK + "Chloroplastics" + nl +
            ChatColor.BLACK + "Dot Jayson" + nl +
            ChatColor.BLACK + "Ember" + nl +
            ChatColor.BLACK + "Enderbrine1199" + nl +
            ChatColor.BLACK + "Erik (duki)" + nl +
            ChatColor.BLACK + "ProChef551" + nl +
            ChatColor.BLACK + "SeanDeBean" + nl +
            ChatColor.BLACK + "27 (eBidoof)" + nl +
            ChatColor.BLACK + "569k" + nl +
            ChatColor.BLACK + "And anyone who has helped along the way!" + nl 
        );


        book.setItemMeta(bookmeta);
        player.openBook(book);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public boolean isInItemRotation(){
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getId() {
        return "beangameguidebook";
    }

    @Override
    public String getName() {
        return "§6Beangame Guidebook";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.WRITABLE_BOOK;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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
        return 99;
    }
}

