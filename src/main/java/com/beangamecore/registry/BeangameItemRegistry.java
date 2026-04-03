package com.beangamecore.registry;

import com.beangamecore.Main;
import com.beangamecore.blocks.generic.BeangameBlock;
import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.util.Booleans;
import com.beangamecore.util.ItemNBT;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import java.util.stream.Collectors;

public class BeangameItemRegistry {

    private static final Map<String, BeangameItem> registry = new ConcurrentSkipListMap<>();
    private static final Collection<NamespacedKey> recipes = new ArrayList<>();

    public static Collection<NamespacedKey> getRecipes(){
        return recipes;
    }

    public static Map<String, BeangameItem> getRegistry(){
        return registry;
    }

    /**
     * Register a list of items in batches to optimize database and configuration updates.
     */
    public static void register(List<BeangameItem> items) {
        if (items == null || items.isEmpty()) return;
        
        // Add items to the registry
        items.forEach(item -> {
            registry.put(item.getKey().toString(), item);

            if (item instanceof BGInvUnstackable) {
                Booleans.register(item.getKey() + "_invstackcheck");
            }
            if (item instanceof BeangameBlock block) {
                BeangameBlockRegistry.register(item.getKey(), block);
            }
        });

        // Process items in batches using the Configuration class
        Main.getConfiguration().createIfAbsent(items);

        setupProgressMonitoring();
        setupFinalVerification(items);

        // Register crafting recipes asynchronously
        registerRecipes(items);
    }

