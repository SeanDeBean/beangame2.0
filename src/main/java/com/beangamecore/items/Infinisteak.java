package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class Infinisteak extends BeangameItem implements BGConsumableI {

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
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "infinisteak";
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
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " S ", " F ", " B ", r.mCFromMaterial(Material.COOKED_BEEF), r.eCFromBeangame(Key.bg("fifteenshrimp")), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§dInfinisteak";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2A magical steak that replenishes",
            "§2itself after being consumed. Never",
            "§2runs out of delicious sustenance.",
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
        return Material.COOKED_BEEF;
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

