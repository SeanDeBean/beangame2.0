package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class TrickPearl extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        loc.setY(loc.getY()+1);
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Entity trickpearl = player.launchProjectile(EnderPearl.class);
        trickpearl.setVelocity(player.getEyeLocation().getDirection().multiply(1.55D));
        for(int i = 0; i < 7; i++){
            Projectile pearl = (Projectile) player.getWorld().spawnEntity(loc, EntityType.ENDER_PEARL);
            Random rand = new Random();
            pearl.setVelocity(loc.getDirection().multiply(1.55).add(new Vector(rand.nextDouble(), 0, rand.nextDouble()).subtract(new Vector(rand.nextDouble(), 0, rand.nextDouble()))));
        }
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 24000L;
    }

    @Override
    public String getId() {
        return "trickpearl";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " P ", " B ", " W ", r.mCFromMaterial(Material.ENDER_PEARL), r.eCFromBeangame(Key.bg("bean")), r.eCFromBeangame(Key.bg("witchsbrew")));
        return null;
    }

    @Override
    public String getName() {
        return "§eTrick Pearl";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to throw one real ender pearl",
            "§9and several fake decoy pearls.",
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
        return Material.ENDER_PEARL;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

