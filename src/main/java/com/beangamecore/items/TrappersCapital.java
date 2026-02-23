package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGHPTalismanI;
import com.beangamecore.util.Longs;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class TrappersCapital extends BeangameItem implements BGRClickableI, BGHPTalismanI {
    
    public void trapperscapitalInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (isValidItemSlot(event.getRawSlot())) {
            handleInventoryItemClick(event, player);
        }
    }

    private boolean isValidItemSlot(int slot) {
        return (slot >= 1 && slot <= 7) ||
                (slot >= 9 && slot <= 17) ||
                (slot >= 19 && slot <= 25);
    }

    private void handleInventoryItemClick(InventoryClickEvent event, Player player) {
        if (player.getInventory().firstEmpty() != -1) {
            addItemToInventory(event, player);
        } else {
            dropItemNaturally(event, player);
        }
        // prevents dupes
        removeClickedItem(event);
        player.closeInventory();
        UUID uuid = player.getUniqueId();
        long stackCount = Longs.getLong("trapperscapital_stacks", uuid);
        Longs.setLong("trapperscapital_stacks", uuid, stackCount - 1);
        stackCount--;
        World world = player.getWorld();
        Location center = world.getWorldBorder().getCenter();
        double x = Math.floor(center.getX());
        double z = Math.floor(center.getZ());
        if(stackCount == 1){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cWorld center -> " + x + ", " + z + "       " + stackCount + " stack"));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cWorld center -> " + x + ", " + z + "       " + stackCount + " stacks"));
        }
    }

    private void addItemToInventory(InventoryClickEvent event, Player player) {
        assert event.getCurrentItem() != null;
        player.getInventory().addItem(new ItemStack(event.getCurrentItem()));
    }

    private void dropItemNaturally(InventoryClickEvent event, Player player) {
        player.getWorld().dropItemNaturally(player.getLocation(), event.getCurrentItem());
    }

    private void removeClickedItem(InventoryClickEvent event) {
        event.getCurrentItem().setType(Material.AIR);
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        long stackCount = Longs.getLong("trapperscapital_stacks", uuid);
        if(stackCount >= 12){
            return;
        }
        if(!onCooldown(uuid)){
            Longs.setLong("trapperscapital_stacks", uuid, stackCount + 1);
            applyCooldown(uuid);
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        Location center = world.getWorldBorder().getCenter();
        double x = Math.floor(center.getX());
        double z = Math.floor(center.getZ());

        long stackCount = Longs.getLong("trapperscapital_stacks", uuid);
        if(stackCount == 1){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cWorld center -> " + x + ", " + z + "       " + stackCount + " stack"));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cWorld center -> " + x + ", " + z + "       " + stackCount + " stacks"));
        }
        
        if(stackCount <= 0){
            if (onCooldown(uuid)){
                sendCooldownMessage(player);
                return false;
            }
        }

        // inventory creation
        Inventory trapperscapitalinv = Bukkit.createInventory(null, 27, "§cTrapper's Capital!");
        ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundglassmeta = backgroundglass.getItemMeta();
        backgroundglassmeta.setHideTooltip(true);
        backgroundglass.setItemMeta(backgroundglassmeta);
        for (int i : new int[] { 0, 18, 8, 26 }) {
            trapperscapitalinv.setItem(i, backgroundglass);
        }
        trapperscapitalinv.setItem(1, new ItemStack(Material.GRASS_BLOCK, 16));
        trapperscapitalinv.setItem(2, new ItemStack(Material.REDSTONE_BLOCK, 3));
        trapperscapitalinv.setItem(3, new ItemStack(Material.REPEATER, 8));
        trapperscapitalinv.setItem(4, new ItemStack(Material.COMPARATOR, 8));
        trapperscapitalinv.setItem(5, new ItemStack(Material.TARGET, 8));
        trapperscapitalinv.setItem(6, new ItemStack(Material.SCULK_SENSOR, 8));
        trapperscapitalinv.setItem(7, new ItemStack(Material.TNT, 16));
        trapperscapitalinv.setItem(9, new ItemStack(Material.NETHERITE_PICKAXE, 1));
        trapperscapitalinv.setItem(10, new ItemStack(Material.WHITE_BANNER, 16));
        trapperscapitalinv.setItem(11, new ItemStack(Material.POINTED_DRIPSTONE, 12));
        trapperscapitalinv.setItem(12, new ItemStack(Material.TRAPPED_CHEST, 8));
        trapperscapitalinv.setItem(13, new ItemStack(Material.PISTON, 8));
        trapperscapitalinv.setItem(14, new ItemStack(Material.STICKY_PISTON, 8));
        trapperscapitalinv.setItem(15, new ItemStack(Material.SLIME_BLOCK, 8));
        trapperscapitalinv.setItem(16, new ItemStack(Material.HONEY_BLOCK, 8));
        trapperscapitalinv.setItem(17, new ItemStack(Material.NETHERITE_SHOVEL, 1));
        trapperscapitalinv.setItem(19, new ItemStack(Material.STONE, 16));
        trapperscapitalinv.setItem(20, new ItemStack(Material.DISPENSER, 8));
        trapperscapitalinv.setItem(21, new ItemStack(Material.HOPPER, 8));
        trapperscapitalinv.setItem(22, new ItemStack(Material.OBSERVER, 8));
        trapperscapitalinv.setItem(23, new ItemStack(Material.NOTE_BLOCK, 8));
        trapperscapitalinv.setItem(24, new ItemStack(Material.RAIL, 16));
        trapperscapitalinv.setItem(25, new ItemStack(Material.POWERED_RAIL, 16));
        player.openInventory(trapperscapitalinv);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 12000L;
    }

    @Override
    public String getId() {
        return "trapperscapital";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§cTrapper's Capital";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to access redstone components",
            "§aand view the final circle location.",
            "§aGains stacks over time while held.",
            "",
            "§5Tool",
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
        return Material.TARGET;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

