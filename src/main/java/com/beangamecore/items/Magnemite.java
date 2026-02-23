package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.particles.BeangameParticleManager;

public class Magnemite extends BeangameItem implements BGArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        Location loc = player.getEyeLocation().add(0, 0.35, 0);
        World world = loc.getWorld();
        UUID uuid = player.getUniqueId();
        if(player.getGameMode() == GameMode.SPECTATOR){
            return;
        }
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
            if(!(entity instanceof LivingEntity)){
                continue;
            }
            LivingEntity victim = (LivingEntity) entity;
            Location vloc = victim.getEyeLocation().add(0, 0.35, 0);;
            if((victim instanceof Player && !((Player)victim).getGameMode().equals(GameMode.SURVIVAL))){
                continue;
            }
            if(!victim.getUniqueId().equals(uuid)){
                // effect
                victim.damage(1, player);
                world.spawnParticle(Particle.ELECTRIC_SPARK, vloc, 3);
                particleManager.lightningEffect(loc, vloc, 255, 255, 102);
            }

            
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "magnetmite";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " E ", "MHM", "   ", r.eCFromBeangame(Key.bg("cosmiceye")), r.eCFromBeangame(Key.bg("itemmagnet")), r.eCFromBeangame(Key.bg("heartofiron")));
        return null;
    }

    @Override
    public String getName() {
        return "§9Magnemite";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Zaps all nearby enemies within",
            "§66 blocks with lightning that",
            "§6deals damage and applies on-hit",
            "§6effects every 3 seconds.",
            "",
            "§6Armor",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.CARVED_PUMPKIN;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
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
        return EquipmentSlotGroup.HEAD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

