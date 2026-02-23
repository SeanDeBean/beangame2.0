package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
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

public class StickySword extends BeangameItem implements BGDDealerHeldI, BGLPTalismanI {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAVING, 100, 0, false, false));
    }
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity damager = (LivingEntity) event.getDamager();
        if (shouldApplyCobwebRandomly(event)) {
            handleCobwebApplication(event, damager, false);
            return;
        }
        UUID uuid = damager.getUniqueId();
        // cooldown system
        if (onCooldown(uuid)) {
            return;
        }
        if (shouldApplyCobweb(event)) {
            handleCobwebApplication(event, damager, true);
            applyCooldown(uuid);
        }
    }

    private boolean shouldApplyCobwebRandomly(EntityDamageByEntityEvent event) {
        return Math.random() < 0.15D && event.getEntity().getLocation().getBlock().getType().equals(Material.AIR);
    }

    private boolean shouldApplyCobweb(EntityDamageByEntityEvent event) {
        return event.getEntity().getLocation().getBlock().getType().equals(Material.AIR);
    }

    private void handleCobwebApplication(EntityDamageByEntityEvent event, LivingEntity damager, boolean checkCooldown) {
        if(event.getEntity() instanceof LivingEntity entity && entity.getPotionEffect(PotionEffectType.WEAVING) != null){
            return;
        }
        event.getEntity().getLocation().getBlock().setType(Material.COBWEB);
    }
    
    @Override
    public long getBaseCooldown() {
        return 12000L;
    }

    @Override
    public String getId() {
        return "stickysword";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "SSC", "CBS", "ICS", r.mCFromMaterial(Material.STRING), r.mCFromMaterial(Material.COBWEB), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.IRON_SWORD));
        return null;
    }

    @Override
    public String getName() {
        return "§cSticky Sword";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cChance to trap victims in cobwebs on hit.",
            "§cGrants Weaving I to the carrier.",
            "",
            "§cOn Hit",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 103;
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

