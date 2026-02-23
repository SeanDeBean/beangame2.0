package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
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
import com.beangamecore.util.Cooldowns;

public class Contagion extends BeangameItem implements BGDDealerInvI {

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack stack) {
        if (!isLivingEntity(event.getDamager())) {
            return;
        }
        if (!isLivingEntity(event.getEntity())) {
            return;
        }
        LivingEntity victim = (LivingEntity) event.getEntity();
        applyActivePotionEffectsToVictim((LivingEntity) event.getDamager(), victim);
        UUID auuid = event.getDamager().getUniqueId();
        UUID vuuid = victim.getUniqueId();
        propagateCooldowns(auuid, vuuid);
    }

    private boolean isLivingEntity(Object entity) {
        return entity instanceof LivingEntity;
    }

    private void applyActivePotionEffectsToVictim(LivingEntity attacker, LivingEntity victim) {
        for (PotionEffect potionEffect : attacker.getActivePotionEffects()) {
            PotionEffectType type = potionEffect.getType();
            int level = potionEffect.getAmplifier();
            victim.addPotionEffect(new PotionEffect(type, 100, level));
        }
    }

    private void propagateCooldowns(UUID attackerId, UUID victimId) {
        propagateCooldown("silenced", attackerId, victimId);
        propagateCooldown("schizophrenic", attackerId, victimId);
        propagateCooldown("use_item", attackerId, victimId);
        propagateCooldown("slot_enforced", attackerId, victimId);
        propagateCooldown("immobilized", attackerId, victimId);
        propagateCooldown("jumbling", attackerId, victimId);
        propagateCooldown("redacted", attackerId, victimId);
    }

    private void propagateCooldown(String cooldownName, UUID attackerId, UUID victimId) {
        if (Cooldowns.onCooldown(cooldownName, attackerId)) {
            Cooldowns.setCooldown(cooldownName, victimId, 5000);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "contagion";
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
        return "§aContagion";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Hitting enemies transfers all your",
            "§3active potion effects and custom",
            "§3status effects to them for 5 seconds.",
            "§3Spreads both positive and negative",
            "§3effects to your victims.",
            "",
            "§cOn Hit",
            "§3Talisman",
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
        return Material.GLASS_BOTTLE;
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

