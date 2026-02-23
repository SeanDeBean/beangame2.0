package com.beangamecore.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.Main;
import com.beangamecore.commands.BeangameStart;
import com.beangamecore.data.DatabaseManager;
import com.beangamecore.items.TalismanOfJumbledFates;
import com.beangamecore.items.WaWaWoodArmor;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerDeath implements Listener {

    // Queue to hold SQL updates
    private static final Queue<String> sqlUpdateQueue = new ConcurrentLinkedQueue<>();
    private static final int BATCH_SIZE = 10; // Number of updates to process per batch
    private static final long DELAY_BETWEEN_BATCHES = 20L; // Delay in ticks (1 second)

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        // Ensure the victim was killed by another player
        if (victim.getKiller() != null && victim.getKiller() instanceof Player) {
            // final Player killer = victim.getKiller();
        }

        if (Cooldowns.onCooldown("jumbling", victim.getUniqueId())) {
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:talismanofjumbledfates"), TalismanOfJumbledFates.class).applyJumbling(victim);
        }

        while (BeangameStart.alivePlayers.contains(victim.getUniqueId())) {
            BeangameStart.alivePlayers.remove(victim.getUniqueId());
        }

        WaWaWoodArmor woodArmorItem = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:wawawoodarmor"), WaWaWoodArmor.class);
        if (woodArmorItem != null) {
            woodArmorItem.handlePlayerDeath(event); // This handles the drops properly
        }

        for (ItemStack item : victim.getInventory()) {
            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }
            if(ItemNBT.hasBeanGameTag(item)){
                for(BeangameItem bgitem : BeangameItemRegistry.collection()){
                    if(ItemNBT.isBeanGame(item, bgitem.getKey())){
                        String nsk = bgitem.getKey().toString(); // Assuming BeangameItem has an ID field
                        incrementNumLosses(nsk);
                        break;
                    }
                }
            }
        }

        Iterator<ItemStack> dropIterator = event.getDrops().iterator();
        double percent = BeangameStart.percent;
        while (dropIterator.hasNext()) {
            ItemStack drop = dropIterator.next();
            if (drop != null && ItemNBT.hasBeanGameTag(drop)) {
                double randomValue = Math.random();
                if (randomValue >= percent) {
                    dropIterator.remove();
                }
            }
        }
    }

    private void incrementNumLosses(String nsk) {
        sqlUpdateQueue.add(nsk); // Add the update to the queue
        processQueue(); // Start processing the queue if it's not already running
    }

    private void processQueue() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            List<String> batch = new ArrayList<>();
            while (!sqlUpdateQueue.isEmpty() && batch.size() < BATCH_SIZE) {
                batch.add(sqlUpdateQueue.poll());
            }

            if (!batch.isEmpty()) {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement statement = conn.prepareStatement("UPDATE items SET num_losses = num_losses + 1 WHERE key_name = ?")) {
                    for (String nsk : batch) {
                        statement.setString(1, nsk);
                        statement.addBatch();
                    }
                    statement.executeBatch(); // Execute the batch update
                } catch (SQLException e) {
                    e.printStackTrace(); // Handle the exception appropriately
                }
            }

            if (!sqlUpdateQueue.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), this::processQueue, DELAY_BETWEEN_BATCHES);
            }
        });
    }
}

