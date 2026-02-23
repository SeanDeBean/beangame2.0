package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Kneecapper extends BeangameItem implements BGDDealerHeldI {
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack stack){
        LivingEntity damager = (LivingEntity) event.getDamager();
        UUID uuid = damager.getUniqueId();
        Entity victim = event.getEntity();
        if(victim.getType().equals(EntityType.PLAYER)){
            if (onCooldown(uuid)){
                return;
            }
            applyCooldown(uuid);
            if(victim instanceof Player){
                ((Player) victim).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3Your knees broke!"));
            }
            Cooldowns.setCooldown("immobilized", victim.getUniqueId(), 1500L);
            damager.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0);
        }
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "  C", " M ", "B  ", r.mCFromMaterial(Material.CHERRY_LOG), r.mCFromMaterial(Material.MANGROVE_LOG), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 7500L;
    }

    @Override
    public String getId() {
        return "kneecapper";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public String getName() {
        return "§3Kneecapper";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cMelee hits immobilize players for",
            "§c1.5 seconds with a 7.5 second",
            "§ccooldown per target.",
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
        return Material.WOODEN_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

