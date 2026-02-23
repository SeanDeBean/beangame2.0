package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.BGDamageInvI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffectType;

public class WithersGift extends BeangameItem implements BGLPTalismanI, BGDamageInvI {
    
    @Override
    public void onDamageInventory(EntityDamageEvent event, ItemStack item){
        if (event.getCause().equals(DamageCause.WITHER)){
            event.setCancelled(true);
        }
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        if(player.hasPotionEffect(PotionEffectType.WITHER)){
            player.removePotionEffect(PotionEffectType.WITHER);
        }
        if (Math.random() <= 0.1){
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(new ItemStack(Material.COAL, 1));
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.COAL, 1));
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "withersgift";
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
        return "§8Wither's Gift";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Grants immunity to Wither",
            "§3Periodically gives 1 coal to the carrier (10% chance)",
            "",
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
        return Material.WITHER_ROSE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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

