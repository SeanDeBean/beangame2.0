package com.beangamecore.commands;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand implements CommandExecutor {

    // Method to give an item to the player
    private void giveItemToPlayer(Player recipient, BeangameItem item) {
        if (recipient.getInventory().firstEmpty() != -1) {
            recipient.getInventory().addItem(item.asItem());
        } else {
            recipient.getWorld().dropItemNaturally(recipient.getLocation(), item.asItem());
        }
        recipient.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Beangame Item Received!"));
    }

    // Method to notify all operators about item giving action
    private void notifyOperators(String message) {
        for (OfflinePlayer p : Bukkit.getOperators()) {
            if (p.isOnline()) {
                p.getPlayer().sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + message);
            }
        }
    }

    // Main command execution method
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use that command!");
            return true;
        }

        if (!player.hasPermission("bg.use")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cInvalid format! /bg <itemID> [target]"));
            return true;
        }

        // Determine if the argument is a NamespacedKey or a simple string
        boolean isKey = args[0].contains(":");
        BeangameItem item = null;

        if (isKey) {
            // Handle NamespacedKey creation properly
            String[] parts = args[0].split(":");
            if (parts.length == 2) {
                // Ensure both parts are valid
                String namespace = parts[0].toLowerCase().trim();
                String key = parts[1].toLowerCase().trim();
        
                if (namespace.isEmpty() || key.isEmpty()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cInvalid itemID format!"));
                    return true;
                }
        
                // Create the NamespacedKey object
                NamespacedKey keyObj = new NamespacedKey(namespace, key);
                
                // Get the item using the NamespacedKey
                item = BeangameItemRegistry.getRaw(keyObj);
                
                // Check if the item was found
                if (item == null) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cNo item found with the ID " + keyObj.toString()));
                    return true;
                }
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cInvalid itemID format!"));
                return true;
            }
        } else {
            // For simple string item IDs (no namespace), we assume it's a full item name
            item = BeangameItemRegistry.getRaw(args[0]);
            
            // Check if the item exists in the registry
            if (item == null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cNo item found with the ID " + args[0]));
                return true;
            }
        }

        // Create a message for notifying operators
        String itemMessage = "[" + player.getName() + ": Gave " + item.getNamespace() + ":" + item.getId() + "]";

        // Handle giving the item to the player
        if (args.length == 1) {
            notifyOperators(itemMessage + " to " + player.getName());
            giveItemToPlayer(player, item);
            return true;
        }

        // Handle giving the item to all players
        String targetName = args[1].toLowerCase();
        if (targetName.equals("@a") || targetName.equals("all")) {
            notifyOperators(itemMessage + " to all players");
            for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
                if (onlinePlayer.getGameMode() != GameMode.SPECTATOR) {
                    giveItemToPlayer(onlinePlayer, item);
                }
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Beangame Item Received By All Players!"));
            return true;
        }

        // Handle giving the item to a specific target player
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer != null) {
            notifyOperators(itemMessage + " to " + targetPlayer.getName());
            giveItemToPlayer(targetPlayer, item);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§6Beangame Item Granted!"));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cInvalid target!"));
        }

        return true;
    }
}
