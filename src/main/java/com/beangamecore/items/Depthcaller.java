package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import com.beangamecore.entities.tentacles.WaterPool;

public class Depthcaller extends BeangameItem implements BGDDealerHeldI {

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity damager = (LivingEntity) event.getDamager();
        UUID uuid = damager.getUniqueId();

        if(onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);
        LivingEntity target = (LivingEntity) event.getEntity();
        spawnWaterPool(target.getLocation(), damager);
    }

    private void spawnWaterPool(Location location, LivingEntity damager){
        new WaterPool(location, damager, Main.getPlugin());
    }


    @Override
    public long getBaseCooldown() {
        return 18000L; // 14 second cooldown
    }

    @Override
    public String getId() {
        return "depthcaller";
    }

    @Override
    public String getName() {
        return "§9Depthcaller";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting enemies summons a whirling water",
            "§cpool pulls all nearby entities twoards",
            "§cthe center and deals damage to all enemies",
            "§cinside the range of the pool.", 
            "",
            "§cOn Hit",
            "§dOn Hit Extender",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 1);
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

