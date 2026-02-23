package com.beangamecore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class GrantRefund implements CommandExecutor{
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bg.use") && args.length == 1){
                if(args.length != 1){
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cInvalid format! /grantrefund <playerName>"));
                }
                String target = args[0];
                for(Player players : Bukkit.getOnlinePlayers()){
                    if(players.getName().toLowerCase().equals(target.toLowerCase())){
                        BeangameDistribute.bgdistributePlayers.put(players.getUniqueId(), true);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Refund granted to " + target + "!"));
                        players.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6You have been granted a refund!"));
                        return false;
                    }
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cInvalid target for refund!"));
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            }
        }
        return false;
    }
}

