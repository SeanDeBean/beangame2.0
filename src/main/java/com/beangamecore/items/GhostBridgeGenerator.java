package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;

public class GhostBridgeGenerator extends BeangameItem implements BGLPTalismanI {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        if (Math.random() <= 0.18){
            BeangameItemRegistry.get(Key.bg("ghostbridge")).ifPresent(gb -> {
                ItemStack ghostbridge = gb.asItem();
                ghostbridge.setAmount(1);
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(ghostbridge);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), ghostbridge);
                }
            });
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "ghostbridgegenerator";
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
        return ChatColor.LIGHT_PURPLE+"Ghost Bridge Generator";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Has a 18% chance every second to",
            "§3generate Ghost Bridges while in inventory.",
            "§3Ghost Bridges create temporary glass",
            "§3platforms for mobility.",
            "",
            "§3Talisman",
            "§fMovement",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.BOOK;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
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

