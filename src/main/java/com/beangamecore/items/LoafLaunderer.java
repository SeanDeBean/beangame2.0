package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.damage.BGDamageHeldI;
import com.beangamecore.util.BlockCategories;

public class LoafLaunderer extends BeangameItem implements BGConsumableI, BGDamageHeldI {

    @Override
    public void onDamageHeldItem(EntityDamageEvent event, ItemStack item) {
        if(event.getCause().equals(DamageCause.SUFFOCATION) && isInsideBorder(event.getEntity())){
            event.setCancelled(true);
        }
    }

    public boolean isInsideBorder(Entity entity) {
        WorldBorder border = entity.getWorld().getWorldBorder();
        Location center = border.getCenter();
        double size = border.getSize() / 2.0; // Size is diameter, we need radius
        
        double x = entity.getLocation().getX() - center.getX();
        double z = entity.getLocation().getZ() - center.getZ();
        
        // Check if within square border (vanilla default)
        return Math.abs(x) < size && Math.abs(z) < size;
    }

    @Override
    public void onConsume(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem().clone();
        Player player = event.getPlayer();

        if(item.equals(player.getEquipment().getItemInOffHand())){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> 
                player.getEquipment().setItemInOffHand(item)
            , 1);
        }
        for(int i = 0; i < 9; i++){
            if(item.equals(player.getInventory().getItem(i))){
                final int j = i;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> 
                    player.getInventory().setItem(j, item)
                , 1);
            }
        }

        // create cube
        spawnWheat(player.getLocation());

    }

    private void spawnWheat(Location center) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    transformBlockIfNotFunctional(center, x, y, z);
                }
            }
        }
    }

    private void transformBlockIfNotFunctional(Location center, int x, int y, int z) {
        Location blockLocation = center.clone().add(x, y, z);
        Block block = blockLocation.getBlock();
        Material type = block.getType();
        if (!BlockCategories.getFunctionalBlocks().contains(type)) {
            block.setType(Material.HAY_BLOCK);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "loaflaunderer";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public boolean isInFoodItemRotation(){
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§6Loaf Launderer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Consume to create a 3x3x3 cube of",
            "§2hay blocks around you. Prevents",
            "§2suffocation damage while held.",
            "§2Bread replenishes after eating.",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BREAD;
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
