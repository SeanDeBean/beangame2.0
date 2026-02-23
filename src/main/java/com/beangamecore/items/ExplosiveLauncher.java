package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class ExplosiveLauncher extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack itemStack){
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_GHAST_SHOOT, 1.0F, -1.0F);
        loc.setY(loc.getY() + 1.5D);
        Entity explosivelaunchertnt = world.spawn(loc, TNTPrimed.class);
        ((TNTPrimed)explosivelaunchertnt).setFuseTicks(64);
        explosivelaunchertnt.setVelocity(loc.getDirection().multiply(1.2D));
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 3500L;
    }

    @Override
    public String getId() {
        return "explosivelauncher";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "GIR", "III", "   ", r.mCFromMaterial(Material.GUNPOWDER), r.mCFromMaterial(Material.IRON_INGOT), r.eCFromBeangame(Key.bg("tntimer")));
        return null;
    }

    @Override
    public String getName() {
        return "§8Explosive Launcher";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to launch a lit TNT",
            "§9in your facing direction with 3.2",
            "§9second fuse. The TNT travels in an",
            "§9arc and explodes on impact or timer.",
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
        return Material.IRON_HORSE_ARMOR;
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

