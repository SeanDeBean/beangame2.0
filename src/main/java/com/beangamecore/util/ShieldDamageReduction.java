package com.beangamecore.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShieldDamageReduction {

    private static double calculateDamageApplied(double damage, double points, double toughness, int resistance, int epf) {
        double withArmorReduction = damage * (1 - Math.min(20, Math.max(points / 5, points - damage / (2 + toughness / 4))) / 25);
        double withResistanceReduction = withArmorReduction * (1 - (resistance * 0.2));
        return withResistanceReduction * (1 - (Math.min(20.0, epf) / 25));
    }

    private static int getEPF(PlayerInventory inv, Enchantment enchantment) {
        ItemStack helm = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boot = inv.getBoots();
        return (helm != null ? helm.getEnchantmentLevel(enchantment) : 0) +
                (chest != null ? chest.getEnchantmentLevel(enchantment) : 0) +
                (legs != null ? legs.getEnchantmentLevel(enchantment) : 0) +
                (boot != null ? boot.getEnchantmentLevel(enchantment) : 0);
    }

    public static double calculate(double input, Player victim, Entity damager){
        PotionEffect effect = victim.getPotionEffect(PotionEffectType.RESISTANCE);
        int resistance = effect != null ? effect.getAmplifier() + 1 : 0;

        PlayerInventory inventory = victim.getInventory();

        AttributeInstance armor = victim.getAttribute(Attribute.ARMOR);
        AttributeInstance toughness = victim.getAttribute(Attribute.ARMOR_TOUGHNESS);

        boolean projectile = damager instanceof Projectile;
        boolean explosive = damager instanceof Creeper || damager instanceof ExplosiveMinecart || damager instanceof Explosive;
        boolean fire = damager.getFireTicks() > 0 || damager.isVisualFire() || damager instanceof SmallFireball;
        int totalepf = getEPF(inventory, Enchantment.PROTECTION);
        if(projectile) totalepf += getEPF(inventory, Enchantment.PROJECTILE_PROTECTION) * 2;
        if(explosive) totalepf += getEPF(inventory, Enchantment.BLAST_PROTECTION) * 2;
        if(fire) totalepf += getEPF(inventory, Enchantment.FIRE_PROTECTION) * 2;

        return calculateDamageApplied(input, armor.getValue(), toughness.getValue(), resistance, totalepf);
    }
    
}

