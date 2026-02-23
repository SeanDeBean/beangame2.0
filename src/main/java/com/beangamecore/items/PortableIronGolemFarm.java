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

public class PortableIronGolemFarm extends BeangameItem implements BGLPTalismanI, BGRClickableI {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        applyIronIngotEffect(player);
        applyPoppyEffect(player);
    }

    private void applyIronIngotEffect(Player player) {
        if (Math.random() <= 0.12) {
            giveItemOrDrop(player, new ItemStack(Material.IRON_INGOT, 1));
        }
    }

    private void applyPoppyEffect(Player player) {
        if (Math.random() <= 0.12) {
            giveItemOrDrop(player, new ItemStack(Material.POPPY, 1));
        }
    }

    private void giveItemOrDrop(Player player, ItemStack itemStack) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(itemStack);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "portableirongolemfarm";
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
        return "§7Portable Iron Golem Farm";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Has a 12% chance every second to",
            "§3generate iron ingots and a 12%",
            "§3chance to generate poppies while held.",
            "§3Provides steady iron and flower",
            "§3supply without golem farming.",
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
        return 103;
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

