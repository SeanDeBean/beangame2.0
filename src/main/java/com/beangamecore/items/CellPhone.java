package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CellPhone extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        event.setCancelled(true);
        Inventory cellphoneinv = createCellphoneInventory(player);
        event.getPlayer().openInventory(cellphoneinv);
        return true;
    }

    private Inventory createCellphoneInventory(Player player) {
        Inventory cellphoneinv = Bukkit.createInventory(null, 54, "§9Cell Phone!");
        ItemStack backgroundglass = createBackgroundGlass();
        populateCellphoneInventory(cellphoneinv, player, backgroundglass);
        return cellphoneinv;
    }

    private ItemStack createBackgroundGlass() {
        ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundglassmeta = backgroundglass.getItemMeta();
        backgroundglassmeta.setHideTooltip(true);
        backgroundglass.setItemMeta(backgroundglassmeta);
        return backgroundglass;
    }

    private void populateCellphoneInventory(Inventory cellphoneinv, Player player, ItemStack backgroundglass) {
        int slot = 0;
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (shouldAddPlayerToCellphoneInv(players, player, slot)) {
                ItemStack skull = createPlayerSkull(players);
                cellphoneinv.setItem(slot, skull);
                slot++;
            }
        }
        fillRemainingSlotsWithBackground(cellphoneinv, slot, backgroundglass);
    }

    private ItemStack createPlayerSkull(Player players) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(players);
        skullMeta.setDisplayName("§9Call " + players.getDisplayName());
        skull.setItemMeta(skullMeta);
        return skull;
    }

    private void fillRemainingSlotsWithBackground(Inventory cellphoneinv, int slot, ItemStack backgroundglass) {
        if (slot < 54) {
            for (int i = slot; i < 54; i++) {
                cellphoneinv.setItem(i, backgroundglass);
            }
        }
    }

    private boolean shouldAddPlayerToCellphoneInv(Player candidate, Player currentPlayer, int slot) {
        return candidate.getGameMode() == GameMode.SURVIVAL
                && candidate.getUniqueId() != currentPlayer.getUniqueId()
                && slot < 54;
    }

    public void cellphoneInventoryClick(InventoryClickEvent event){
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        if(event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)){
            // respawn the owner of the head & start cellphone cooldown
            ItemStack skull = event.getCurrentItem();
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if(!skullMeta.getOwningPlayer().isOnline()){
                return;
            } else if(!skullMeta.getOwningPlayer().getPlayer().getGameMode().equals(GameMode.SURVIVAL)){
                return;
            }
            UUID uuid = event.getWhoClicked().getUniqueId();
            Player owner = (Player)skullMeta.getOwningPlayer();
            applyCooldown(uuid);
            // warns owner
            player.closeInventory();
            for(int i = 1; i < 4; i++){
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                    public void run(){
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 1.0F);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9Ringing!"));
                        owner.getWorld().playSound(owner.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 1.0F);
                        owner.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9You are receiving a call from " + player.getDisplayName() + "!"));
                    }
                }, 20*i);
            }
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
                public void run(){
                    new CellPhone().cellphoneOwnerOpenMenu(owner, player);
                }
            }, 60L);
        } else {
            return;
        }
    }

    public void cellphonereceiveInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem().getType().equals(Material.RED_CONCRETE)) {
            handleRedConcreteClick(event, player);
        } else if (event.getCurrentItem().getType().equals(Material.LIME_CONCRETE)) {
            handleLimeConcreteClick(event, player);
        } else {
            return;
        }
    }

    private void handleRedConcreteClick(InventoryClickEvent event, Player player) {
        // denies the phone call
        player.closeInventory();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9Sent to voicemail!"));
        ItemStack skull = event.getInventory().getItem(13);
        if (!skull.getType().equals(Material.PLAYER_HEAD)) {
            return;
        }
        SkullMeta skullmeta = (SkullMeta) skull.getItemMeta();
        Player caller = (Player) skullmeta.getOwningPlayer();
        if (caller.isOnline()) {
            caller.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§9" + player.getName() + " sent you to voicemail!"));
        }
    }

    private void handleLimeConcreteClick(InventoryClickEvent event, Player player) {
        // accepts the phone call
        player.closeInventory();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9Call accepted!"));
        ItemStack skull = event.getInventory().getItem(13);
        if (!skull.getType().equals(Material.PLAYER_HEAD)) {
            return;
        }
        SkullMeta skullmeta = (SkullMeta) skull.getItemMeta();
        Player caller = (Player) skullmeta.getOwningPlayer();
        if (caller.isOnline()) {
            caller.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§9" + player.getName() + " accepted your phone call!"));
        }
        player.teleport(caller);
    }
    
    public void cellphoneOwnerOpenMenu(Player player, Player caller) {
        Inventory cellphoneinv = createCellphoneInventory();
        ItemStack backgroundglass = createBackgroundGlass();
        ItemStack decline = createDeclineItem();
        ItemStack accept = createAcceptItem();
        ItemStack skull = createCallerSkull(caller);

        populateInventory(cellphoneinv, backgroundglass, decline, accept, skull, caller);

        player.openInventory(cellphoneinv);
    }

    private Inventory createCellphoneInventory() {
        // inventory creation
        return Bukkit.createInventory(null, 27, "§9Cell Phone Ringing!");
    }

    private ItemStack createDeclineItem() {
        ItemStack decline = new ItemStack(Material.RED_CONCRETE);
        ItemMeta declinemeta = decline.getItemMeta();
        declinemeta.setDisplayName(String.valueOf(ChatColor.RED) + "Decline");
        decline.setItemMeta(declinemeta);
        return decline;
    }

    private ItemStack createAcceptItem() {
        ItemStack accept = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta acceptmeta = accept.getItemMeta();
        acceptmeta.setDisplayName(String.valueOf(ChatColor.GREEN) + "Accept");
        accept.setItemMeta(acceptmeta);
        return accept;
    }

    private ItemStack createCallerSkull(Player caller) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(caller);
        skullMeta.setDisplayName("§9" + caller.getDisplayName() + " is calling!");
        skull.setItemMeta(skullMeta);
        return skull;
    }

    private void populateInventory(Inventory cellphoneinv, ItemStack backgroundglass, ItemStack decline,
            ItemStack accept, ItemStack skull, Player caller) {
        // Fill positions 0 to 3 with decline
        populateSection(cellphoneinv, decline, 0, 3);
        // Fill position 4 with backgroundglass
        populateSection(cellphoneinv, backgroundglass, 4, 4);
        // Fill positions 5 to 8 with accept
        populateSection(cellphoneinv, accept, 5, 8);
        // Fill positions 9 to 12 with decline
        populateSection(cellphoneinv, decline, 9, 12);
        // Fill position 13 with skull
        populateSection(cellphoneinv, skull, 13, 13);
        // Fill positions 14 to 17 with accept
        populateSection(cellphoneinv, accept, 14, 17);
        // Fill positions 18 to 21 with decline
        populateSection(cellphoneinv, decline, 18, 21);
        // Fill position 22 with backgroundglass
        populateSection(cellphoneinv, backgroundglass, 22, 22);
        // Fill positions 23 to 26 with accept
        populateSection(cellphoneinv, accept, 23, 26);
    }

    private void populateSection(Inventory inventory, ItemStack item, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {
            inventory.setItem(i, item);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 75000L;
    }

    @Override
    public String getId() {
        return "cellphone";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "ICI", "WRS", "IBI", r.mCFromMaterial(Material.IRON_INGOT), r.eCFromBeangame(Key.bg("lagconjurer")), r.eCFromBeangame(Key.bg("walkietalkie")), r.mCFromMaterial(Material.GLASS_PANE), r.eCFromBeangame(Key.bg("stopwatch")), r.mCFromMaterial(Material.REDSTONE));
        return null;
    }

    @Override
    public String getName() {
        return "§9Cell Phone";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to open a menu showing all",
            "§aonline survival players. Call another",
            "§aplayer to teleport them to your location",
            "§aif they accept the call.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.COPPER_INGOT;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

