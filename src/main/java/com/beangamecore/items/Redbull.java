package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.items.type.damage.BGDamageInvI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.ItemNBT;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class Redbull extends BeangameItem implements BGConsumableI, BGDamageInvI {

    @Override
    public void onDamageInventory(EntityDamageEvent event, ItemStack item){
        if(event.getCause().equals(DamageCause.FALL) && item.getItemMeta().getCustomModelData() == 102){
            event.getEntity().getWorld().spawnParticle(Particle.CLOUD, event.getEntity().getLocation(), 2);
            event.setCancelled(true);
        }
    }

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack original = event.getItem();

        restoreOffhandItem(player, original);

        restoreHotbarItem(player, original);

        toggleBeanGameItemModelData(player);
    }

    private void restoreOffhandItem(Player player, ItemStack original) {
        // Restore offhand item if consumed from there
        if (original.equals(player.getEquipment().getItemInOffHand())) {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> player.getEquipment().setItemInOffHand(original),
                    1L);
        }
    }

    private void restoreHotbarItem(Player player, ItemStack original) {
        // Restore hotbar item if consumed from hotbar
        for (int i = 0; i < 9; i++) {
            if (original.equals(player.getInventory().getItem(i))) {
                final int slot = i;
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(),
                        () -> player.getInventory().setItem(slot, original), 1L);
            }
        }
    }

    private void toggleBeanGameItemModelData(Player player) {
        // Delay to allow inventory updates to settle
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            processPlayerInventory(player);
        }, 2L);
    }

    private void processPlayerInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null)
                continue;

            processInventoryItem(item);
        }
    }

    private void processInventoryItem(ItemStack item) {
        if (ItemNBT.hasBeanGameTag(item) && ItemNBT.isBeanGame(item, this.getKey())) {
            if (BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(item)) instanceof BeangameSoftItem)
                return;

            ItemMeta meta = item.getItemMeta();
            if (meta == null)
                return;

            toggleItemModelData(item, meta);
        }
    }

    private void toggleItemModelData(ItemStack item, ItemMeta meta) {
        int cmd = meta.getCustomModelData();
        // Toggle model data
        meta.setCustomModelData(cmd == 101 ? 102 : 101);
        item.setItemMeta(meta);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "redbull";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public boolean isInFoodItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§cRedbull";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Consume to toggle between active",
            "§2and inactive states. When active,",
            "§2grants complete fall damage immunity.",
            "§2Item replenishes after consumption.",
            "",
            "§2Food",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.CARROT;
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

