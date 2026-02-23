package com.beangamecore.items;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class MobExterminator extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        double radius = 7;

        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Cooldown check
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // Item event action
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GOAT_SCREAMING_DEATH, 1, 0);

        // Set of protected entity types for fast lookup
        HashSet<EntityType> protectedEntityTypes = new HashSet<>(List.of(
            EntityType.PLAYER, EntityType.ITEM, EntityType.TNT, EntityType.EGG,
            EntityType.WITHER_SKULL, EntityType.SNOWBALL, EntityType.SPECTRAL_ARROW, 
            EntityType.TNT_MINECART, EntityType.ENDER_PEARL, EntityType.END_CRYSTAL, 
            EntityType.AREA_EFFECT_CLOUD, EntityType.ARMOR_STAND, EntityType.ARROW, 
            EntityType.BLOCK_DISPLAY, 
            EntityType.DRAGON_FIREBALL, EntityType.EXPERIENCE_ORB, 
            EntityType.FALLING_BLOCK, EntityType.FIREBALL, EntityType.FIREWORK_ROCKET, 
            EntityType.FISHING_BOBBER, EntityType.GLOW_ITEM_FRAME, EntityType.INTERACTION, 
            EntityType.ITEM_DISPLAY, EntityType.ITEM_FRAME, EntityType.LEASH_KNOT, 
            EntityType.LLAMA_SPIT, EntityType.MARKER, EntityType.MINECART, 
            EntityType.CHEST_MINECART, EntityType.FURNACE_MINECART, EntityType.HOPPER_MINECART, 
            EntityType.COMMAND_BLOCK_MINECART, EntityType.SPAWNER_MINECART, EntityType.PAINTING, 
            EntityType.SHULKER_BULLET, EntityType.SMALL_FIREBALL, EntityType.POTION, 
            EntityType.TEXT_DISPLAY, EntityType.EXPERIENCE_BOTTLE, EntityType.EXPERIENCE_ORB, 
            EntityType.TRIDENT,

            EntityType.OAK_BOAT, EntityType.OAK_CHEST_BOAT, EntityType.BIRCH_BOAT, EntityType.BIRCH_CHEST_BOAT,
            EntityType.JUNGLE_BOAT, EntityType.JUNGLE_CHEST_BOAT, EntityType.ACACIA_BOAT, EntityType.ACACIA_CHEST_BOAT,
            EntityType.MANGROVE_BOAT, EntityType.MANGROVE_CHEST_BOAT, EntityType.DARK_OAK_BOAT, EntityType.DARK_OAK_CHEST_BOAT,
            EntityType.PALE_OAK_BOAT, EntityType.PALE_OAK_CHEST_BOAT, EntityType.BAMBOO_RAFT, EntityType.BAMBOO_CHEST_RAFT,
            EntityType.CHERRY_BOAT, EntityType.CHERRY_CHEST_BOAT, EntityType.SPRUCE_BOAT, EntityType.SPRUCE_CHEST_BOAT
        ));

        // Iterate through nearby entities and remove unprotected ones
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            EntityType type = entity.getType();
            if (!protectedEntityTypes.contains(type)) {
                entity.getWorld().spawnParticle(Particle.SMOKE, entity.getLocation(), 3);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 0);
                entity.remove();
            }
        }

        DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 2);
        Main.getPlugin().getParticleManager().spawnParticleSphere(player.getLocation(), radius, Particle.DUST, dustOptions, 200);

        dustOptions = new DustOptions(Color.fromRGB(34, 139, 34), 1);
        Main.getPlugin().getParticleManager().spawnParticleSphere(player.getLocation(), radius, Particle.DUST, dustOptions, 40);

        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 18000L;
    }

    @Override
    public String getId() {
        return "mobexterminator";
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
        return "§4Mob Exterminator";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to instantly eliminate",
            "§9all mobs and entities within a 7",
            "§9block radius.",
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
        return Material.BLACK_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

