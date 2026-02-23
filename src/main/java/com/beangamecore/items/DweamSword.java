package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class DweamSword extends BeangameItem implements BGRClickableI, BGDDealerHeldI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack itemStack){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        // inventory creation
        Inventory dweamswordinv = Bukkit.createInventory(null, 27, "§aDweam sword!");
        ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundglassmeta = backgroundglass.getItemMeta();
        backgroundglassmeta.setHideTooltip(true);
        backgroundglass.setItemMeta(backgroundglassmeta);
        for (int i : new int[] { 0, 9, 18, 8, 17, 26 }) {
            dweamswordinv.setItem(i, backgroundglass);
        }
        dweamswordinv.setItem(1, new ItemStack(Material.GLASS_BOTTLE, 9));
        dweamswordinv.setItem(2, new ItemStack(Material.WATER_BUCKET, 1));
        dweamswordinv.setItem(3, new ItemStack(Material.BLAZE_POWDER, 2));
        dweamswordinv.setItem(4, new ItemStack(Material.BREWING_STAND));
        dweamswordinv.setItem(5, new ItemStack(Material.CHORUS_FRUIT, 3));
        dweamswordinv.setItem(6, new ItemStack(Material.DRAGON_BREATH));
        dweamswordinv.setItem(7, new ItemStack(Material.FERMENTED_SPIDER_EYE));
        dweamswordinv.setItem(10, new ItemStack(Material.GHAST_TEAR));
        dweamswordinv.setItem(11, new ItemStack(Material.GLISTERING_MELON_SLICE));
        dweamswordinv.setItem(12, new ItemStack(Material.SLIME_BLOCK));
        dweamswordinv.setItem(13, new ItemStack(Material.GOLDEN_CARROT));
        dweamswordinv.setItem(14, new ItemStack(Material.STONE));
        dweamswordinv.setItem(15, new ItemStack(Material.MAGMA_CREAM));
        dweamswordinv.setItem(16, new ItemStack(Material.NETHER_WART));
        dweamswordinv.setItem(19, new ItemStack(Material.BREEZE_ROD));
        dweamswordinv.setItem(20, new ItemStack(Material.PUFFERFISH));
        dweamswordinv.setItem(21, new ItemStack(Material.RABBIT_FOOT));
        dweamswordinv.setItem(22, new ItemStack(Material.REDSTONE));
        dweamswordinv.setItem(23, new ItemStack(Material.COBWEB));
        dweamswordinv.setItem(24, new ItemStack(Material.SUGAR));
        dweamswordinv.setItem(25, new ItemStack(Material.TURTLE_HELMET));
        player.openInventory(dweamswordinv);
        return true;
    }

    public void dweamswordInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (isHandledSlot(event.getRawSlot())) {
            handleItemTransfer(event, player);
            // prevents dupes
            event.getCurrentItem().setType(Material.AIR);
            player.closeInventory();
            applyCooldown(player.getUniqueId());
        }
    }

    private boolean isHandledSlot(int slot) {
        return isSlotInHandledRange(slot) || isSlotInHandledList(slot);
    }

    private boolean isSlotInHandledRange(int slot) {
        // Slots 1-7 are handled
        return slot >= 1 && slot <= 7;
    }

    private boolean isSlotInHandledList(int slot) {
        // Slots 10-16 and 19-25 are handled
        return (slot >= 10 && slot <= 16) || (slot >= 19 && slot <= 25);
    }

    private void handleItemTransfer(InventoryClickEvent event, Player player) {
        if (player.getInventory().firstEmpty() != -1) {
            assert event.getCurrentItem() != null;
            player.getInventory().addItem(new ItemStack[] {
                    new ItemStack(event.getCurrentItem())
            });
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), event.getCurrentItem());
        }
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack stack){
        if(!(event.getEntity() instanceof Player) && Math.random() <= 0.2){
            LivingEntity attacker = (LivingEntity) event.getDamager();
            attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 0.65f, 0.9f);
            if(attacker instanceof Player p){
                if(p.getInventory().firstEmpty() != -1){
                    p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                } else {
                    p.getWorld().dropItemNaturally(attacker.getLocation(), new ItemStack(Material.ENDER_PEARL));
                }
            } else {
                attacker.getWorld().dropItemNaturally(attacker.getLocation(), new ItemStack(Material.ENDER_PEARL));
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 30000L;
    }

    @Override
    public String getId() {
        return "dweamsword";
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
        return "§aDweam Sword";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to open a menu with various",
            "§apotion-making resources. Hitting mobs",
            "§ahas a 20% chance to drop ender pearls.",
            "§aProvides convenient access to brewing",
            "§aingredients and combat utilities.",
            "",
            "§aSupport",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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

