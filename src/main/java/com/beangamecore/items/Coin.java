package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;

public class Coin extends BeangameItem implements BGRClickableI, BeangameSoftItem {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location playerLocation = player.getLocation();
        if(onCooldown(event.getPlayer().getUniqueId())){
            return false;
        }
        applyCooldown(event.getPlayer().getUniqueId());

        String result = Math.random() < 0.5 ? "Heads" : "Tails";
        String message = ChatColor.GOLD + result;

        for (Player nearby : world.getPlayers()) {
            if (nearby.getLocation().distance(playerLocation) <= 16) {
                nearby.sendMessage(message);
            }
        }

        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 1000;
    }

    @Override
    public String getId() {
        return "coin";
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
        return "§6Coin";
    }

    @Override
    public List<String> getLore() {
        return List.of("§9§obeangame");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.SUNFLOWER;
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
        return 99;
    }
}

