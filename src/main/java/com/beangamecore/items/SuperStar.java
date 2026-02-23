package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.items.type.talisman.BGHPTalismanI;
import com.beangamecore.util.Cooldowns;

public class SuperStar extends BeangameItem implements BGHPTalismanI, BGDDealerInvI {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Cooldowns.setCooldown("immobilized", uuid, 0);
    }

    @Override
        public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity damager = (LivingEntity) event.getDamager();

        int duration = 20 * 5; // 5 seconds
        int newLevel = 0;

        // Check if damager already has Haste
        if (damager.hasPotionEffect(PotionEffectType.HASTE)) {
            PotionEffect current = damager.getPotionEffect(PotionEffectType.HASTE);
            newLevel = Math.min(current.getAmplifier() + 1, 7); // increase level by 1
        }

        // Apply new Haste effect with updated level
        damager.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, newLevel, true, true));
    }

    @Override
    public long getBaseCooldown() {
        return 0L;
    }

    @Override
    public String getId() {
        return "superstar";
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
        return "§eSuper Star";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3The carrier is immune to immobilization.",
            "§3Grants Haste when attacking enemies,",
            "§3stacking up to Haste VIII.",
            "§3Haste effects reduce all cooldowns.",
            "",
            "§cOn Hit",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.GLOWSTONE_DUST;
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

