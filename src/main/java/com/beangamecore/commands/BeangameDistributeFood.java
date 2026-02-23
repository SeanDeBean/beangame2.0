package com.beangamecore.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
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
import com.beangamecore.data.DatabaseManager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

public class BeangameDistributeFood implements CommandExecutor {
    // Queue to hold SQL updates
    private static final Queue<SQLUpdateTask> sqlUpdateQueue = new ConcurrentLinkedQueue<>();
    private static final int SQL_BATCH_SIZE = 10;
    private static final long SQL_DELAY_BETWEEN_BATCHES = 20L;
    
    // New: Queue for player processing
    private static final Queue<Player> playerProcessQueue = new ConcurrentLinkedQueue<>();
    private static final int PLAYER_BATCH_SIZE = 3;
    private static final long PLAYER_DELAY_BETWEEN_BATCHES = 2L;
    private static boolean isProcessingPlayers = false;
    private static ArrayList<ItemStack> sharedFoodItems; // Store shared items for all players

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bg.use") && !BeangameDistribute.bgdistributeRecent) {
                startFoodDistribution(player);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            }
        }
        return false;
    }

    private void startFoodDistribution(Player commander) {
        // Initial announcements
        for (Player warnmessage : Bukkit.getOnlinePlayers()) {
            warnmessage.getWorld().playSound(warnmessage.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
            warnmessage.sendTitle(null, "§3Food items arriving soon!", 20, 100, 20);
            warnmessage.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fPvP disabled!"));
        }
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");
        commander.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Distributed food items!"));
        
        BeangameDistribute.bgdistributeRecent = true;
        PvpToggleCommand.pvp = false;
        
        generateAndDistributeFoodItems();    
        
        // Schedule PvP re-enable after 160 ticks (8 seconds)
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (!BeangameStart.startphase) {
                for (Player warnmessage : Bukkit.getOnlinePlayers()) {
                    warnmessage.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fPvP enabled!"));
                }
                PvpToggleCommand.pvp = true;
            }
            BeangameDistribute.bgdistributeRecent = false;
        }, 160L);
    }
    
    private void generateAndDistributeFoodItems() {
        // Generate shared food items first
        generateSharedFoodItems();
        
        // Clear and populate player queue
        playerProcessQueue.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                playerProcessQueue.add(player);
            }
        }
        
        // Start processing players

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            // Start processing players after delay
            startPlayerProcessing();
        }, 44L);
    }
    
    private void generateSharedFoodItems() {
        ArrayList<BeangameItem> beangameItems = BeangameItemRegistry.getFoodItemsInRotation();
        sharedFoodItems = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();
        
        // Generate 3 unique food items
        for (int j = 0; j < 3; j++) {
            int randomIndex;
            do {
                randomIndex = ThreadLocalRandom.current().nextInt(beangameItems.size());
            } while (usedIndices.contains(randomIndex));
            
            usedIndices.add(randomIndex);
            ItemStack item = beangameItems.get(randomIndex).asItem();
            sharedFoodItems.add(item);
            
            // Queue database update for all players
            String nsk = beangameItems.get(randomIndex).getKey().toString();
            int count = Bukkit.getOnlinePlayers().size();
            incrementNumAppearances(nsk, count);
        }
    }
    
    private void startPlayerProcessing() {
        if (isProcessingPlayers) {
            return;
        }
        
        isProcessingPlayers = true;
        processPlayerBatch();
    }
    
    private void processPlayerBatch() {
        if (playerProcessQueue.isEmpty()) {
            isProcessingPlayers = false;
            return;
        }
        
        Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
            int processed = 0;
            ItemStack backgroundglass = createBackgroundGlass();
            
            while (!playerProcessQueue.isEmpty() && processed < PLAYER_BATCH_SIZE) {
                Player player = playerProcessQueue.poll();
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    processFoodInventoryForPlayer(player, backgroundglass);
                }
                
                processed++;
            }
            
            // Schedule next batch if there are more players
            if (!playerProcessQueue.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), 
                    this::processPlayerBatch, PLAYER_DELAY_BETWEEN_BATCHES);
            } else {
                isProcessingPlayers = false;
            }
        });
    }
    
    private void processFoodInventoryForPlayer(Player player, ItemStack backgroundglass) {
        try {
            // Create individual array for this player
            ArrayList<ItemStack> individualArray = new ArrayList<>();
            individualArray.add(backgroundglass.clone());
            individualArray.add(backgroundglass.clone());
            
            // Add the 3 shared food items
            for (int i = 0; i < 3; i++) {
                individualArray.add(sharedFoodItems.get(i).clone());
            }
            
            individualArray.add(backgroundglass.clone());
            individualArray.add(backgroundglass.clone());
            
            // Save to global storage
            BeangameDistribute.bgdistributeSave.put(player.getUniqueId(), individualArray);
            
            // Create and open inventory
            Inventory bginv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Beangame!");
            
            // Fill background slots
            int[] backgroundSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 
                                    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
            for (int slot : backgroundSlots) {
                bginv.setItem(slot, backgroundglass.clone());
            }
            
            // Add food items to slots 12, 13, 14
            for (int i = 12; i < 15; i++) {
                bginv.setItem(i, sharedFoodItems.get(i - 12).clone());
            }
            
            // Open inventory
            player.openInventory(bginv);
            
            // Update player status
            UUID playerUUID = player.getUniqueId();
            BeangameDistribute.bgdistributePlayers.put(playerUUID, true);
            
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while creating your food inventory.");
        }
    }
    
    private ItemStack createBackgroundGlass() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setHideTooltip(true);
        glass.setItemMeta(meta);
        return glass;
    }
    
    private void incrementNumAppearances(String nsk, int count) {
        if (nsk != null && count > 0) {
            sqlUpdateQueue.add(new SQLUpdateTask(nsk, count));
            processSqlQueue();
        }
    }
    
    private void processSqlQueue() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            List<SQLUpdateTask> batch = new ArrayList<>();
            while (!sqlUpdateQueue.isEmpty() && batch.size() < SQL_BATCH_SIZE) {
                SQLUpdateTask task = sqlUpdateQueue.poll();
                if (task != null) {
                    batch.add(task);
                }
            }

            if (!batch.isEmpty()) {
                Main.getPlugin().getLogger().info("Processing SQL batch of " + batch.size() + " food items");
                
                try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement statement = conn.prepareStatement(
                        "UPDATE items SET num_appearances = num_appearances + ? WHERE key_name = ?")) {
                    
                    for (SQLUpdateTask task : batch) {
                        if (task != null && task.nsk != null) {
                            statement.setInt(1, task.count);
                            statement.setString(2, task.nsk);
                            statement.addBatch();
                        }
                    }
                    
                    statement.executeBatch();
                    
                } catch (SQLException e) {
                    Main.getPlugin().getLogger().severe("Failed to update " + batch.size() + " food items: " + e.getMessage());
                    sqlUpdateQueue.addAll(batch);
                }
            }

            if (!sqlUpdateQueue.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), 
                    this::processSqlQueue, SQL_DELAY_BETWEEN_BATCHES);
            }
        });
    }

    // Helper class to store SQL update tasks
    private static class SQLUpdateTask {
        String nsk;
        int count;

        SQLUpdateTask(String nsk, int count) {
            this.nsk = nsk;
            this.count = count;
        }
    }
}
