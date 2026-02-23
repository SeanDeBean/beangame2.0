package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.particles.BeangameParticleManager;
import com.beangamecore.util.Cooldowns;

public class OhMyPants extends BeangameItem implements BGArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Location ploc = player.getLocation().add(0, 0.5, 0);
        World world = player.getWorld();
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation().add(0, 1, 0), 1.75, 1.75, 1.75)) {
            if (isEligibleTarget(entity, uuid)) {
                UUID vuuid = entity.getUniqueId();
                Cooldowns.setCooldown("immobilized", vuuid, 1100L);
                Location eloc = entity.getLocation().add(0, 0.5, 0);
                particleManager.particleTrail(ploc, eloc, 255, 70, 0);
                world.playSound(eloc, Sound.BLOCK_BAMBOO_BREAK, 1, 1);
            }
        }
    }

    private boolean isEligibleTarget(Entity entity, UUID uuid) {
        return entity instanceof Player && entity.getUniqueId() != uuid
                && !((Player) entity).getGameMode().equals(GameMode.SPECTATOR);
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "ohmypants";
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
        return "§6Oh My Pants";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Automatically immobilizes nearby",
            "§6players within 1.75 blocks for",
            "§61.1 seconds. Creates orange particle",
            "§6trails to stunned targets.",
            "§6Continuous area denial effect.",
            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+3 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.CHAINMAIL_LEGGINGS;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.GOLD, TrimPattern.FLOW);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 3;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.LEGS;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

