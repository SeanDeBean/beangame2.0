package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.items.type.damage.BGDamageArmorI;

public class Prickleplate extends BeangameItem implements BGDamageArmorI, BGArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        player.setArrowsInBody(100);
    }

    @Override
    public void onDamageArmor(EntityDamageEvent event, ItemStack armor) {
        if (!(event.getEntity() instanceof LivingEntity entity))
            return;

        UUID uuid = entity.getUniqueId();
        if (onCooldown(uuid))
            return;
        applyCooldown(uuid);

        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null)
            return;

        damageNearbyEntities(world, loc, uuid, entity);
    }

    

    private void damageNearbyEntities(World world, Location loc, UUID uuid, LivingEntity entity) {
        for (Entity nearby : world.getNearbyEntities(loc, 3, 3, 3)) {
            if (!(nearby instanceof LivingEntity target))
                continue;
            if (target.getUniqueId().equals(uuid))
                continue;

            // Prevent self-attribution issues
            if (!target.isValid() || target.isDead())
                continue;

            try {
                target.damage(1.0, entity);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Prickleplate failed to damage " + target.getType() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 333;
    }

    @Override
    public String getId() {
        return "prickleplate";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CRC", "CPC", "CBC", r.mCFromMaterial(Material.CACTUS), r.mCFromMaterial(Material.LIGHTNING_ROD), r.mCFromMaterial(Material.IRON_CHESTPLATE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§2Prickleplate";
    }


    @Override
    public List<String> getLore() {
        return List.of(
            "§6Damages all nearby entities within",
            "§63 blocks when you take damage.",
            "§6Deals 0.5 hearts of damage with",
            "§60.33 second cooldown.",
            "",
            "§6Armor",
            "§dOn Hit Extender",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+8 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:thorns", 5);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DYE);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.RIB);
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(60, 180, 50);
    }

    @Override
    public int getArmor(){
        return 8;
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

