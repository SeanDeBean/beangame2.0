package com.beangamecore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.beangamecore.gamemodes.QuickCooldownsGamemode;

import java.util.*;

/**
 * BeangameModes - Gamemode management system with toggle interface
 * and per-gamemode disabled item lists.
 */
public class BeangameModes implements Listener {
    
    private final JavaPlugin plugin;
    private final Map<String, GameMode> gameModes;
    private final Map<UUID, String> playerViewing;
    private static final String INVENTORY_TITLE = ChatColor.DARK_GREEN + "Beangame Modes";
    
    /**
     * Represents a gamemode with toggle state and disabled items list
     */
    public static class GameMode {
        private final String id;
        private final String displayName;
        private final String description;
        private final Material iconEnabled;
        private final Material iconDisabled;
        private boolean enabled;
        private final List<String> disabledItems;
        
        /**
         * Create a gamemode with no disabled items
         */
        public GameMode(String id, String displayName, String description, 
                       Material iconEnabled, Material iconDisabled) {
            this(id, displayName, description, iconEnabled, iconDisabled, Collections.emptyList());
        }
        
        /**
         * Create a gamemode with specific disabled items
         */
        public GameMode(String id, String displayName, String description, 
                       Material iconEnabled, Material iconDisabled,
                       List<String> disabledItems) {
            this.id = id.toLowerCase();
            this.displayName = displayName;
            this.description = description;
            this.iconEnabled = iconEnabled;
            this.iconDisabled = iconDisabled;
            this.enabled = false;
            this.disabledItems = new ArrayList<>();
            
            // Add initial disabled items
            if (disabledItems != null) {
                for (String item : disabledItems) {
                    this.disabledItems.add(item.toLowerCase());
                }
            }
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Material getIcon() { return enabled ? iconEnabled : iconDisabled; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public void toggle() { this.enabled = !this.enabled; }
        
        /**
         * Get the list of disabled item keys for this gamemode
         */
        public List<String> getDisabledItems() { 
            return Collections.unmodifiableList(disabledItems); 
        }
        
        /**
         * Add an item to the disabled list
         */
        public void addDisabledItem(String itemKey) {
            String key = itemKey.toLowerCase();
            if (!disabledItems.contains(key)) {
                disabledItems.add(key);
            }
        }
        
        /**
         * Remove an item from the disabled list
         */
        public void removeDisabledItem(String itemKey) {
            disabledItems.remove(itemKey.toLowerCase());
        }
        
        /**
         * Check if an item is disabled in this gamemode
         */
        public boolean isItemDisabled(String itemKey) {
            return disabledItems.contains(itemKey.toLowerCase());
        }
        
        /**
         * Clear all disabled items
         */
        public void clearDisabledItems() {
            disabledItems.clear();
        }
        
        /**
         * Set the entire disabled items list
         */
        public void setDisabledItems(List<String> items) {
            disabledItems.clear();
            for (String item : items) {
                disabledItems.add(item.toLowerCase());
            }
        }
    }
    
    public BeangameModes(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gameModes = new HashMap<>();
        this.playerViewing = new HashMap<>();
        
        registerDefaultGameModes();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Register the two built-in gamemodes: randomizer and quick_cooldowns
     * with their default disabled items
     */
    private void registerDefaultGameModes() {
        // Randomizer gamemode
        registerGameMode(new GameMode(
            "randomizer",
            ChatColor.LIGHT_PURPLE + "Randomizer",
            ChatColor.GRAY + "Randomizes item drops",
            Material.CHEST,
            Material.CHEST
        ));
        
        // Quick Cooldowns gamemode
        registerGameMode(new GameMode(
            "quick_cooldowns",
            ChatColor.AQUA + "Quick Cooldowns",
            ChatColor.GRAY + "Reduces all ability cooldowns by " + Math.round((1 - QuickCooldownsGamemode.getMultiplier()) * 100) + "%",
            Material.CLOCK,
            Material.CLOCK
        ));

        // Juggernaut gamemode
        // registerGameMode(new GameMode(
        //     "juggernaut",
        //     ChatColor.DARK_RED + "Juggernaut",
        //     ChatColor.GRAY + "Makes 1 player a juggernaut",
        //     Material.DIAMOND_SWORD,
        //     Material.DIAMOND_SWORD,
        //     Arrays.asList(
        //         "beangame:blueshell"
        //     )
        // ));

        // lava rises gamemode
        // registerGameMode(new GameMode(
        //     "lava_rises",
        //     ChatColor.RED + "Lava Rises",
        //     ChatColor.GRAY + "Lava will begin rising in final circle",
        //     Material.LAVA_BUCKET,
        //     Material.BUCKET,
        //     Arrays.asList(
        //         "beangame:angelicshield",
        //         "beangame:dweamsword",
        //         "beangame:obsidianskull"
        //     )
        // ));


        registerGameMode(new GameMode(
            "brangame",
            ChatColor.YELLOW + "Brangame",
            ChatColor.GRAY + "All base items are bread",
            Material.BREAD,
            Material.WHEAT
        ));

    }
    
    /**
     * Register a custom gamemode
     */
    public void registerGameMode(GameMode gameMode) {
        gameModes.put(gameMode.getId(), gameMode);
    }
    
    /**
     * Open the gamemodes inventory for a player
     */
    public void openInventory(Player player) {
        int size = Math.max(9, ((gameModes.size() / 9) + 1) * 9);
        size = Math.min(size, 54);
        
        Inventory inv = Bukkit.createInventory(null, size, INVENTORY_TITLE);
        
        int slot = 0;
        for (GameMode gm : gameModes.values()) {
            if (slot >= size) break;
            inv.setItem(slot++, createGameModeItem(gm));
        }
        
        // Fill remaining with glass
        ItemStack filler = createFiller();
        for (int i = slot; i < size; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
        
        playerViewing.put(player.getUniqueId(), "main");
        player.openInventory(inv);
    }
    
    /**
     * Create the display item for a gamemode
     */
    private ItemStack createGameModeItem(GameMode gm) {
        ItemStack item = new ItemStack(gm.getIcon());
        ItemMeta meta = item.getItemMeta();
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.WHITE + gm.getDescription());
        lore.add("");
        
        // Show disabled items count if any
        int disabledCount = gm.getDisabledItems().size();
        if (disabledCount > 0) {
            lore.add(ChatColor.GRAY + "Disabled items: " + ChatColor.RED + disabledCount);
            // Optionally show first few disabled items
            List<String> items = gm.getDisabledItems();
            int showCount = Math.min(items.size(), 3);
            for (int i = 0; i < showCount; i++) {
                lore.add(ChatColor.DARK_GRAY + " • " + items.get(i));
            }
            if (items.size() > 3) {
                lore.add(ChatColor.DARK_GRAY + " • ... and " + (items.size() - 3) + " more");
            }
            lore.add("");
        }
        
        if (gm.isEnabled()) {
            lore.add(ChatColor.GREEN + "✔ ENABLED");
            lore.add(ChatColor.GRAY + "Click to disable");
            meta.setDisplayName(gm.getDisplayName() + ChatColor.GREEN + " [ON]");
            meta.setEnchantmentGlintOverride(true);
        } else {
            lore.add(ChatColor.RED + "✘ DISABLED");
            lore.add(ChatColor.GRAY + "Click to enable");
            meta.setDisplayName(gm.getDisplayName() + ChatColor.RED + " [OFF]");
            meta.setEnchantmentGlintOverride(false);
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createFiller() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
    
    // ==================== API METHODS ====================
    
    public GameMode getGameMode(String id) {
        return gameModes.get(id.toLowerCase());
    }
    
    public boolean isEnabled(String id) {
        GameMode gm = getGameMode(id);
        return gm != null && gm.isEnabled();
    }
    
    public void setEnabled(String id, boolean enabled) {
        GameMode gm = getGameMode(id);
        if (gm != null) gm.setEnabled(enabled);
    }
    
    public boolean toggle(String id) {
        GameMode gm = getGameMode(id);
        if (gm != null) {
            gm.toggle();
            return gm.isEnabled();
        }
        return false;
    }
    
    public Collection<GameMode> getAllGameModes() {
        return Collections.unmodifiableCollection(gameModes.values());
    }
    
    public List<String> getEnabledGameModes() {
        List<String> enabled = new ArrayList<>();
        for (GameMode gm : gameModes.values()) {
            if (gm.isEnabled()) enabled.add(gm.getId());
        }
        return enabled;
    }
    
    public boolean isItemDisabledInAnyEnabledMode(String itemKey) {
        for (GameMode gm : gameModes.values()) {
            if (gm.isEnabled() && gm.isItemDisabled(itemKey)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isItemDisabled(String gamemodeId, String itemKey) {
        GameMode gm = getGameMode(gamemodeId);
        return gm != null && gm.isItemDisabled(itemKey);
    }
    
    public void addDisabledItem(String gamemodeId, String itemKey) {
        GameMode gm = getGameMode(gamemodeId);
        if (gm != null) gm.addDisabledItem(itemKey);
    }
    
    public void removeDisabledItem(String gamemodeId, String itemKey) {
        GameMode gm = getGameMode(gamemodeId);
        if (gm != null) gm.removeDisabledItem(itemKey);
    }
    
    public void setDisabledItems(String gamemodeId, List<String> itemKeys) {
        GameMode gm = getGameMode(gamemodeId);
        if (gm != null) gm.setDisabledItems(itemKeys);
    }
    
    public List<String> getDisabledItems(String gamemodeId) {
        GameMode gm = getGameMode(gamemodeId);
        return gm != null ? gm.getDisabledItems() : Collections.emptyList();
    }
    
    // ==================== EVENT HANDLERS ====================
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(INVENTORY_TITLE)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        
        int slot = event.getSlot();
        GameMode[] modes = gameModes.values().toArray(new GameMode[0]);
        
        if (slot < modes.length) {
            GameMode gm = modes[slot];
            boolean newState = !gm.isEnabled();
            gm.setEnabled(newState);
            
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, () -> openInventory(player));
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        playerViewing.remove(event.getPlayer().getUniqueId());
    }


}