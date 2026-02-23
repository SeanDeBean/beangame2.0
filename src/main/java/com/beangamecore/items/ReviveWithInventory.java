package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.commands.BeangameStart;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.ItemNBT;
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

public class ReviveWithInventory extends BeangameItem implements BGRClickableI {

    public void reviveInventoryClick(InventoryClickEvent event){
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();
        if(event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)){
            int status = 0;
            int i = 0;
            ItemStack items[] = { player.getEquipment().getItemInMainHand(), player.getEquipment().getItemInOffHand() };
            for(ItemStack item : items){
                i++;
                if(ItemNBT.hasBeanGameTag(item) && ItemNBT.isBeanGame(item, this.getKey())){
                    status = i;
                }
            }
            switch (status) {
                case 1:
                    player.getEquipment().setItemInMainHand(new ItemStack(Material.AIR, 1));
                    player.closeInventory();
                    break;
                case 2:
                    player.getEquipment().setItemInOffHand(new ItemStack(Material.AIR, 1));
                    player.closeInventory();
                    break;
                default:
                    return;
            }

            // respawn the owner of the head & remove revive item from inventory
            ItemStack skull = event.getCurrentItem();
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if(!skullMeta.getOwningPlayer().isOnline()){
                return;
            }
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0);

            Player owner = (Player)skullMeta.getOwningPlayer();
            owner.teleport(player);
            owner.getInventory().clear();
            owner.setGameMode(GameMode.SURVIVAL);
            if(!BeangameStart.alivePlayers.contains(owner.getUniqueId())){
                BeangameStart.alivePlayers.add(owner.getUniqueId());
            }
            owner.getInventory().setContents(player.getInventory().getContents());
        } else {
            return;
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // inventory creation
        Inventory reviveinv = createReviveInventory();
        event.getPlayer().openInventory(reviveinv);
        return true;
    }

    private Inventory createReviveInventory() {
        Inventory reviveinv = Bukkit.createInventory(null, 54, "§dRevive With Inventory!");
        ItemStack backgroundglass = createBackgroundGlassPane();
        int slot = fillReviveInventoryWithPlayers(reviveinv, backgroundglass);
        fillRemainingSlotsWithGlass(reviveinv, backgroundglass, slot);
        return reviveinv;
    }

    private ItemStack createBackgroundGlassPane() {
        ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundglassmeta = backgroundglass.getItemMeta();
        backgroundglassmeta.setHideTooltip(true);
        backgroundglass.setItemMeta(backgroundglassmeta);
        return backgroundglass;
    }

    private int fillReviveInventoryWithPlayers(Inventory reviveinv, ItemStack backgroundglass) {
        int slot = 0;
        for (Player players : Bukkit.getOnlinePlayers()) {
            UUID uuid = players.getUniqueId();
            if (isPlayerEligibleForRevive(players, uuid) && slot < 54) {
                ItemStack skull = createPlayerSkull(players);
                reviveinv.setItem(slot, skull);
                slot++;
            }
        }
        return slot;
    }

    private boolean isPlayerEligibleForRevive(Player players, UUID uuid) {
        return players.getGameMode() == GameMode.SPECTATOR && !Revive.noRevive.contains(uuid);
    }

    private ItemStack createPlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§dRevive " + player.getDisplayName());
        skull.setItemMeta(skullMeta);
        return skull;
    }

    private void fillRemainingSlotsWithGlass(Inventory reviveinv, ItemStack backgroundglass, int startSlot) {
        if (startSlot < 54) {
            for (int i = startSlot; i < 54; i++) {
                reviveinv.setItem(i, backgroundglass);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "revivewithinventory";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§dRevive With Inventory";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to open revive menu.",
            "§aClick a player head to revive them",
            "§aat your location with your entire",
            "§ainventory. Consumes the item and",
            "§acopies all of your items to them.",
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
        return Material.PUFFERFISH_BUCKET;
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

