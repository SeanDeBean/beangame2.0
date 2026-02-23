package com.beangamecore.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.beangamecore.items.Revive;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

public class BeangameDistribute implements CommandExecutor {
    public static boolean bgdistributeRecent = false;
    public static Map<UUID, Boolean> bgdistributePlayers = new HashMap<>();
    public static Map<UUID, ArrayList<ItemStack>> bgdistributeSave = new HashMap<>();

    // Queue to hold SQL updates
    private static final Queue<String> sqlUpdateQueue = new ConcurrentLinkedQueue<>();
    private static final int SQL_BATCH_SIZE = 10;
    private static final long SQL_DELAY_BETWEEN_BATCHES = 20L;
    
    // New: Queue for player processing
    private static final Queue<Player> playerProcessQueue = new ConcurrentLinkedQueue<>();
    private static final int PLAYER_BATCH_SIZE = 5; // Process 3 players at a time
    private static final long PLAYER_DELAY_BETWEEN_BATCHES = 2L; // 2 ticks between batches
    private static boolean isProcessingPlayers = false;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("bg.use") && !bgdistributeRecent) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Distributed items!"));
                distribute();
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to use this command!"));
            }
        }
        return false;
    }

    public void distribute() {
        for (Player warnmessage : Bukkit.getOnlinePlayers()) {
            warnmessage.getWorld().playSound(warnmessage.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
            warnmessage.sendTitle(null, "§3Items arriving soon!", 20, 100, 20);
            warnmessage.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fPvP disabled!"));
            bgdistributePlayers.put(warnmessage.getUniqueId(), false);
        }
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");
        bgdistributeRecent = true;
        PvpToggleCommand.pvp = false;
        
        // Clear any existing queue
        playerProcessQueue.clear();
        
        // Add all online players to the queue
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL || 
                (player.getGameMode() == GameMode.SPECTATOR && Revive.noRevive.contains(player.getUniqueId()))) {
                playerProcessQueue.add(player);
            }
        }
        
        // Start processing players in batches
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            // Start processing players in batches after delay
            startPlayerProcessing();
        }, 44L);
        
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (!BeangameStart.startphase) {
                for (Player warnmessage : Bukkit.getOnlinePlayers()) {
                    warnmessage.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fPvP enabled!"));
                }
                PvpToggleCommand.pvp = true;
            }
            bgdistributeRecent = false;
        }, 160L);
    }
    
    private void startPlayerProcessing() {
        if (isProcessingPlayers) {
            return; // Already processing
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
            ArrayList<BeangameItem> beangameItems = BeangameItemRegistry.getItemsInRotation();
            
            // Prepare reusable items
            ItemStack backgroundglass = createBackgroundGlass();
            ItemStack rerollicon = createRerollIcon();
            
            while (!playerProcessQueue.isEmpty() && processed < PLAYER_BATCH_SIZE) {
                Player player = playerProcessQueue.poll();
                if (player == null || !player.isOnline()) {
                    continue;
                }
                
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    processSurvivalPlayer(player, beangameItems, backgroundglass, rerollicon);
                } else if (player.getGameMode() == GameMode.SPECTATOR && Revive.noRevive.contains(player.getUniqueId())) {
                    processSpectatorPlayer(player);
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
    
    private void processSurvivalPlayer(Player player, ArrayList<BeangameItem> beangameItems, 
                                       ItemStack backgroundglass, ItemStack rerollicon) {
        try {
            Inventory bginv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Beangame!");
            
            // Fill background slots
            for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25}) {
                bginv.setItem(i, backgroundglass.clone()); // Clone to avoid sharing item meta
            }
            
            bginv.setItem(26, rerollicon.clone());
            
            // Generate unique items
            ArrayList<ItemStack> individualArray = new ArrayList<>();
            Set<Integer> usedIndices = new HashSet<>();
            int[] itemSlots = {10, 11, 12, 13, 14, 15, 16};
            
            for (int slot : itemSlots) {
                int rindex;
                do {
                    rindex = ThreadLocalRandom.current().nextInt(beangameItems.size());
                } while (usedIndices.contains(rindex));
                
                usedIndices.add(rindex);
                BeangameItem item = beangameItems.get(rindex);
                ItemStack itemStack = item.asItem();
                bginv.setItem(slot, itemStack);
                
                // Queue database update
                sqlUpdateQueue.add(item.getKey().toString());
                individualArray.add(itemStack);
            }
            
            // Save and open inventory
            bgdistributeSave.put(player.getUniqueId(), individualArray);
            player.openInventory(bginv);
            bgdistributePlayers.put(player.getUniqueId(), true);
            
            // Start processing SQL queue if not already running
            processSqlQueue();
            
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while creating your inventory.");
        }
    }
    
    private void processSpectatorPlayer(Player player) {
        try {
            ItemStack item = BeangameItemRegistry.get(NamespacedKey.fromString("beangame:beanchronicles"))
                .orElseThrow().asItem();
            
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private ItemStack createBackgroundGlass() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setHideTooltip(true);
        glass.setItemMeta(meta);
        return glass;
    }
    
    private ItemStack createRerollIcon() {
        ItemStack reroll = new ItemStack(Material.TRIAL_KEY);
        ItemMeta meta = reroll.getItemMeta();
        meta.setDisplayName("§aReroll");
        meta.setLore(List.of("§7Spend 12 Iron Ingots to reroll items!"));
        reroll.setItemMeta(meta);
        return reroll;
    }
    

    private void processSqlQueue() {
        // Process SQL queue asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            List<String> batch = new ArrayList<>();
            while (!sqlUpdateQueue.isEmpty() && batch.size() < SQL_BATCH_SIZE) {
                batch.add(sqlUpdateQueue.poll());
            }

            if (!batch.isEmpty()) {
                Main.getPlugin().getLogger().info("Processing SQL batch of " + batch.size() + " items");
                
                try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement statement = conn.prepareStatement(
                        "UPDATE items SET num_appearances = num_appearances + 1 WHERE key_name = ?")) {
                    
                    for (String nsk : batch) {
                        statement.setString(1, nsk);
                        statement.addBatch();
                    }
                    
                    statement.executeBatch();
                    
                } catch (SQLException e) {
                    Main.getPlugin().getLogger().severe("Failed to update " + batch.size() + " items: " + e.getMessage());
                    // Re-add failed items to the queue
                    sqlUpdateQueue.addAll(batch);
                }
            }

            // Schedule next batch if there are more updates
            if (!sqlUpdateQueue.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), 
                    this::processSqlQueue, SQL_DELAY_BETWEEN_BATCHES);
            }
        });
    }
}
