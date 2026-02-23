package com.beangamecore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SwapInventories implements CommandExecutor{

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bg.use")){
                if(args.length != 2){
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cInvalid format! /swapinventories <playerName> <playerName>"));
                }
                String player1 = args[0];
                String player2 = args[1];
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Inventories Swapped!"));
                swapInventories(player1, player2);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            }
        }
        return false;
    }
    public static void swapInventories(String p1, String p2){
        boolean check1 = false;
        boolean check2 = false;
        Player player1 = null;
        Player player2 = null;
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
            if(onlinePlayer.getName().toLowerCase().equals(p1.toLowerCase())){
                check1 = true;
                player1 = onlinePlayer;
                break;
            }
        }
        if(!check1){
            return;
        }
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
            if(onlinePlayer.getName().toLowerCase().equals(p2.toLowerCase())){
                check2 = true;
                player2 = onlinePlayer;
                break;
            }
        }
        if(!check2){
            return;
        }
        // inventory
        ItemStack[] invcontents1 = player1.getInventory().getContents();
        player1.getInventory().setContents(player2.getInventory().getContents());
        player2.getInventory().setContents(invcontents1);
        player1.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Inventory swapped with " + p2 + "'s!"));
        player2.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Inventory swapped with " + p1 + "'s!"));
    }
}

