package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class ChorusBomb extends BeangameItem implements BGConsumableI {

    @Override
    public void onConsume(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem().clone();
        Player player = event.getPlayer();
        player.getLocation().getWorld().spawn(player.getLocation(), TNTPrimed.class, tnt -> {
            tnt.setFuseTicks(20 * 5);
            tnt.setSource(player);
        });
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
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "chorusbomb";
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
        return "§dChorus Bomb";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2When eaten, spawns a lit TNT with",
            "§25 second fuse at your location before",
            "§2triggering the normal chorus fruit",
            "§2teleportation effect.",
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
        return Material.CHORUS_FRUIT;
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

