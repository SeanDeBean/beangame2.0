package com.beangamecore.items;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Donkinator extends BeangameItem implements BGRClickableI {
    
    private static final double MAX_RADIUS = 8.0;
    private static final long EXPANSION_DURATION = 20L;

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
       
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
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DONKEY_ANGRY, 1, 0);

        double[] radius = {0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (radius[0] >= MAX_RADIUS) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            radius[0] += MAX_RADIUS / EXPANSION_DURATION;

            // Visualize the area
            DustOptions dustOptions = new DustOptions(Color.fromRGB(140, 125, 117), 1);
            Main.getPlugin().getParticleManager().spawnParticleSphere(player.getLocation(), radius[0], Particle.DUST, dustOptions, 100);

            // Exterminate unprotected entities in this radius
            for (Entity entity : player.getNearbyEntities(radius[0], radius[0], radius[0])) {
                if (shouldExterminate(entity)) {
                    transformAndShrinkToDonkey(entity);
                }
            }
        }, 0, 1).getTaskId();

        return true;
    }

    private boolean shouldExterminate(Entity entity) {
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
            EntityType.CHERRY_BOAT, EntityType.CHERRY_CHEST_BOAT, EntityType.SPRUCE_BOAT, EntityType.SPRUCE_CHEST_BOAT,

            EntityType.DONKEY
        ));

        if(protectedEntityTypes.contains(entity.getType())){
            return false;
        }
        return true;
    }

    private void transformAndShrinkToDonkey(Entity entity) {
        Location loc = entity.getLocation();
        entity.remove();

        Donkey donkey = (Donkey) loc.getWorld().spawnEntity(loc, EntityType.DONKEY);
        donkey.setAdult();

        AttributeInstance scale = donkey.getAttribute(Attribute.SCALE);
        if (scale != null) {
            scale.setBaseValue(1.0);
        }

       double[] scaleValue = {1.0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!donkey.isValid()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            scaleValue[0] *= 0.85;
            if (scaleValue[0] < 0.05) {
                donkey.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            AttributeInstance scaleAttr = donkey.getAttribute(Attribute.SCALE);
            if (scaleAttr != null) {
                scaleAttr.setBaseValue(scaleValue[0]);
            }
        }, 0L, 5L).getTaskId(); // Shrinks every 5 ticks
    }

    @Override
    public long getBaseCooldown() {
        return 22000L;
    }

    @Override
    public String getId() {
        return "donkinator";
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
        return "§7Donkinator";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to create an expanding zone",
            "§9that transforms all unprotected entities",
            "§9into shrinking donkeys. The zone grows",
            "§9to 8 blocks radius over 1 second.",
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
        return Material.BROWN_DYE;
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

