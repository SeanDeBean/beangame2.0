package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BGResetableI;

public class TennisBallItem extends BeangameItem implements BGRClickableI, BGResetableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1F, 1);
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location eyeLoc = player.getEyeLocation();

        // tennisball
        new TennisBall(eyeLoc, player, direction);
        return true;
    }

    @Override
    public void resetItem(){
        for(TennisBall tennisball : TennisBall.getTennisBalls()){
            tennisball.remove(true);
        }
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 12000L;
    }

    @Override
    public String getId() {
        return "tennisball";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§aTennis Ball";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to throw a tennis ball",
            "§9that summons a horde of angry dogs",
            "§9to chase after it.",
            "",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.SLIME_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 104;
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

