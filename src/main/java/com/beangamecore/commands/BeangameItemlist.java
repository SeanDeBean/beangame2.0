package com.beangamecore.commands;

import java.util.ArrayList;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.ItemNBT;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BeangameItemlist implements CommandExecutor{
    public static ArrayList<Inventory> menu = new ArrayList<>();
    public static boolean init = false;
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(player.hasPermission("bg.use")){
                if(!init){
                    init();
                }
                player.openInventory(menu.get(0));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            }
        }
        return false;
    }
    public void init(){
        ArrayList<BeangameItem> items = new ArrayList<>(BeangameItemRegistry.getRegistry().values());
        // elements
        ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundglassmeta = backgroundglass.getItemMeta();
        backgroundglassmeta.setHideTooltip(true);
        backgroundglassmeta.setCustomModelData(0);
        backgroundglass.setItemMeta(backgroundglassmeta);
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backmeta = back.getItemMeta();
        backmeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backmeta);
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextmeta = next.getItemMeta();
        nextmeta.setDisplayName(ChatColor.GREEN + "Next");
        next.setItemMeta(nextmeta);

        // filling inventories
        int k = 0;
        for (int i = 0; i < Math.floor(items.size() / 45) + 1; i++) {
            Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Beangame Itemlist!");
            for (int j = 0; j <= 44; j++) {
                // Ensure there are still items to process
                while (k < items.size() && BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(items.get(k).asItem())) instanceof BeangameSoftItem) {
                    k++; // Skip BeangameSoftItem
                }

                if (k < items.size()) {
                    inv.setItem(j, items.get(k).asItem());
                    k++;
                } else {
                    inv.setItem(j, backgroundglass);
                }
            }

            // Set navigation and filler items
            inv.setItem(45, back);
            inv.setItem(53, next);
            for (int j = 46; j <= 52; j++) {
                inv.setItem(j, backgroundglass);
            }

            // Customize navigation for the first and last pages
            if (i == 0) { 
                inv.setItem(45, backgroundglass); // No back button on the first page
            } else if (i == Math.floor(items.size() / 45)) { 
                inv.setItem(53, backgroundglass); // No next button on the last page
            }

            // Update the custom model data for `backgroundglass`
            backgroundglassmeta = backgroundglass.getItemMeta();
            backgroundglassmeta.setCustomModelData(i + 1);
            backgroundglass.setItemMeta(backgroundglassmeta);

            // Add the inventory to the menu list
            menu.add(i, inv);
        }
    }
}