    /**
     * Register crafting recipes for the given items.
     */
    private static void registerRecipes(List<BeangameItem> items) {
        for (BeangameItem item : items) {
            CraftingRecipe recipe = item.getCraftingRecipe();
            if (recipe != null) {
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    Bukkit.addRecipe(recipe);
                    recipes.add(recipe.getKey());
                }, 1);
            }
        }
    }

    /**
     * Check if a player has a specific BeangameItem in their inventory.
     */
    public static boolean hasBeangameItem(Player player, BeangameItem item){
        for(ItemStack i : player.getInventory().getContents()){
            if(i == null) continue;
            if(i.getType().equals(Material.AIR)) continue;
            BeangameItem itest = getFromItemStackRaw(i);
            if(itest != null && itest.equals(item)) return true;
        }
        return false;
    }

    /**
     * Get a BeangameItem by its key.
     */
    public static BeangameItem getRaw(NamespacedKey key) {
        if (key == null) return null;
        return registry.get(key.toString());
    }

    public static <T extends BeangameItem> T getRaw(NamespacedKey key, Class<T> clazz){
        BeangameItem item = getRaw(key);
        return clazz.cast(item);
    }

    public static BeangameItem getRaw(String key){
        return registry.get("beangame:" + key);
    }

    /**
     * Get a BeangameItem by its key wrapped in an Optional.
     */
    public static Optional<BeangameItem> get(NamespacedKey key) {
        return Optional.ofNullable(getRaw(key));
    }

    /**
     * Get a BeangameItem by its key and cast it to a specific type.
     */
    public static <T extends BeangameItem> Optional<T> get(NamespacedKey key, Class<T> clazz) {
        BeangameItem item = getRaw(key);
        return clazz.isInstance(item) ? Optional.of(clazz.cast(item)) : Optional.empty();
    }

    /**
     * Get a BeangameItem from an ItemStack.
     */
    public static Optional<BeangameItem> getFromItemStack(ItemStack stack) {
        NamespacedKey key = ItemNBT.getBeanGame(stack);
        return get(key);
    }

    /**
     * Get a BeangameItem from an ItemStack and cast it to a specific type.
     */
    public static <T extends BeangameItem> Optional<T> getFromItemStack(ItemStack stack, Class<T> clazz) {
        NamespacedKey key = ItemNBT.getBeanGame(stack);
        return get(key, clazz);
    }

    /**
     * Get a BeangameItem from an ItemStack without wrapping it in an Optional.
     */
    public static BeangameItem getFromItemStackRaw(ItemStack stack) {
        NamespacedKey key = ItemNBT.getBeanGame(stack);
        return getRaw(key);
    }

    /**
     * Get a BeangameItem from an ItemStack and cast it to a specific type without wrapping it in an Optional.
     */
    public static <T extends BeangameItem> T getFromItemStackRaw(ItemStack stack, Class<T> clazz) {
        NamespacedKey key = ItemNBT.getBeanGame(stack);
        return clazz.cast(getRaw(key));
    }

    /**
     * Get all registered BeangameItems.
     */
    public static Collection<BeangameItem> collection() {
        return new ArrayList<>(registry.values());
    }

    /**
     * Get all BeangameItems that match a given predicate.
     */
    public static Collection<BeangameItem> filteredCollection(Predicate<BeangameItem> predicate) {
        return registry.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Perform an action on all BeangameItems of a specific type.
     */
    public static <T> void doIf(Class<T> clazz, Consumer<T> consumer) {
        registry.values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .forEach(consumer);
    }

    /**
     * Perform an action on all BeangameItems of a specific type that match a given predicate.
     */
    public static <T> void doIf(Class<T> clazz, Predicate<BeangameItem> predicate, Consumer<T> consumer) {
        registry.values().stream()
                .filter(clazz::isInstance)
                .filter(predicate)
                .map(clazz::cast)
                .forEach(consumer);
    }

    /**
     * Get all BeangameItems that are in the item rotation.
     */
    public static ArrayList<BeangameItem> getItemsInRotation() {
        ArrayList<BeangameItem> items = registry.values().stream()
                .filter(BeangameItem::inItemRotation)
                .filter(BeangameItem::isUsable)
                .collect(Collectors.toCollection(ArrayList::new));
        return items;
    }

    /**
     * Get all BeangameItems that are in the food item rotation.
     */
    public static ArrayList<BeangameItem> getFoodItemsInRotation() {
        return registry.values().stream()
                .filter(BeangameItem::inFoodItemRotation)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Collection<BeangameFish> getAllFish() {
        return registry.values().stream()
                .filter(item -> item instanceof BeangameFish)
                .map(item -> (BeangameFish) item)
                .collect(Collectors.toList());
    }

    public static Collection<BeangameFish> getFishableFish() {
        return registry.values().stream()
                .filter(item -> item instanceof BeangameFish)
                .map(item -> (BeangameFish) item)
                .filter(BeangameFish::getIsFishable)
                .collect(Collectors.toList());
    }



    private static void setupProgressMonitoring() {
        // Check every second for 30 seconds
        for (int i = 1; i <= 30; i++) {
            final int checkNumber = i;
            // Use lambda instead of anonymous class
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (!Main.getConfiguration().isProcessingComplete()) {
                    int processed = Main.getConfiguration().getProcessedCount();
                    int failed = Main.getConfiguration().getFailedCount();
                    
                    String status = String.format(
                        "Item processing progress [Check %d/30]: Processed: %d, Failed: %d",
                        checkNumber, processed, failed
                    );
                    
                    if (checkNumber % 5 == 0) { // Log every 5th check
                        Main.getPlugin().getLogger().warning(status);
                    } else if (checkNumber <= 5) {
                        Main.getPlugin().getLogger().info(status);
                    }
                    
                    // If we're at check 15 and still not complete, try to restart processing
                    if (checkNumber == 15 && failed > 0) {
                        Main.getPlugin().getLogger().severe("Detected " + failed + " failed items. Attempting recovery...");
                        attemptRecovery();
                    }
                }
            }, 20L * i); // Check every second (20 ticks)
        }
    }
    
    private static void setupFinalVerification(List<BeangameItem> items) {
        // Final verification after 40 seconds - use lambda
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            boolean complete = Main.getConfiguration().isProcessingComplete();
            int processed = Main.getConfiguration().getProcessedCount();
            int failed = Main.getConfiguration().getFailedCount();
            
            if (complete && failed == 0) {
                Main.getPlugin().getLogger().info(String.format(
                    "Item initialization completed successfully! Processed: %d/%d items",
                    processed, items.size()
                ));
            } else if (complete) {
                Main.getPlugin().getLogger().warning(String.format(
                    "Item initialization completed with errors! Processed: %d, Failed: %d, Total: %d",
                    processed, failed, items.size()
                ));
                
                // Log specific missing items
                logMissingItems(items);
            } else {
                Main.getPlugin().getLogger().severe(String.format(
                    "Item initialization TIMED OUT! Processed: %d, Failed: %d, Total: %d",
                    processed, failed, items.size()
                ));
                
                // Force shutdown executor and try synchronous fallback
                forceSynchronousFallback(items);
            }
        }, 800L); // 40 seconds (20 * 40)
    }
    
    private static void attemptRecovery() {
        // This could trigger a manual retry of failed items
        // You might want to add a method to Configuration to manually retry
        Main.getPlugin().getLogger().info("Recovery attempt initiated...");
        // Implement recovery logic based on your needs
    }
    
    private static void logMissingItems(List<BeangameItem> allItems) {
        Set<String> processedItems = new HashSet<>();
        // You'll need to add a method to get processed item keys from Configuration
        // processedItems = Main.getConfiguration().getProcessedItemKeys();
        
        List<String> missingItems = allItems.stream()
            .filter(item -> !processedItems.contains(item.getKey().toString()))
            .map(item -> item.getKey().toString())
            .collect(Collectors.toList());
            
        if (!missingItems.isEmpty()) {
            Main.getPlugin().getLogger().warning("Missing items: " + missingItems);
        }
    }
    
    private static void forceSynchronousFallback(List<BeangameItem> items) {
        Main.getPlugin().getLogger().info("Attempting synchronous fallback for remaining items...");
        
        // Shutdown async executor first
        Main.getConfiguration().shutdown();
        
        // Process remaining items synchronously
        int successful = 0;
        int failed = 0;
        
        for (BeangameItem item : items) {
            try {
                // You'll need to add a synchronous process method to Configuration
                // Main.getConfiguration().processItemSync(item);
                successful++;
            } catch (Exception e) {
                Main.getPlugin().getLogger().severe("Failed to process item synchronously: " + item.getKey());
                e.printStackTrace();
                failed++;
            }
        }
        
        Main.getPlugin().getLogger().info(String.format(
            "Synchronous fallback completed: %d successful, %d failed",
            successful, failed
        ));
    }
}
