package com.beangamecore.blocks;

import com.beangamecore.Main;
import com.beangamecore.blocks.type.BGBreakableB;
import com.beangamecore.blocks.type.BGPlaceableB;
import com.beangamecore.blocks.type.BGWalkableB;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.recipes.RecipeAPI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;

public class LaunchPad extends BeangameItem implements BGPlaceableB, BGWalkableB, BGBreakableB, BeangameSoftItem {
    
    @Override
    public void onBlockPlace(BlockPlaceEvent event, ItemStack item) {

    }

    @Override
    public long getBaseCooldown() {
        return 500L;
    }

    @Override
    public String getId() {
        return "launchpad";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return new RecipeAPI(Main.getPlugin()).shapedRecipe("launchpad", asItem(), "SSS", "SWS", "SSS", new RecipeChoice.MaterialChoice(Material.SLIME_BALL), new RecipeChoice.MaterialChoice(Material.RED_WOOL));
    }

    @Override
    public String getName() {
        return ChatColor.RED+"Launch Pad";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§fPlaceable block that launches players",
            "§fforward and upward when stepped on.",
            "§fGrants fall damage immunity during",
            "§flaunch. Reusable placement item.",
            "",
            "§fMovement",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.RED_WOOL;
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
        return 16;
    }

    @Override
    public void onDestroy(Block block) {

    }

    @Override
    public boolean shouldDropOnDestroy(Block block) {
        return true;
    }

    @Override
    public void onMoveToBlock(PlayerMoveEvent event, Block block) {
        Player p = event.getPlayer();
        if(onCooldown(p.getUniqueId())) return;
        applyCooldown(p.getUniqueId());
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.4f, 0.5f);
        if(!(p.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    p.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
            p.setVelocity(p.getLocation().getDirection().multiply(1.6D).setY(1));
            Cooldowns.setCooldown("fall_damage_immunity", event.getPlayer().getUniqueId(), 3000L);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        event.setDropItems(false);
    }
}

