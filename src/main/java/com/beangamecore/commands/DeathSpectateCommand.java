package com.beangamecore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class DeathSpectateCommand implements CommandExecutor {
    public static boolean deathspectate = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bg.use")) {
                toggleSpectate();
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "You don't have permission to use this command!"));
            }
            return true;
        }
        sender.sendMessage("Only players can use this command.");
        return true;
    }

    private void toggleSpectate() {
        deathspectate = !deathspectate;
        String message = deathspectate ? ChatColor.WHITE + "Spectating enabled!" : ChatColor.WHITE + "Spectating disabled!";
        for (Player warnMessage : Bukkit.getOnlinePlayers()) {
            warnMessage.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }
}

