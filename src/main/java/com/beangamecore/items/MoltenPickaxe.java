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
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class MoltenPickaxe extends BeangameItem implements BGToolI {
    
    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location loc = event.getBlock().getLocation();
        Material type = event.getBlock().getType();
        Integer amount = getRandomAmount();

        // item event
        if (isIronOre(type)) {
            handleIronOreDrop(event, world, loc, amount);
        } else if (isCopperOre(type)) {
            handleCopperOreDrop(event, world, loc, amount);
        } else if (isGoldOre(type)) {
            handleGoldOreDrop(event, world, loc, amount);
        } else if (type.equals(Material.ANCIENT_DEBRIS)) {
            handleAncientDebrisDrop(event, world, loc);
        } else if (type.equals(Material.WET_SPONGE)) {
            handleWetSpongeDrop(event, world, loc);
        } else {
            handleOtherBlocks(event, world, loc, type, amount);
        }
    }

    private Integer getRandomAmount() {
        Integer amount = 1;
        if (Math.random() < 0.2D) {
            amount = 2;
        }
        return amount;
    }

    private boolean isIronOre(Material type) {
        return type.equals(Material.IRON_ORE) || type.equals(Material.DEEPSLATE_IRON_ORE);
    }

    private boolean isCopperOre(Material type) {
        return type.equals(Material.COPPER_ORE) || type.equals(Material.DEEPSLATE_COPPER_ORE);
    }

    private boolean isGoldOre(Material type) {
        return type.equals(Material.GOLD_ORE) || type.equals(Material.DEEPSLATE_GOLD_ORE)
                || type.equals(Material.NETHER_GOLD_ORE);
    }

    private void handleIronOreDrop(BlockBreakEvent event, World world, Location loc, Integer amount) {
        event.setDropItems(false);
        world.dropItemNaturally(loc, new ItemStack(Material.IRON_INGOT, amount));
    }

    private void handleCopperOreDrop(BlockBreakEvent event, World world, Location loc, Integer amount) {
        event.setDropItems(false);
        world.dropItemNaturally(loc, new ItemStack(Material.COPPER_INGOT, amount + 3));
    }

    private void handleGoldOreDrop(BlockBreakEvent event, World world, Location loc, Integer amount) {
        event.setDropItems(false);
        world.dropItemNaturally(loc, new ItemStack(Material.GOLD_INGOT, amount));
    }

    private void handleAncientDebrisDrop(BlockBreakEvent event, World world, Location loc) {
        event.setDropItems(false);
        world.dropItemNaturally(loc, new ItemStack(Material.NETHERITE_SCRAP, 1));
    }

    private void handleWetSpongeDrop(BlockBreakEvent event, World world, Location loc) {
        event.setDropItems(false);
        world.dropItemNaturally(loc, new ItemStack(Material.SPONGE, 1));
    }

    private void handleOtherBlocks(BlockBreakEvent event, World world, Location loc, Material type, Integer amount) {
        if (BlockCategories.logs.contains(type)) {
            event.setDropItems(false); // Prevent regular drops
            world.dropItemNaturally(loc, new ItemStack(Material.CHARCOAL, amount)); // Drop charcoal instead
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "moltenpickaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CNN", " E ", " S ", r.eCFromBeangame(Key.bg("cosmicingot")), r.mCFromMaterial(Material.NETHERITE_SCRAP), r.eCFromBeangame(Key.bg("explosivepickaxe")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§4Molten Pickaxe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§5Automatically smelts ores when",
            "§5mined. Drops iron/gold ingots,",
            "§5copper ingots (+3), netherite",
            "§5scrap, and converts logs to charcoal.",
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
        return Material.DIAMOND_PICKAXE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

