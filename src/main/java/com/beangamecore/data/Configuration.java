package com.beangamecore.data;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BeangameSoftItem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration implements DataObject {

    public static final int VERSION = 5;
    private int version = 0;
    private TreeMap<String, BGItemMeta> meta = new TreeMap<>();
    private static final Logger logger = Main.logger();

    // Cache for frequently accessed item metadata to reduce database queries
    private static final Map<String, BGItemMeta> itemMetaCache = new ConcurrentHashMap<>();
    
    // Track processed items to ensure all items are handled
    private static final Set<String> processedItems = ConcurrentHashMap.newKeySet();
    
    // Track failed items for retry
    private static final Queue<BeangameItem> failedItems = new ConcurrentLinkedQueue<>();
    
    // Flag to track if processing is complete
    private static volatile boolean processingComplete = false;
    
    // Executor for async processing
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    // Check if the configuration needs an update and reset metadata if necessary
    public boolean update() {
        if (version < VERSION) {
            version = VERSION;
            meta.clear(); // Clear existing metadata
            itemMetaCache.clear(); // Clear the cache
            processedItems.clear();
            failedItems.clear();
            processingComplete = false;
            return true;
        }
        return false;
    }

    // Process a list of items with completion tracking
    public void createIfAbsent(List<BeangameItem> items) {
        if (items == null || items.isEmpty()) return;
        
        // Reset tracking
        processedItems.clear();
        failedItems.clear();
        processingComplete = false;

        // Split items into smaller batches for efficient processing
        List<List<BeangameItem>> batches = partition(items, 50); // Smaller batch size for reliability

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (List<BeangameItem> batch : batches) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processBatch(batch), executorService)
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error processing batch, queuing for retry", ex);
                        // Add failed items to retry queue
                        failedItems.addAll(batch);
                        return null;
                    });
            futures.add(future);
        }

        // Wait for all batches to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    processingComplete = true;
                    logger.info("Initial item processing completed. Processed: " + processedItems.size() + " items.");
                    
                    // Process any failed items
                    if (!failedItems.isEmpty()) {
                        logger.warning("Retrying " + failedItems.size() + " failed items...");
                        retryFailedItems();
                    }
                })
                .exceptionally(ex -> {
                    logger.log(Level.SEVERE, "Fatal error during item processing", ex);
                    processingComplete = true;
                    return null;
                });
    }
    
    // Retry failed items with exponential backoff
    private void retryFailedItems() {
        if (failedItems.isEmpty()) return;
        
        List<BeangameItem> retryBatch = new ArrayList<>();
        while (!failedItems.isEmpty() && retryBatch.size() < 20) {
            retryBatch.add(failedItems.poll());
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second before retry
                processBatchSync(retryBatch); // Use synchronous processing for retries
                
                if (!failedItems.isEmpty()) {
                    // Schedule another retry
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), this::retryFailedItems, 100L);
                } else {
                    logger.info("All failed items processed successfully.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during retry", e);
                // Re-add to queue for later retry
                failedItems.addAll(retryBatch);
            }
        }, executorService);
    }
    
    // Synchronous batch processing for retries
    private void processBatchSync(List<BeangameItem> batch) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            for (BeangameItem item : batch) {
                processSingleItemSync(conn, item);
            }
            
            conn.commit();
            logger.info("Retry batch processed: " + batch.size() + " items");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to process retry batch", e);
            throw new RuntimeException("Database error during retry", e);
        }
    }

    // Process a batch of items in a single database transaction
    private void processBatch(List<BeangameItem> batch) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            List<BeangameItem> successfulItems = new ArrayList<>();

            for (BeangameItem item : batch) {
                try {
                    if (meta.containsKey(item.getKey().toString())) {
                        // Already in meta, just update cache
                        itemMetaCache.put(item.getKey().toString(), meta.get(item.getKey().toString()));
                        processedItems.add(item.getKey().toString());
                        continue;
                    }

                    BGItemMeta bgItemMeta = createBGItemMeta(item);
                    meta.put(item.getKey().toString(), bgItemMeta);
                    itemMetaCache.put(item.getKey().toString(), bgItemMeta);
                    
                    // Skip database for soft items
                    if (!(item instanceof BeangameSoftItem)) {
                        if (itemExistsInDatabase(conn, item.getKey().toString())) {
                            updateItemInDatabase(conn, item);
                        } else {
                            saveItemToDatabase(conn, item);
                        }
                        
                        // Verify the item was saved
                        if (!verifyItemSaved(conn, item.getKey().toString())) {
                            throw new SQLException("Failed to verify item save for: " + item.getKey());
                        }
                    }
                    
                    processedItems.add(item.getKey().toString());
                    successfulItems.add(item);
                    
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to process item: " + item.getKey(), e);
                    // Don't add to successful items
                }
            }
            
            conn.commit();
            
            if (!successfulItems.isEmpty()) {
                logger.info("Batch processed: " + successfulItems.size() + "/" + batch.size() + " items");
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error processing batch of " + batch.size() + " items", e);
            throw new RuntimeException("Database error", e);
        }
    }
    
    // Verify an item was successfully saved to the database
    private boolean verifyItemSaved(Connection conn, String keyName) throws SQLException {
        String query = "SELECT key_name, name FROM items WHERE key_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, keyName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Create a new BGItemMeta object from a BeangameItem
    private BGItemMeta createBGItemMeta(BeangameItem item) {
        BGItemMeta bgItemMeta = new BGItemMeta();
        bgItemMeta.setName(item.getName());
        bgItemMeta.setLore(item.getLore());
        bgItemMeta.setEnchantments(item.getEnchantments());
        bgItemMeta.setMaterial(item.getMaterial());
        bgItemMeta.setCustomModelData(item.getCustomModelData());
        bgItemMeta.setItemFlags(item.getItemFlags());
        bgItemMeta.setArmorTrim(item.getArmorTrim());
        bgItemMeta.setColor(item.getColor());
        bgItemMeta.setArmor(item.getArmor());
        bgItemMeta.setSlot(item.getSlot());
        bgItemMeta.setInRotation(item.isInItemRotation());
        bgItemMeta.setInFoodRotation(item.isInFoodItemRotation());
        bgItemMeta.setUnbreakable(item.isUnbreakable());
        bgItemMeta.setMaxStackSize(item.getMaxStackSize());
        bgItemMeta.setEquipmentData(item.getEquipmentData());
        bgItemMeta.setGlideArmor(item.isGlidingArmor());
        bgItemMeta.serialize();
        return bgItemMeta;
    }
    
    // Synchronous processing for single item (used during retry)
    private void processSingleItemSync(Connection conn, BeangameItem item) throws SQLException {
        if (meta.containsKey(item.getKey().toString())) {
            return;
        }

        BGItemMeta bgItemMeta = createBGItemMeta(item);
        meta.put(item.getKey().toString(), bgItemMeta);
        itemMetaCache.put(item.getKey().toString(), bgItemMeta);

        if (!(item instanceof BeangameSoftItem)) {
            if (itemExistsInDatabase(conn, item.getKey().toString())) {
                updateItemInDatabase(conn, item);
            } else {
                saveItemToDatabase(conn, item);
            }
            
            if (!verifyItemSaved(conn, item.getKey().toString())) {
                throw new SQLException("Verification failed for: " + item.getKey());
            }
        }
        
        processedItems.add(item.getKey().toString());
    }

    public void applyMeta(ItemStack item, NamespacedKey id) {
        BGItemMeta bgmeta = meta.get(id.toString());
        if (item == null || bgmeta == null) return;
        if (item.getType() != Material.AIR) {
            item.setType(bgmeta.getMaterial());
            ItemMeta bukkitMeta = item.getItemMeta();
            bukkitMeta.setDisplayName(bgmeta.getName());
            bukkitMeta.setLore(bgmeta.getLore());
            bukkitMeta.setUnbreakable(bgmeta.isUnbreakable());
            bukkitMeta.setCustomModelData(bgmeta.getCustomModelData());
            if(bgmeta.getMaxStackSize() != 64){
                bukkitMeta.setMaxStackSize(bgmeta.getMaxStackSize());
            }
            bukkitMeta.setGlider(bgmeta.isGlideArmor());
            if (bgmeta.getEquipmentData() != null) bukkitMeta.setEquippable(bgmeta.getEquipmentData());
        
            // Apply ArmorMeta
            if (bukkitMeta instanceof ArmorMeta armorMeta) {
                if (bgmeta.getArmorTrim() != null) {
                    armorMeta.setTrim(bgmeta.getArmorTrim());
                }
                if (bgmeta.getArmor() != 0) {
                    NamespacedKey nsk = new NamespacedKey(Main.getPlugin(), "armor." + id.getKey());
                    EquipmentSlotGroup slot = bgmeta.getSlot() != null ? bgmeta.getSlot() : EquipmentSlotGroup.HAND;
                    armorMeta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(nsk, bgmeta.getArmor(), AttributeModifier.Operation.ADD_NUMBER, slot));
                }
            }

            bgmeta.getItemFlags().forEach(bukkitMeta::addItemFlags);

            // Apply enchantments safely
            bgmeta.getEnchantments().forEach((k, v) -> {
                NamespacedKey key = NamespacedKey.fromString(k);
                if (key == null) {
                    Main.logger().log(Level.WARNING, "Invalid enchantment key: " + k);
                    return;
                }
                Enchantment enchantment = Enchantment.getByKey(key);
                if (enchantment == null) {
                    Main.logger().log(Level.WARNING, "Enchantment not found: " + k);
                    return;
                }
                bukkitMeta.addEnchant(enchantment, v, true);
            });

            if(bukkitMeta instanceof LeatherArmorMeta leatherArmorMeta){
                leatherArmorMeta.setColor(bgmeta.getColor());
            }
            item.setItemMeta(bukkitMeta);
        }
    }

    // Check if an item exists in the database
    private boolean itemExistsInDatabase(Connection conn, String keyName) throws SQLException {
        String query = "SELECT 1 FROM items WHERE key_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, keyName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Update an existing item in the database
    private void updateItemInDatabase(Connection conn, BeangameItem item) throws SQLException {
        String query = "UPDATE items SET name = ?, lore = ?, material = ?, custom_model_data = ?, " +
                      "armor = ?, max_stack_size = ?, in_rotation = ?, in_food_rotation = ?, " +
                      "description = ?, last_updated = CURRENT_TIMESTAMP WHERE key_name = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, sanitizeLoreText(item.getLore())); // Use sanitized lore
            stmt.setString(3, item.getMaterial().name());
            stmt.setInt(4, item.getCustomModelData());
            stmt.setDouble(5, item.getArmor());
            stmt.setInt(6, item.getMaxStackSize());
            stmt.setBoolean(7, item.isInItemRotation());
            stmt.setBoolean(8, item.isInFoodItemRotation());
            stmt.setString(9, "update description here");
            stmt.setString(10, item.getKey().toString());
            
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No rows updated for item: " + item.getKey());
            }
        }
    }

    // Save a new item to the database
    private void saveItemToDatabase(Connection conn, BeangameItem item) throws SQLException {
        String query = "INSERT INTO items (name, key_name, lore, material, custom_model_data, " +
                      "armor, max_stack_size, in_rotation, in_food_rotation, description, " +
                      "created_at, last_updated) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getKey().toString());
            stmt.setString(3, sanitizeLoreText(item.getLore())); // Use sanitized lore
            stmt.setString(4, item.getMaterial().name());
            stmt.setInt(5, item.getCustomModelData());
            stmt.setDouble(6, item.getArmor());
            stmt.setInt(7, item.getMaxStackSize());
            stmt.setBoolean(8, item.isInItemRotation());
            stmt.setBoolean(9, item.isInFoodItemRotation());
            stmt.setString(10, "update description here");
            stmt.executeUpdate();
        }
    }

    // Serialize metadata
    @Override
    public void onSerialize() {
        meta.forEach((k, v) -> v.serialize());
    }

    // Deserialize metadata
    @Override
    public void onDeserialize() {
        meta.forEach((k, v) -> v.deserialize());
    }

    // Get item metadata from cache or metadata map
    public BGItemMeta getItemMeta(BeangameItem item) {
        return itemMetaCache.computeIfAbsent(item.getKey().toString(), k -> meta.get(k));
    }

    // Get the current version of the configuration
    public int getVersion() {
        return version;
    }
    
    // Check if processing is complete
    public boolean isProcessingComplete() {
        return processingComplete;
    }
    
    // Get number of processed items
    public int getProcessedCount() {
        return processedItems.size();
    }
    
    // Get number of failed items
    public int getFailedCount() {
        return failedItems.size();
    }

    // Split a list into smaller batches
    private List<List<BeangameItem>> partition(List<BeangameItem> items, int batchSize) {
        List<List<BeangameItem>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(items.subList(i, Math.min(i + batchSize, items.size())));
        }
        return batches;
    }
    
    // Shutdown executor on plugin disable
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private String sanitizeLoreText(List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            return "[]";
        }
        
        // Convert lore list to JSON string, ensuring proper escaping
        try {
            // Use Gson or simple escaping for special characters
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lore.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"");
                
                // Escape special characters
                String line = lore.get(i);
                line = line.replace("\\", "\\\\")
                          .replace("\"", "\\\"")
                          .replace("\n", "\\n")
                          .replace("\r", "\\r")
                          .replace("\t", "\\t");
                
                sb.append(line);
                sb.append("\"");
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            logger.warning("Failed to sanitize lore: " + e.getMessage());
            return "[]";
        }
    }
}
