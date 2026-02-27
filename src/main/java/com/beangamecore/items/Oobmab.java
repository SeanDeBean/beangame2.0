package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.BlockCategories;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Oobmab extends BeangameItem implements BGRClickableI {
    
    private void flipBlocks(Location loc, int radius) {
        int px = loc.getBlockX();
        int py = loc.getBlockY();
        int pz = loc.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= 0; y++) {
                for (int z = -radius; z <= radius; z++) {
                    processBlockPair(loc, px, py, pz, x, y, z);
                }
            }
        }
    }

    private void processBlockPair(Location loc, int px, int py, int pz, int x, int y, int z) {
        Block block = loc.getWorld().getBlockAt(px + x, py + y, pz + z);
        Block belowblock = loc.getWorld().getBlockAt(px + x, py - y, pz + z);

        Material blockType = block.getType();
        Material belowblockType = belowblock.getType();

        if (!shouldCancel(blockType, belowblockType)) {
            block.setType(belowblockType);
            belowblock.setType(blockType);
        }
    }

    private boolean shouldCancel(Material blockType, Material belowblockType) {
        if (isFunctionalBlock(blockType)) {
            return true;
        }
        if (isFunctionalBlock(belowblockType)) {
            return true;
        }
        return false;
    }

    private boolean isFunctionalBlock(Material material) {
        for (Material functionalblocks : BlockCategories.getFunctionalBlocks()) {
            if (material == functionalblocks) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getEyeLocation();
        flipBlocks(loc, 4);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 26500L;
    }

    @Override
    public String getId() {
        return "oobmab";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "DDD", "SRS", "GGG", r.mCFromMaterial(Material.DEEPSLATE), r.mCFromMaterial(Material.STONE), r.eCFromBeangame(Key.bg("drit")), r.mCFromMaterial(Material.GRASS_BLOCK));
        return null;
    }

    @Override
    public String getName() {
        return "§2Oobmab";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to flip all blocks",
            "§9within a 4 block radius vertically.",
            "§9Swaps blocks above and below you",
            "§9while preserving functional blocks.",
            "§9Creates dramatic terrain changes.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BAMBOO;
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

