package com.beangamecore.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class BeangameInvsee implements CommandExecutor {
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        // Check if player can use invsee
        boolean canUseInvsee = !BeangameStart.alivePlayers.contains(uuid) || 
                              player.getGameMode().equals(GameMode.SPECTATOR) || 
                              player.isOp();
        
        if (!canUseInvsee) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§cYou must be dead to use invsee!"));
            return true;
        }
        
        // Handle command based on arguments and permissions
        if (args.length == 0) {
            // No arguments - show spectated player's inventory or own if not spectating
            handleNoArguments(player);
            return true;
        } else if (args.length >= 1) {
            // Has arguments - check if player is OP
            if (!player.isOp()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    TextComponent.fromLegacy("§cYou must be spectating a player to use this command!"));
                return true;
            }
            
            // OP player using argument - show specific player's inventory
            handleWithArguments(player, args[0]);
            return true;
        }
        
        return true;
    }
    
    /**
     * Handle /invsee with no arguments
     */
    private void handleNoArguments(Player viewer) {
        // Check if viewer is spectating someone
        if (viewer.getGameMode() == GameMode.SPECTATOR && 
            viewer.getSpectatorTarget() instanceof Player) {
            
            Player target = (Player) viewer.getSpectatorTarget();
            showPlayerInventory(viewer, target);
            return;
        }
        
        // Not spectating - show usage based on permission
        if (viewer.isOp()) {
            viewer.sendMessage("§eUsage:");
            viewer.sendMessage("§7/invsee §f- View spectated player's inventory");
            viewer.sendMessage("§7/invsee <player> §f- View specific player's inventory");
        } else {
            viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§cYou must be spectating a player to use this command!"));
        }
    }
    
    /**
     * Handle /invsee <player> (OP only)
     */
    private void handleWithArguments(Player viewer, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§cPlayer not found: §7" + targetName));
            return;
        }
        
        if (target.equals(viewer)) {
            viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacy("§cYou cannot view your own inventory with this command!"));
            return;
        }
        
        showPlayerInventory(viewer, target);
    }
    
    /**
     * Shows a player's inventory to the viewer
     * @param isForced true if using argument (OP command), false if viewing spectated player
     */
    private void showPlayerInventory(Player viewer, Player target) {
        // Create the inventory
        String title = ChatColor.YELLOW + target.getName() + "'s Inventory";
        
        Inventory inv = Bukkit.createInventory(null, 45, title);
        
        // Copy main inventory and hotbar
        copyPlayerInventory(target, inv);
        
        // Copy armor
        copyArmor(target, inv);
        
        // Copy offhand
        copyOffhand(target, inv);
        
        // Add separator items
        addSeparators(inv);
        
        // Add information item
        addInfoItem(inv, target);
        
        // Open inventory for viewer
        viewer.openInventory(inv);
        
        // Play sound
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
        
    }
    
    /**
     * Copies main inventory and hotbar to display slots
     */
    private void copyPlayerInventory(Player target, Inventory display) {
        ItemStack[] targetContents = target.getInventory().getContents();
        
        // Main inventory (slots 9-35 in player inventory -> slots 0-26 in display)
        for (int i = 9; i < 36; i++) {
            int displaySlot = i - 9;
            if (targetContents[i] != null && targetContents[i].getType() != Material.AIR) {
                display.setItem(displaySlot, targetContents[i].clone());
            }
        }
        
        // Hotbar (slots 0-8 in player inventory -> slots 36-44 in display)
        for (int i = 0; i < 9; i++) {
            int displaySlot = i + 27;
            if (targetContents[i] != null && targetContents[i].getType() != Material.AIR) {
                display.setItem(displaySlot, targetContents[i].clone());
            }
        }
    }
    
    /**
     * Copies armor to display slots
     */
    private void copyArmor(Player target, Inventory display) {
        ItemStack[] armorContents = target.getInventory().getArmorContents();
        
        // Armor slots: 36-39 (boots, leggings, chestplate, helmet)
        display.setItem(36, armorContents[0] != null ? armorContents[0].clone() : null); // Boots
        display.setItem(37, armorContents[1] != null ? armorContents[1].clone() : null); // Leggings
        display.setItem(38, armorContents[2] != null ? armorContents[2].clone() : null); // Chestplate
        display.setItem(39, armorContents[3] != null ? armorContents[3].clone() : null); // Helmet
    }
    
    /**
     * Copies offhand item
     */
    private void copyOffhand(Player target, Inventory display) {
        ItemStack offhand = target.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR) {
            display.setItem(40, offhand.clone());
        }
    }
    
    /**
     * Adds separator glass panes
     */
    private void addSeparators(Inventory inventory) {
        ItemStack separator = createSeparatorItem();
        
        for (int i = 41; i < 44; i++) {
            inventory.setItem(i, separator);
        }
    }
    
    /**
     * Creates a separator glass pane item
     */
    private ItemStack createSeparatorItem() {
        ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = separator.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7 ");
            separator.setItemMeta(meta);
        }
        return separator;
    }
    
    /**
     * Adds an info item to the inventory
     */
    private void addInfoItem(Inventory inventory, Player target) {
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta meta = info.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§eInventory Information");
            meta.setLore(java.util.Arrays.asList(
                 "§7Player: §f" + target.getName(),
                "§7Health: §f" + String.format("%.1f", target.getHealth()) + "§7/§f" + String.format("%.1f", target.getAttribute(Attribute.MAX_HEALTH)),
                "§7Food: §f" + target.getFoodLevel() + "§7/20",
                "§7Gamemode: §f" + target.getGameMode().name()
            ));
            info.setItemMeta(meta);
        }
        
        inventory.setItem(44, info);
    }
}
