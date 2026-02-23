package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BeangameSoftItem;

import org.bukkit.ChatColor;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Pizza extends BeangameItem implements BeangameSoftItem, BGLClickableI, BGProjectileI, BGConsumableI {
    
    @Override
    public void onProjHit(ProjectileHitEvent event) {
        // Check if the projectile hit a living entity
        if (event.getHitEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getHitEntity();
            Projectile projectile = event.getEntity();
            
            Entity damager = null;
            
            if (projectile.getShooter() instanceof Entity) {
                damager = (Entity) projectile.getShooter();
            }
            if (damager != null) {
                // Damage with the shooter as the damager
                target.damage(1.0, damager);
            } else {
                // If no shooter, just apply damage
                target.damage(1.0);
            }
        }
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // cooldown system
        if (onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);

        stack.setAmount(stack.getAmount() - 1);

        // item event
        Location loc = player.getEyeLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
        Snowball pizza = launchProjectile(this, player, Snowball.class);
        pizza.setItem(this.asItem());
    
        return;
    }

    @Override
    public void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        // do nothing
    }

    @Override
    public long getBaseCooldown() {
        return 500;
    }

    @Override
    public String getId() {
        return "pizza";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public int getCraftingAmount(){
        return 4;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Pizza";
    }

    @Override
    public List<String> getLore() {
        return List.of(ChatColor.BLUE + "beangame");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.CARROT;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 8;
    }

}

