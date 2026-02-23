package com.beangamecore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuickCooldownCommand implements CommandExecutor {
    
    private static boolean quickcooldowns = false;

    public static boolean getRandomizer(){
        return quickcooldowns;
    }

    public static void setRandomizer(Boolean bool){
        quickcooldowns = bool;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bg.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        toggleQuickCooldown();
        return true;
    }

    private void toggleQuickCooldown() {
        quickcooldowns = !quickcooldowns;

        String message = quickcooldowns ? "§dQUICK COOLDOWNS ENABLED!" : "§dQUICK COOLDOWNS DISABLED!";

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(null, message, 20, 100, 20);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.4F);
        }

    }
}
