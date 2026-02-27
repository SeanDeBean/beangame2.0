package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.PotionCategories;

public class CoconutMilkCutlass extends BeangameItem implements BGDDealerHeldI {
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity entity = (LivingEntity) event.getEntity();

        int effectsRemoved = 0;
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            int level = effect.getAmplifier() + 1;
            entity.removePotionEffect(effect.getType());
            effectsRemoved = effectsRemoved + level;
        }

        // Increase damage by 0.5 per effect removed
        double extraDamage = effectsRemoved * 0.5;
        event.setDamage(event.getDamage() + extraDamage);

        if (entity instanceof Player player) {
            UUID uuid = player.getUniqueId();
            for (String harmfulCustomPotions : PotionCategories.getHarmfulCustomPotions()) {
                Cooldowns.setCooldown(harmfulCustomPotions, uuid, 0);
            }
        }

        DustOptions whiteDust = new DustOptions(Color.fromRGB(255, 255, 255), 1.2f);

        for (int j = 0; j < 5; j++) {
            double offsetX = (Math.random() - 0.5) * 1.0;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 1.0;

            Location particleLoc = entity.getLocation().clone().add(offsetX, offsetY, offsetZ);
            entity.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, whiteDust);
        }

        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_DRINK, 0.7f, 1.2f);
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "coconutmilkcutlass";
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
        return "§fCoconut Milk Cutlass";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting enemies removes all their",
            "§cpotion effects and custom status",
            "§ceffects. Deals bonus damage equal to",
            "§c0.5 per effect level removed.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 104;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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

