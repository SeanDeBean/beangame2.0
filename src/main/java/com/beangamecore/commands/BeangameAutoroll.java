package com.beangamecore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BeangameAutoroll implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("bg.autoroll.toggle"))) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        BeangameStart.autoroll = !BeangameStart.autoroll;
        String message = BeangameStart.autoroll ? ChatColor.WHITE + "Autoroll enabled!" : ChatColor.WHITE + "Autoroll disabled!";
        if(sender instanceof Player p){
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
        return true;
    }
}
