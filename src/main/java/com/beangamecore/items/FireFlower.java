package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.general.BGResetableI;

import org.bukkit.Bukkit;
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

public class FireFlower extends BeangameItem implements BGLClickableI, BGResetableI {
    
    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid) || player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }
        applyCooldown(uuid);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1F, 1);
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Location eyeLoc = player.getEyeLocation();

        // Center fireball
        new Fireball(eyeLoc, player, direction);

        // Left fireball after 1 tick
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Vector left = rotateVectorAroundY(direction.clone(), -10).multiply(0.8);
            new Fireball(eyeLoc, player, left);
        }, 1L);

        // Right fireball after 2 ticks
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Vector right = rotateVectorAroundY(direction.clone(), 10).multiply(0.8);
            new Fireball(eyeLoc, player, right);
        }, 2L);
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

    @Override
    public void resetItem(){
        for(Fireball fireball : Fireball.getFireballs()){
            fireball.remove();
        }
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 1250L;
    }

    @Override
    public String getId() {
        return "fireflower";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§cFire Flower";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§dFires three spread fireballs that",
            "§ddamage players on impact. Triggers",
            "§don-hit effects from fireball damage.",
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
        return Material.POPPY;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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
