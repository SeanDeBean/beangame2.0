package com.beangamecore.commands;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.beangamecore.BeangameModes;
import com.beangamecore.Main;

import java.util.List;

public class GamemodesCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("bg.use")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            return true;
        }
        
        // Open GUI if no args
        if (args.length == 0) {
            Main.getPlugin().getBeangameModes().openInventory(player);
            return true;
        }
        
        // Handle subcommands
        if (args.length == 1) {
            String subcommand = args[0].toLowerCase();
            
            switch (subcommand) {
                case "list" -> {
                    BeangameModes modes = Main.getPlugin().getBeangameModes();
                    player.sendMessage(ChatColor.GOLD + "=== Gamemodes ===");
                    for (BeangameModes.GameMode gm : modes.getAllGameModes()) {
                        String status = gm.isEnabled() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
                        player.sendMessage(gm.getDisplayName() + ChatColor.GRAY + " - " + status);
                    }
                    return true;
                }
                case "enabled" -> {
                    BeangameModes modes = Main.getPlugin().getBeangameModes();
                    List<String> enabled = modes.getEnabledGameModes();
                    if (enabled.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + "No gamemodes are currently enabled.");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Enabled: " + String.join(", ", enabled));
                    }
                    return true;
                }
                default -> {
                    // Try to toggle by name
                    BeangameModes modes = Main.getPlugin().getBeangameModes();
                    boolean newState = modes.toggle(subcommand);
                    String status = newState ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled";
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        TextComponent.fromLegacy("§6Gamemode §e" + subcommand + " §6is now " + status));
                    return true;
                }
            }
        }
        
        // Toggle specific gamemode on/off
        if (args.length == 2) {
            String gamemode = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            
            BeangameModes modes = Main.getPlugin().getBeangameModes();
            
            switch (action) {
                case "on", "enable", "true" -> {
                    modes.setEnabled(gamemode, true);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        TextComponent.fromLegacy("§aEnabled §e" + gamemode));
                    return true;
                }
                case "off", "disable", "false" -> {
                    modes.setEnabled(gamemode, false);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        TextComponent.fromLegacy("§cDisabled §e" + gamemode));
                    return true;
                }
                default -> {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        TextComponent.fromLegacy("§cInvalid action! Use: on/off/enable/disable"));
                    return true;
                }
            }
        }
        
        // Handle disabled items management
        if (args.length >= 3 && args[0].equalsIgnoreCase("disableitem")) {
            String gamemode = args[1].toLowerCase();
            String itemKey = args[2];
            
            BeangameModes modes = Main.getPlugin().getBeangameModes();
            modes.addDisabledItem(gamemode, itemKey);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§cDisabled §e" + itemKey + " §cin §e" + gamemode));
            return true;
        }
        
        if (args.length >= 3 && args[0].equalsIgnoreCase("enableitem")) {
            String gamemode = args[1].toLowerCase();
            String itemKey = args[2];
            
            BeangameModes modes = Main.getPlugin().getBeangameModes();
            modes.removeDisabledItem(gamemode, itemKey);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§aEnabled §e" + itemKey + " §ain §e" + gamemode));
            return true;
        }
        
        // Invalid format
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
            TextComponent.fromLegacy("§cInvalid format! /gamemodes [mode] [on/off] or /gamemodes disableitem <mode> <item>"));
        return true;
    }
}