package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.move.BGMoveArmorI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.ItemNBT;


public class Multipants extends BeangameItem implements BGMoveArmorI {
    
    private static final double SPEED_MAX = 1;
    private static final double SPEED_INCREMENT = 0.0004;

    private final Map<UUID, Double> speedBuffs = new HashMap<>();

    @Override
    public void onMoveArmor(PlayerMoveEvent event, ItemStack armor) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        double current = speedBuffs.getOrDefault(id, 0.0);
        if (player.isSprinting()) {
            // Increase speed gradually up to cap
            double newSpeed = Math.min(current + SPEED_INCREMENT, SPEED_MAX);
            speedBuffs.put(id, newSpeed);
            updateArmorSpeedAttribute(player, armor, newSpeed);
        } else {
            // Reset speed buff
            speedBuffs.remove(id);
            updateArmorSpeedAttribute(player, armor, 0);
        }


    }

    private void updateArmorSpeedAttribute(Player player, ItemStack armor, double speed) {
        ItemStack updatedArmor = updateAttribute(armor, Attribute.MOVEMENT_SPEED, speed);
        replaceArmorItem(player, armor, updatedArmor);
    }

    private void replaceArmorItem(Player player, ItemStack oldItem, ItemStack newItem) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = player.getInventory().getItem(slot);
            if (equipped != null && equipped.equals(oldItem)) {
                player.getInventory().setItem(slot, newItem);
                break;
            }
        }
    }

    public static ItemStack updateAttribute(ItemStack item, Attribute attribute, double value) {
        BeangameItem bgitem = BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(item));
        if(!bgitem.getId().equals("multipants")){
            return item;
        }

        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        NamespacedKey nsk = new NamespacedKey(Main.getPlugin(), "beangame.multipants");
        meta.removeAttributeModifier(attribute);
        meta.addAttributeModifier(attribute, new AttributeModifier(nsk, value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS));
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "multipants";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§eMultipants";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Gradually increases movement speed",
            "§6while sprinting. Speed bonus resets",
            "§6when you stop sprinting.",
            "",
            "§6Armor",
            "§fMovement",
            "§9§obeangame",
            "§9", "§7When on Legs:", "§9+3 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_LEGGINGS;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.RIB);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 3;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.LEGS;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

