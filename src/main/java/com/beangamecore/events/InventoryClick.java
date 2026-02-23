package com.beangamecore.events;

import com.beangamecore.items.*;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGInventoryI;
import com.beangamecore.util.ItemNBT;
import com.beangamecore.util.Key;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.Main;
import com.beangamecore.commands.BeangameActiveItemlist;
import com.beangamecore.commands.BeangameDistribute;
import com.beangamecore.commands.BeangameItemlist;
import com.beangamecore.data.DatabaseManager;

public class InventoryClick implements Listener{
    
    // Queue to hold SQL updates for item takes
    private static final Queue<String> sqlTakeQueue = new ConcurrentLinkedQueue<>();
    private static final int SQL_BATCH_SIZE = 10;
    private static final long SQL_DELAY_BETWEEN_BATCHES = 20L;
    
    @EventHandler
    private void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getView().getTitle().contains("'s Inventory")) {
            // Cancel all interactions with invsee inventories
            event.setCancelled(true);
        // beangame item distribute inventory
        } else if (ChatColor.translateAlternateColorCodes('&', event.getView().getTitle()).equals(ChatColor.GOLD + "Beangame!") && event.getCurrentItem() != null) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            switch (event.getRawSlot()) {
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                    if (event.getCurrentItem().getType().equals(Material.GRAY_STAINED_GLASS_PANE)) {
                        return;
                    }
                    if (player.getInventory().firstEmpty() != -1) {
                        assert event.getCurrentItem() != null;
                        player.getInventory().addItem(new ItemStack(event.getCurrentItem()));
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), event.getCurrentItem());
                    }

                    for(BeangameItem bgitem : BeangameItemRegistry.collection()){
                        if(ItemNBT.isBeanGame(event.getCurrentItem(), bgitem.getKey())){
                            String nsk = bgitem.getKey().toString();
                            incrementNumTakes(nsk); // Now uses queue system
                            break;
                        }
                    }

                    // prevents dupes
                    event.getCurrentItem().setType(Material.AIR);
                    BeangameDistribute.bgdistributePlayers.remove(player.getUniqueId());
                    player.closeInventory();
                    break;
                case 26:
                    if (event.getCurrentItem().getType().equals(Material.TRIAL_KEY)) {
                        // Consume iron and reroll if possible
                        int count = 0;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item == null || item.getType() != Material.IRON_INGOT) {
                                continue;
                            }
                            if (ItemNBT.hasBeanGameTag(item)) {
                                continue;
                            }
                            count += item.getAmount();
                        }
                
                        if (count < 12) {
                            return; // Not enough iron to proceed
                        }
                
                        int removed = 0;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (removed >= 12) {
                                break; // Exit once 8 iron has been removed
                            }
                            if (item == null || item.getType() != Material.IRON_INGOT) {
                                continue;
                            }
                            if (ItemNBT.hasBeanGameTag(item)) {
                                continue;
                            }
                
                            // Remove iron ingots, up to 8
                            int amountToRemove = Math.min(12 - removed, item.getAmount());
                            item.setAmount(item.getAmount() - amountToRemove);
                            removed += amountToRemove;
                        }
                
                        // Play sound
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 1);
                
                        // Create the Bean Chronicle item and add it to the player's inventory
                        ItemStack item = BeangameItemRegistry.get(NamespacedKey.fromString("beangame:beanchronicles")).get().asItem();
                
                        // Add item to inventory or drop if full
                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(item);
                        } else {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                
                        // Clear the current item from the event (set it to AIR)
                        event.getCurrentItem().setType(Material.AIR);
                
                        // Remove player from the distribution queue
                        BeangameDistribute.bgdistributePlayers.remove(player.getUniqueId());
                
                        // Close the player's inventory
                        player.closeInventory();
                        break;
                    }
                default:
            }
        } else if ((ChatColor.translateAlternateColorCodes('&', event.getView().getTitle()).equals(String.valueOf(ChatColor.GOLD) + "Beangame Itemlist!") || ChatColor.translateAlternateColorCodes('&', event.getView().getTitle()).equals(String.valueOf(ChatColor.GOLD) + "Beangame Active Itemlist!")) && event.getCurrentItem() != null) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            Player player = (Player) event.getWhoClicked();
            if (slot <= 44) {
                if(player.isOp()){
                    if (event.getCurrentItem().getType().equals(Material.GRAY_STAINED_GLASS_PANE)) {
                        return;
                    }
                    if (player.getInventory().firstEmpty() != -1) {
                        assert event.getCurrentItem() != null;
                        player.getInventory().addItem(new ItemStack[]{
                                new ItemStack(event.getCurrentItem())
                        });
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), event.getCurrentItem());
                    }
                    // prevents dupes
                    event.getCurrentItem().setType(Material.AIR);
                    player.closeInventory();
                }
            } else if (event.getCurrentItem().getType().equals(Material.GRAY_STAINED_GLASS_PANE)) {
                return;
            } else if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED + "Back")) { // go back
                    int page = event.getInventory().getItem(49).getItemMeta().getCustomModelData();
                    page--;
                    if(ChatColor.translateAlternateColorCodes('&', event.getView().getTitle()).equals(String.valueOf(ChatColor.GOLD) + "Beangame Itemlist!")){
                        player.openInventory(BeangameItemlist.menu.get(page));
                    } else {
                        player.openInventory(BeangameActiveItemlist.menu.get(page));
                    }
                } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next")) { // go forwards
                    int page = event.getInventory().getItem(49).getItemMeta().getCustomModelData();
                    page++;
                    if(ChatColor.translateAlternateColorCodes('&', event.getView().getTitle()).equals(String.valueOf(ChatColor.GOLD) + "Beangame Itemlist!")){
                        player.openInventory(BeangameItemlist.menu.get(page));
                    } else {
                        player.openInventory(BeangameActiveItemlist.menu.get(page));
                    }
                }
                return;
            } else {
                return;
            }
            // cell phone inventory
        } else if(event.getView().getTitle().equals("§9Cell Phone!")){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cellphone"), CellPhone.class).cellphoneInventoryClick(event);
        // cell phone inventory part 2
        } else if(event.getView().getTitle().equals("§9Cell Phone Ringing!")){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cellphone"), CellPhone.class).cellphonereceiveInventoryClick(event);
        // cookie clicker inventory
        } else if(event.getView().getTitle().equals("§6Cookie Clicker ™")){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cookieclicker"), CookieClicker.class).cookieclickerInventoryClick(event);
        // dweam sword inventory
        } else if(event.getView().getTitle().equals("§aDweam sword!")){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:dweamsword"), DweamSword.class).dweamswordInventoryClick(event);
        // revive inventory
        } else if(event.getView().getTitle().equals("§dRevive!")){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:revive"), Revive.class).reviveInventoryClick(event);
        // revive with inventory inventory
        } else if(event.getView().getTitle().equals("§dRevive With Inventory!")){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:revivewithinventory"), ReviveWithInventory.class).reviveInventoryClick(event);
        // trapper's capital inventory
        } else if(event.getView().getTitle().equals("§cTrapper's Capital!")){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:trapperscapital"), TrappersCapital.class).trapperscapitalInventoryClick(event);
        } else if(event.getView().getTitle().equals("§bWalkie Talkie!")){
            BeangameItemRegistry.getRaw(Key.bg("walkietalkie"), WalkieTalkie.class).walkieTalkieInventoryClick(event);
        } else {
            for (BeangameItem item : BeangameItemRegistry.collection()) {
                if(item instanceof BGInventoryI i && event.getView().getTitle().equals(i.getInventoryName())) i.onInventoryClick(event);
            }
        } 

    }

    private void incrementNumTakes(String nsk){
        // Add to queue instead of processing immediately
        sqlTakeQueue.add(nsk);
        processTakeQueue();
    }

    private void processTakeQueue() {
        // Don't check isProcessingTakes here - just schedule the async task
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            // Use a synchronized block to ensure only one batch processes at a time
            synchronized (sqlTakeQueue) {
                List<String> batch = new ArrayList<>();
                while (!sqlTakeQueue.isEmpty() && batch.size() < SQL_BATCH_SIZE) {
                    batch.add(sqlTakeQueue.poll());
                }

                if (!batch.isEmpty()) {
                    
                    try (Connection conn = DatabaseManager.getConnection();
                        PreparedStatement statement = conn.prepareStatement(
                            "UPDATE items SET num_takes = num_takes + 1 WHERE key_name = ?")) {
                        
                        for (String nsk : batch) {
                            statement.setString(1, nsk);
                            statement.addBatch();
                        }
                        
                        int[] results = statement.executeBatch();
                        int successfulUpdates = 0;
                        
                        for (int result : results) {
                            if (result >= 0) {
                                successfulUpdates++;
                            }
                        }
                        
                        if (successfulUpdates < batch.size()) {
                            Main.getPlugin().getLogger().warning("Some take updates failed: " + (batch.size() - successfulUpdates) + " items");
                            // Re-add failed items
                            sqlTakeQueue.addAll(batch);
                        }
                        
                    } catch (SQLException e) {
                        Main.getPlugin().getLogger().severe("Failed to process take batch: " + e.getMessage());
                        // Re-add all items on failure
                        sqlTakeQueue.addAll(batch);
                    }
                }

                // Schedule next batch if there are more updates
                if (!sqlTakeQueue.isEmpty()) {
                    
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), 
                        () -> {
                            processTakeQueue();
                        }, SQL_DELAY_BETWEEN_BATCHES);
                }
            }
        });
    }
}
