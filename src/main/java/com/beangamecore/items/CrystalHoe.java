package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;
import com.beangamecore.commands.BeangameStart;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class CrystalHoe extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        if (event.getClickedBlock() == null) {
            return false;
        }
        Block block = event.getClickedBlock();
        Material type = block.getType();
        if (!isValidSoil(type)) {
            return false;
        }
        // timer system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            event.setCancelled(true);
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // event
        Location loc = block.getLocation();
        loc.setY(loc.getY() + 1);
        Block crop = loc.getBlock();
        World world = block.getWorld();
        if (!crop.getType().equals(Material.AIR)) {
            return false;
        }
        world.playSound(loc, Sound.BLOCK_BELL_RESONATE, 1.0F, 1.0F);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(),
                () -> crop.setType(Material.BEETROOTS, true), 1L);
        loc.setX(loc.getX() + 0.5);
        loc.setZ(loc.getZ() + 0.5);
        double delayPercent = BeangameStart.percent;
        if(delayPercent <= 0.4) delayPercent = 0.4;

        long delayTicks = 38L - (long)(12L * ((delayPercent - 0.4) / 0.6));

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(),
                () -> world.spawn(loc, EnderCrystal.class), delayTicks);
        return true;
    }

    private boolean isValidSoil(Material type) {
        return type.equals(Material.GRASS_BLOCK) || type.equals(Material.DIRT);
    }

    @Override
    public long getBaseCooldown() {
        return 5250L;
    }

    @Override
    public String getId() {
        return "crystalhoe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "TTC", " S ", "S  ", r.eCFromBeangame(Key.bg("tntimer")), r.mCFromMaterial(Material.END_CRYSTAL), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§5Crystal Hoe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click on dirt, grass, or path",
            "§9blocks to instantly grow beetroots and",
            "§9summon an end crystal after 1 second.",
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
        return Material.DIAMOND_HOE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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
