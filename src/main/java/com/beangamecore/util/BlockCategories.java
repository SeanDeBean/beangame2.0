package com.beangamecore.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

public class BlockCategories {

    public static Set<Material> getFunctionalBlocks(){
        return functionalBlocks;
    }

    private static Set<Material> functionalBlocks = new HashSet<>(Arrays.asList(
        Material.CRAFTING_TABLE, Material.STONECUTTER, Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE,
        Material.SMITHING_TABLE, Material.GRINDSTONE, Material.LOOM, Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE, Material.CAMPFIRE, Material.SOUL_CAMPFIRE, 
        Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.COMPOSTER, Material.NOTE_BLOCK, Material.JUKEBOX, Material.ENCHANTING_TABLE, 
        Material.BREWING_STAND, Material.CAULDRON, Material.BELL, Material.BEACON, Material.CONDUIT, Material.LODESTONE, Material.BEEHIVE, Material.BEE_NEST, 
        Material.LECTERN, Material.CHEST, Material.BARREL, Material.ENDER_CHEST, Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, 
        Material.GRAY_SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX, 
        Material.LIME_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, 
        Material.MAGENTA_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.RESPAWN_ANCHOR, Material.DRAGON_EGG, Material.END_PORTAL_FRAME, Material.END_PORTAL, 
        Material.NETHER_PORTAL, Material.BEDROCK, Material.REINFORCED_DEEPSLATE, Material.TRAPPED_CHEST, Material.HOPPER, Material.SPAWNER, Material.TURTLE_EGG, 
        Material.SCULK_SHRIEKER, Material.CHISELED_BOOKSHELF, Material.DISPENSER, Material.DROPPER, Material.BUDDING_AMETHYST, Material.BARRIER, Material.CRYING_OBSIDIAN,
        Material.TRIAL_SPAWNER, Material.VAULT, Material.HEAVY_CORE, Material.CRAFTER));

}

