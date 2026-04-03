package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGHPTalismanI;
import com.beangamecore.util.BlockCategories;

public class Blice extends BeangameItem implements BGHPTalismanI {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));

        // Convert blocks under player to blue ice
        Block standingBlock = player.getLocation().subtract(0, 1, 0).getBlock();
        Block[] nearbyBlocks = {
            standingBlock,
            standingBlock.getRelative(1, 0, 0),
            standingBlock.getRelative(-1, 0, 0),
            standingBlock.getRelative(0, 0, 1),
            standingBlock.getRelative(0, 0, -1)
        };
        
        for (Block block : nearbyBlocks) {
            if (block.getType().isSolid() && block.getType() != Material.BLUE_ICE && !BlockCategories.getFunctionalBlocks().contains(block.getType())) {
                block.setType(Material.BLUE_ICE);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "blice";
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
        return "§bBlice";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Grants permanent Speed II and",
            "§3places ice blocks below the carrier",
            "",
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
        return Material.BLUE_ICE;
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
