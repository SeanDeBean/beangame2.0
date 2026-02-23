package com.beangamecore.commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.beangamecore.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Refund implements CommandExecutor{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            if(BeangameDistribute.bgdistributePlayers.containsKey(uuid)){
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
                player.sendTitle(null, "§3Items arriving soon!", 20, 100, 20);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"title " + player.getName() + " title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    if(player.getGameMode() == GameMode.SURVIVAL){
                        // creating inventory
                        Inventory bginv = Bukkit.createInventory(null, 27, String.valueOf(ChatColor.GOLD) + "Beangame!");
                        ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                        ItemMeta meta = backgroundglass.getItemMeta();
                        meta.setDisplayName(ChatColor.BLACK + " ");
                        backgroundglass.setItemMeta(meta);
                        for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26 }){
                            bginv.setItem(i, backgroundglass);
                        }
                        ArrayList<ItemStack> array = BeangameDistribute.bgdistributeSave.get(uuid);
                        int j = 0;
                        for (int i : new int[] { 10, 11, 12, 13, 14, 15, 16 }){
                            bginv.setItem(i, array.get(j));
                            j++;
                        }
                        // opening inventory
                        player.openInventory(bginv);
                    } else {
                        BeangameDistribute.bgdistributePlayers.remove(uuid);
                    }
                }, 22L);
                BeangameDistribute.bgdistributePlayers.remove(uuid);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou do not qualify for a refund!"));
            }
        } else {
            return false;
        }
        return true;
    }
}
