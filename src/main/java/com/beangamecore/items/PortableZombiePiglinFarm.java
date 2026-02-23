package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class PortableZombiePiglinFarm extends BeangameItem implements BGLPTalismanI, BGRClickableI {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        applyGoldIngotEffect(player);
        applyGoldNuggetEffect(player);
    }

    private void applyGoldIngotEffect(Player player) {
        if (Math.random() <= 0.12) {
            giveOrDropItem(player, new ItemStack(Material.GOLD_INGOT, 1));
        }
    }

    private void applyGoldNuggetEffect(Player player) {
        if (Math.random() <= 0.12) {
            giveOrDropItem(player, new ItemStack(Material.GOLD_NUGGET, 1));
        }
    }

    private void giveOrDropItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "portablezombiepiglinfarm";
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
        return "§fPortable Zombie Piglin Farm";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Has a 12% chance every second to",
            "§3generate gold ingots and a 12% chance",
            "§3to generate gold nuggets while held.",
            "§3Provides steady gold supply without",
            "§3nether mob farming.",
            "",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.EXPERIENCE_BOTTLE;
    }

    @Override
    public int getCustomModelData() {
        return 105;
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
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        return true;
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

