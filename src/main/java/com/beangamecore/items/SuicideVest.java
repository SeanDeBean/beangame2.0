package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.death.BGDeathArmorI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class SuicideVest extends BeangameItem implements BGDeathArmorI {
    
    @Override
    public void onDeathArmor(EntityDeathEvent event, ItemStack armor) {
        LivingEntity entity = event.getEntity();
        if (!isValidEntity(entity))
            return;

        UUID uuid = entity.getUniqueId();
        if (uuid == null)
            return;

        // Ensure only player or mob (LivingEntity) calls explosion
        if (!isPlayerOrMob(entity))
            return;

        if (!Cooldowns.onCooldown("use_item", uuid)) {
            armor.setAmount(0);
            Location loc = entity.getLocation();
            World world = entity.getWorld();

            createExplosionAtDeath(entity, loc, world);
        }
    }

    private boolean isValidEntity(LivingEntity entity) {
        return entity != null;
    }

    private boolean isPlayerOrMob(LivingEntity entity) {
        return (entity instanceof Player || entity instanceof Mob);
    }

    private void createExplosionAtDeath(LivingEntity entity, Location loc, World world) {
        // safer explosion attribution
        if (entity.isValid() && !entity.isDead()) {
            try {
                world.createExplosion(loc, 5.0F, false, true, entity);
            } catch (Exception e) {
                // fallback without attribution
                world.createExplosion(loc, 5.0F, false, true);
            }
        } else {
            world.createExplosion(loc, 5.0F, false, true);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "suicidevest";
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
        return "§4Suicide Vest";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Explodes with a powerful blast when",
            "§6the wearer dies, destroying the vest.",
            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+3 Armor"
        );
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:blast_protection", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public int getCustomModelData() {
        return 105;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DYE);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.REDSTONE, TrimPattern.SENTRY);
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(139,0,0);
    }

    @Override
    public int getArmor(){
        return 3;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.CHEST;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

