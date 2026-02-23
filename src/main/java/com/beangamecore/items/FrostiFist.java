package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Effect;
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

public class FrostiFist extends BeangameItem implements BGDDealerInvI {

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack stack){
        LivingEntity entity = (LivingEntity) event.getEntity();

        int currentFreezeTicks = entity.getFreezeTicks();
        entity.setFreezeTicks(Math.max(100, currentFreezeTicks));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 1));

        if(event.getDamager() instanceof Player damager){
            damager.playEffect(entity.getLocation(), Effect.STEP_SOUND, Material.PACKED_ICE);
            damager.playEffect(entity.getEyeLocation(), Effect.STEP_SOUND, Material.PACKED_ICE);
        }
        if(entity instanceof Player player){
            player.playEffect(entity.getLocation(), Effect.STEP_SOUND, Material.PACKED_ICE);
            player.playEffect(entity.getEyeLocation(), Effect.STEP_SOUND, Material.PACKED_ICE);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "frostifist";
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
        return "§bFrosti Fist";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Freezes enemies for 5 seconds and",
            "§3applies mining fatigue for 2 seconds",
            "§3on hit.",
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
        return Material.ICE;
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

