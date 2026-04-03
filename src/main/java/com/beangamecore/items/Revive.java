package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.commands.BeangameStart;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
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

public class Revive extends BeangameItem implements BGRClickableI {

    public static List<UUID> noRevive = new ArrayList<>();

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
            Player owner = (Player)skullMeta.getOwningPlayer();
            owner.teleport(player);
            owner.getInventory().clear();
            owner.setGameMode(GameMode.SURVIVAL);
            BeangameStart.alivePlayers.add(owner.getUniqueId());
            ArrayList<BeangameItem> foods = BeangameItemRegistry.getFoodItemsInRotation();
            int rindex = (int)Math.round(Math.random() * (foods.size()-1));
            ItemStack food = foods.get(rindex).asItem();
            owner.getInventory().addItem(food);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0);
            Main.getPlugin().getLevelingSystem().onRevive(player);
        } else {
            return;
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // inventory creation
        Inventory reviveinv = Bukkit.createInventory(null, 54, "§dRevive!");
        ItemStack backgroundglass = createBackgroundGlass();
        int slot = addRevivablePlayersToInventory(reviveinv, slot = 0);
        fillRemainingSlotsWithBackground(reviveinv, backgroundglass, slot);
        event.getPlayer().openInventory(reviveinv);
        return true;
    }

    private ItemStack createBackgroundGlass() {
        ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundglassmeta = backgroundglass.getItemMeta();
        backgroundglassmeta.setHideTooltip(true);
        backgroundglass.setItemMeta(backgroundglassmeta);
        return backgroundglass;
    }

    private int addRevivablePlayersToInventory(Inventory reviveinv, int slot) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            UUID uuid = players.getUniqueId();
            if (isPlayerRevivable(players, slot, uuid)) {
                ItemStack skull = createPlayerSkull(players);
                reviveinv.setItem(slot, skull);
                slot++;
            }
        }
        return slot;
    }

    private ItemStack createPlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§dRevive " + player.getDisplayName());
        skull.setItemMeta(skullMeta);
        return skull;
    }

    private void fillRemainingSlotsWithBackground(Inventory reviveinv, ItemStack backgroundglass, int startSlot) {
        if (startSlot < 54) {
            for (int i = startSlot; i < 54; i++) {
                reviveinv.setItem(i, backgroundglass);
            }
        }
    }

    private boolean isPlayerRevivable(Player players, int slot, UUID uuid) {
        return players.getGameMode() == GameMode.SPECTATOR && slot < 54 && !noRevive.contains(uuid);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "revive";
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
        return "§dRevive";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to open revive menu.",
            "§aClick a player head to revive them",
            "§aat your location. Consumes the item",
            "§aand gives revived player random food.",
            "§aCannot revive yourself.",
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

