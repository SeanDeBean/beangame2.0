package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import com.beangamecore.Main;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.particles.BeangameParticleManager;
import com.beangamecore.util.BlockCategories;

public class MelonAxe extends BeangameItem implements BGRClickableI, BGDDealerHeldI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Block clickedBlock = event.getClickedBlock();
        if (isInvalidBlock(clickedBlock)) {
            return true;
        }
        Material type = clickedBlock.getType();
        for (Material functionalblocks : BlockCategories.getFunctionalBlocks()) {
            if (type == functionalblocks) {
                return false;
            }
        }
        clickedBlock.setType(Material.MELON);

        Location min = clickedBlock.getLocation();
        Location max = min.clone().add(1.0, 1.0, 1.0);
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        particleManager.particleCube(min, max, 50, 205, 50);

        clickedBlock.getWorld().playSound(min, Sound.ITEM_CROP_PLANT, 0.6f, 1.2f);

        return true;
    }

    private boolean isInvalidBlock(Block block) {
        return block == null || block.getType() == Material.AIR || block.getType() == Material.MELON;
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        Block block = event.getEntity().getLocation().getBlock();
        if(block.getType().equals(Material.AIR)){
            block.setType(Material.MELON);

            Location min = block.getLocation();
            Location max = min.clone().add(1.0, 1.0, 1.0);
            BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
            particleManager.particleCube(min, max, 50, 205, 50);

            block.getWorld().playSound(min, Sound.ITEM_CROP_PLANT, 0.6f, 1.2f);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0L;
    }

    @Override
    public String getId() {
        return "melonaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public boolean isInFoodItemRotation(){
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "MM ", "MB ", " S ", r.mCFromMaterial(Material.MELON_SLICE), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§2Melon Axe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Right-click blocks to convert them",
            "§2to melons. Melee hits also spawn",
            "§2melons at enemy locations.",
            "",
            "§cOn Hit",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:efficiency", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_AXE;
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

