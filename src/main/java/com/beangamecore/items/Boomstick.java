package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.BlockCategories;
import com.beangamecore.util.Cooldowns;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Boomstick extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        UUID uuid = player.getUniqueId();
        
        Block clickedBlock = event.getClickedBlock();
        
        // Check if the block is not null and not air
        if (clickedBlock == null || clickedBlock.getType() == Material.AIR) {
            return false;
        }

        Material type = clickedBlock.getType();

        // Check if the clicked block is in the functional blocks category
        if (BlockCategories.getFunctionalBlocks().contains(type) || type.equals(Material.TNT)) {
            return false;
        }

        // Set the block type to TNT if it's not a functional block
        clickedBlock.setType(Material.TNT);

        Cooldowns.setCooldown("explosion_immunity", uuid, 500L);
        
        Location min = clickedBlock.getLocation();
        Location max = min.clone().add(1.0, 1.0, 1.0);
        Main.getPlugin().getParticleManager().particleCube(min, max, 255, 0, 0);

        world.playSound(min, Sound.BLOCK_AZALEA_PLACE, 0.7f, 1f);

        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "boomstick";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " TR", " RT", "S  ", r.mCFromMaterial(Material.TNT), r.eCFromBeangame(Key.bg("tntimer")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§4Boomstick";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click on any solid block to",
            "§9instantly convert it into TNT.",
            "§9Grants brief explosion immunity on use.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.BLAZE_ROD;
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

