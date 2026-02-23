package com.beangamecore.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

public class BlockCategories {

    public static Set<Material> functionalblock = new HashSet<>(Arrays.asList(
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

    public static Set<Material> naturalblocks = new HashSet<>(Arrays.asList(
        Material.STONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE,
        Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, Material.SANDSTONE, Material.RED_SANDSTONE, Material.BASALT, Material.SMOOTH_BASALT, Material.BLACKSTONE, 
        Material.NETHERRACK, Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM, Material.MAGMA_BLOCK, Material.END_STONE, Material.AMETHYST_BLOCK, Material.TUFF, Material.CALCITE, 
        Material.DRIPSTONE_BLOCK, Material.CLAY, Material.GRAVEL, Material.SCULK, Material.TERRACOTTA, Material.WHITE_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA, 
        Material.GRAY_TERRACOTTA, Material.BLACK_TERRACOTTA, Material.BROWN_TERRACOTTA, Material.RED_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.YELLOW_TERRACOTTA, 
        Material.LIME_TERRACOTTA, Material.GREEN_TERRACOTTA, Material.CYAN_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA, Material.BLUE_TERRACOTTA, Material.PURPLE_TERRACOTTA, 
        Material.MAGENTA_TERRACOTTA, Material.PINK_TERRACOTTA, Material.INFESTED_COBBLESTONE, Material.INFESTED_STONE, Material.INFESTED_STONE_BRICKS, 
        Material.INFESTED_MOSSY_STONE_BRICKS, Material.INFESTED_CRACKED_STONE_BRICKS, Material.INFESTED_CHISELED_STONE_BRICKS, Material.INFESTED_DEEPSLATE));

    public static Set<Material> ores = new HashSet<>(Arrays.asList(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.COPPER_ORE,
        Material.DEEPSLATE_COPPER_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.EMERALD_ORE, 
        Material.DEEPSLATE_EMERALD_ORE, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.NETHER_GOLD_ORE, 
        Material.NETHER_QUARTZ_ORE));

    public static Set<Material> logs = new HashSet<>(Arrays.asList(
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG, Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, 
        Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_MANGROVE_LOG, Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD, Material.JUNGLE_WOOD, Material.ACACIA_WOOD, 
        Material.DARK_OAK_WOOD, Material.MANGROVE_WOOD, Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD, Material.STRIPPED_BIRCH_WOOD, Material.STRIPPED_JUNGLE_WOOD, 
        Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_DARK_OAK_WOOD, Material.STRIPPED_MANGROVE_WOOD, Material.CRIMSON_STEM, Material.WARPED_STEM, Material.STRIPPED_CRIMSON_STEM, 
        Material.STRIPPED_WARPED_STEM, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE, Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE,
        Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_CHERRY_WOOD, Material.CHERRY_LOG, Material.CHERRY_WOOD, Material.STRIPPED_PALE_OAK_LOG, Material.STRIPPED_PALE_OAK_WOOD, 
        Material.PALE_OAK_LOG, Material.PALE_OAK_WOOD ));

    public static Set<Material> leaves = new HashSet<>(Arrays.asList(
        Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES, Material.MANGROVE_LEAVES, 
        Material.FLOWERING_AZALEA_LEAVES, Material.OAK_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, 
        Material.SPRUCE_LEAVES, Material.CHERRY_LEAVES, Material.PALE_OAK_LEAVES));

}

