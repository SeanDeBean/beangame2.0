package com.beangamecore.items;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;

public class Lunchly extends BeangameItem implements BGConsumableI {

    @Override
    public void onConsume(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem().clone();
        Player player = event.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20, 2, false, false));
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
        return "lunchly";
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
        return "§bLunchly";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2A magical lunchable that",
            "§2replenishes itself after being",
            "§2consumed. Though, its been",
            "§2on that shelf for a long time...",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_CARROT;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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
