package com.beangamecore.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Guidebook implements CommandExecutor{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(sender instanceof Player){
            Player player = (Player) sender;
            BeangameItem item = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:beangameguidebook"));
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item.asItem());
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item.asItem());
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Beangame Guidebook Recieved!"));
        } else {
            return false;
        }
        return true;
    }
}
