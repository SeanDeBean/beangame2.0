package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;

import org.bukkit.Color;
import org.bukkit.GameMode;
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

public class Baleout extends BeangameItem implements BGLClickableI {
    
    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid) || player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }
        applyCooldown(uuid);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DROWNED_SHOOT, 0.7f, 0.9f);
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location eyeLoc = player.getEyeLocation();

        Vector left = rotateVectorAroundY(direction.clone(), -7).multiply(0.65);
        new BaleoutProjectile(eyeLoc, player, left);

        Vector right = rotateVectorAroundY(direction.clone(), 7).multiply(0.65);
        new BaleoutProjectile(eyeLoc, player, right);
    }

    public Vector rotateVectorAroundY(Vector vec, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = vec.getX();
        double z = vec.getZ();
        return new Vector(
            x * cos - z * sin,
            vec.getY(),
            x * sin + z * cos
        );
    }

    public void removeAllBales(){
        for(BaleoutProjectile bale : BaleoutProjectile.getBales()){
            bale.remove();
        }
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 1300L;
    }

    @Override
    public String getId() {
        return "baleout";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§eBaleout";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§dLeft-click to launch two high-damage",
            "§dhay bales that slow enemies on hit.",
            "",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
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
