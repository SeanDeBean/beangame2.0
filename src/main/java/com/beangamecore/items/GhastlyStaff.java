package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class GhastlyStaff extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_GHAST_SHOOT, 1.0F, 1.0F);
        Fireball ghastlystafffireball = (Fireball) player.launchProjectile(Fireball.class);
        loc.setY(loc.getY() + 1.5D);
        ghastlystafffireball.setVelocity(player.getEyeLocation().getDirection().multiply(1));
        ghastlystafffireball.setYield(1.0f);
        // despawn fireballs
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if(ghastlystafffireball != null){
                ghastlystafffireball.remove();
            }
        }, 240L);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 2500L;
    }

    @Override
    public String getId() {
        return "ghastlystaff";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " FF", " BF", "S  ", r.mCFromMaterial(Material.FIRE_CHARGE), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§6Ghastly Staff";
    }

    public List<String> getLore() {
    return List.of(
        "§9Shoots fireballs that travel forward",
        "§9and explode on impact.",
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
        return Material.BLAZE_ROD;
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

