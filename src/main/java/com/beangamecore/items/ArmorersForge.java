package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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

public class ArmorersForge extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);

        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        World world = player.getWorld();
        Location loc = player.getLocation();

        world.playSound(loc, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 0);

        // Create and distribute armor pieces
        giveArmorPiece(inventory, world, loc, Material.IRON_HELMET);
        giveArmorPiece(inventory, world, loc, Material.IRON_CHESTPLATE);
        giveArmorPiece(inventory, world, loc, Material.IRON_LEGGINGS);
        giveArmorPiece(inventory, world, loc, Material.IRON_BOOTS);

        return true;
    }

    private void giveArmorPiece(PlayerInventory inventory, World world, Location loc, Material armorMaterial) {
        // Create armor piece with a chance of Protection I enchantment
        ItemStack armorPiece = new ItemStack(armorMaterial);
        if (Math.random() < 0.35) {
            armorPiece.addEnchantment(Enchantment.PROTECTION, 1);
        }

        // Add to inventory or drop if inventory is full
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
        return "armorersforge";
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
        return "§dArmorer's Forge";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Right-click to receive a full set",
            "§6of iron armor with a 35% chance",
            "§6for each piece to have Protection I.",
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
        return Material.ANVIL;
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

