package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class CosmicBrownie extends BeangameItem implements BGConsumableI {

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

        // item effect
        HashMap<UUID, List<CosmicCirclingStar>> stars = CrownOfTheCosmos.stars;
        if(!stars.containsKey(player.getUniqueId())) stars.put(player.getUniqueId(), new ArrayList<>());
        List<CosmicCirclingStar> s = stars.get(player.getUniqueId());
        if(s.size() < 6 && player.getGameMode() != GameMode.SPECTATOR){
            s.add(new CosmicCirclingStar(player));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_THROWN, 1, 0.5F);
        }
        CrownOfTheCosmos.stars = stars;

    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "cosmicbrownie";
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
        return "§5Cosmic Brownie";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2When eaten, summons a cosmic circling",
            "§2star that orbits around you. Can have",
            "§2up to 6 stars active at once. Left-click",
            "§2to throw stars as projectiles.",
            "",
            "§2Food",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.COOKED_MUTTON;
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

