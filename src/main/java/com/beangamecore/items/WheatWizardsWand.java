package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BGToolI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class WheatWizardsWand extends BeangameItem implements BGToolI, BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Block clickedBlock = event.getClickedBlock();
        
        // Check if the block is GRASS_BLOCK and change it to HAY_BLOCK
        if (clickedBlock != null && clickedBlock.getType() == Material.GRASS_BLOCK) {
            clickedBlock.setType(Material.HAY_BLOCK);
            Location min = clickedBlock.getLocation();
            Location max = min.clone().add(1.0, 1.0, 1.0);
            Main.getPlugin().getParticleManager().particleCube(min, max, 228, 155, 15);
        }
        return true;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        Block block = event.getBlock();
        Material type = block.getType();
        
        // Cancel default drops for certain grass-related blocks and drop custom items
        if (type == Material.SHORT_GRASS || type == Material.TALL_GRASS) {
            handleGrassBreak(event, block, Material.WHEAT, 1);
        } else if (type == Material.GRASS_BLOCK) {
            handleGrassBreak(event, block, Material.HAY_BLOCK, 1);
        }
    }

    // Helper method to handle the custom drops and cancel default drops
    private void handleGrassBreak(BlockBreakEvent event, Block block, Material dropType, int amount) {
        World world = block.getWorld();
        Location loc = block.getLocation();
        event.setDropItems(false); // Disable default drops
        world.dropItemNaturally(loc, new ItemStack(dropType, amount)); // Drop custom item
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "wheatwizardswand";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public boolean isInFoodItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "HH ", " B ", " W ", r.mCFromMaterial(Material.HAY_BLOCK), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.WHEAT));
        return null;
    }

    @Override
    public String getName() {
        return "§eWheat Wizard's Wand";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Right-click to transmute grass blocks",
            "§2into hay bales infused with magic.",
            "§2Breaking grass yields wheat instead.",
            "",
            "§9Castable",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_HOE;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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

