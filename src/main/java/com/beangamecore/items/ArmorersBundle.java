package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;

public class ArmorersBundle extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        World world = player.getWorld();
        Location loc = player.getLocation();

        world.playSound(loc, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1, 0);

        // Generate armor pieces
        giveArmorPiece(inventory, world, loc, Material.DIAMOND_HELMET, Material.IRON_HELMET);
        giveArmorPiece(inventory, world, loc, Material.DIAMOND_CHESTPLATE, Material.IRON_CHESTPLATE);
        giveArmorPiece(inventory, world, loc, Material.DIAMOND_LEGGINGS, Material.IRON_LEGGINGS);
        giveArmorPiece(inventory, world, loc, Material.DIAMOND_BOOTS, Material.IRON_BOOTS);

        return true;
    }

    private void giveArmorPiece(PlayerInventory inventory, World world, Location loc, Material diamondPiece, Material ironPiece) {
        ItemStack armorPiece = Math.random() < 0.15 ? new ItemStack(diamondPiece, 1) : new ItemStack(ironPiece, 1);
        if (inventory.firstEmpty() != -1) {
            inventory.addItem(armorPiece);
        } else {
            world.dropItemNaturally(loc, armorPiece);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "armorersbundle";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§dArmorer's Bundle";
    }

    public List<String> getLore() {
        return List.of(
            "§6Right-click to receive a full set",
            "§6of iron armor with a 15% chance",
            "§6for each piece to be diamond instead.",
            "",
            "§6Armor",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BARREL;
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

