package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;

public class BandageCannon extends BeangameItem implements BGRClickableI, BGProjectileI {
    
    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);

        // check a radius, heal all living entities in that radius
        Location loc = event.getEntity().getLocation();
        event.getEntity().remove();
        World world = loc.getWorld();
        Boolean consumed = false;
        for(Entity entity : world.getNearbyEntities(loc, 1, 1, 1)){
            if(!(entity instanceof LivingEntity e)){
                continue;
            }
            consumed = heal(e);
        }

        // if there are no entities in radius, spawn a bandage item at the location
        if(consumed){
            return;
        }
        ItemStack bandage = BeangameItemRegistry.getRaw(Key.bg("bandage")).asItem();
        bandage.setAmount(1);
        world.dropItem(loc, bandage);
    }

    private boolean heal(LivingEntity entity){
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        double health = attribute.getValue();
        if(entity.getHealth() == health){
            return false;
        }
        if(entity.getHealth() + 4 >= health){
            entity.setHealth(health);
        } else {
            entity.setHealth(entity.getHealth() + 4);
        }
        entity.getWorld().spawnParticle(Particle.GUST, entity.getLocation(), 3);
        entity.getWorld().playSound(entity, Sound.BLOCK_NOTE_BLOCK_FLUTE, 1, 1);
        return true;
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // cooldown system
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // item event
        Location loc = player.getEyeLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
        Snowball bandage = launchProjectile(this, player, Snowball.class);
        BeangameItemRegistry.get(Key.bg("bandage")).ifPresent(b -> {
            bandage.setItem(b.asItem());
        });

        return true;
    }
    
    @Override
    public long getBaseCooldown() {
        return 10000L;
    }

    @Override
    public String getId() {
        return "bandagecannon";
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
        return "§cBandage Cannon";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aShoots a projectile that heals all",
            "§aentities in a small radius for 2 hearts.",
            "§aDrops a bandage if no one is healed.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_HORSE_ARMOR;
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

