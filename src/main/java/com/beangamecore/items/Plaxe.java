package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGToolI;
import com.beangamecore.util.BlockCategories;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Plaxe extends BeangameItem implements BGToolI {
    
    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        Block block = event.getBlock();
        Material type = block.getType();
        World world = block.getWorld();
        Location loc = block.getLocation();
        if (event.getPlayer().isSneaking()) {
            return;
        }
        // checks all the leaf types
        if (BlockCategories.logs.contains(type)) {
            handleLogBlockBreak(event, world, loc, type);
            return;
        }
    }

    private void handleLogBlockBreak(BlockBreakEvent event, World world, Location loc, Material type) {
        event.setDropItems(false);
        world.dropItemNaturally(loc, new ItemStack(Material.STICK, 4));
        dropPlanksForLogType(world, loc, type);
    }

    private void dropPlanksForLogType(World world, Location loc, Material type) {
        Material planksType = getPlanksTypeForLog(type);
        world.dropItemNaturally(loc, new ItemStack(planksType, 8));
    }

    private Material getPlanksTypeForLog(Material type) {
        if (isOakLog(type)) {
            return Material.OAK_PLANKS;
        } else if (isSpruceLog(type)) {
            return Material.SPRUCE_PLANKS;
        } else if (isBirchLog(type)) {
            return Material.BIRCH_PLANKS;
        } else if (isJungleLog(type)) {
            return Material.JUNGLE_PLANKS;
        } else if (isAcaciaLog(type)) {
            return Material.ACACIA_PLANKS;
        } else if (isDarkOakLog(type)) {
            return Material.DARK_OAK_PLANKS;
        } else if (isMangroveLog(type)) {
            return Material.MANGROVE_PLANKS;
        } else if (isCherryLog(type)) {
            return Material.CHERRY_PLANKS;
        } else if (isPaleOakLog(type)) {
            return Material.PALE_OAK_PLANKS;
        } else if (isCrimsonLog(type)) {
            return Material.CRIMSON_PLANKS;
        } else {
            return Material.WARPED_PLANKS;
        }
    }

    private boolean isOakLog(Material type) {
        return type == Material.OAK_LOG || type == Material.STRIPPED_OAK_LOG || type == Material.OAK_WOOD
                || type == Material.STRIPPED_OAK_WOOD;
    }

    private boolean isSpruceLog(Material type) {
        return type == Material.SPRUCE_LOG || type == Material.STRIPPED_SPRUCE_LOG || type == Material.SPRUCE_WOOD
                || type == Material.STRIPPED_SPRUCE_WOOD;
    }

    private boolean isBirchLog(Material type) {
        return type == Material.BIRCH_LOG || type == Material.STRIPPED_BIRCH_LOG || type == Material.BIRCH_WOOD
                || type == Material.STRIPPED_BIRCH_WOOD;
    }

    private boolean isJungleLog(Material type) {
        return type == Material.JUNGLE_LOG || type == Material.STRIPPED_JUNGLE_LOG || type == Material.JUNGLE_WOOD
                || type == Material.STRIPPED_JUNGLE_WOOD;
    }

    private boolean isAcaciaLog(Material type) {
        return type == Material.ACACIA_LOG || type == Material.STRIPPED_ACACIA_LOG || type == Material.ACACIA_WOOD
                || type == Material.STRIPPED_ACACIA_WOOD;
    }

    private boolean isDarkOakLog(Material type) {
        return type == Material.DARK_OAK_LOG || type == Material.STRIPPED_DARK_OAK_LOG
                || type == Material.DARK_OAK_WOOD || type == Material.STRIPPED_DARK_OAK_WOOD;
    }

    private boolean isMangroveLog(Material type) {
        return type == Material.MANGROVE_LOG || type == Material.STRIPPED_MANGROVE_LOG
                || type == Material.MANGROVE_WOOD || type == Material.STRIPPED_MANGROVE_WOOD;
    }

    private boolean isCherryLog(Material type) {
        return type == Material.CHERRY_LOG || type == Material.STRIPPED_CHERRY_LOG || type == Material.CHERRY_WOOD
                || type == Material.STRIPPED_CHERRY_WOOD;
    }

    private boolean isPaleOakLog(Material type) {
        return type == Material.PALE_OAK_LOG || type == Material.STRIPPED_PALE_OAK_LOG
                || type == Material.PALE_OAK_WOOD || type == Material.STRIPPED_PALE_OAK_WOOD;
    }

    private boolean isCrimsonLog(Material type) {
        return type == Material.CRIMSON_STEM || type == Material.STRIPPED_CRIMSON_STEM
                || type == Material.CRIMSON_HYPHAE || type == Material.STRIPPED_CRIMSON_HYPHAE;
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "plaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "LLL", "LAL", "LBL", r.mCFromMaterial(Material.OAK_LOG), r.mCFromMaterial(Material.DIAMOND_AXE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§bPlaxe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Automatically converts logs to",
            "§58 planks and 4 sticks when mined.",
            "§5Preserves wood type and processes",
            "§5all log variants.",
            "",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:efficiency", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_AXE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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

