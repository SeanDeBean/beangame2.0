package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;

public class BatCape extends BeangameItem implements BGArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        byte light = player.getLocation().getBlock().getLightLevel();
        if (shouldApplyEffects(player, light)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0, false, false));
        }
    }

    private boolean shouldApplyEffects(Player player, byte light) {
        return player.getPotionEffect(PotionEffectType.BLINDNESS) != null
                || player.getPotionEffect(PotionEffectType.DARKNESS) != null
                || light <= 7;
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "batcape";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "S S", "SIS", "CBC", r.mCFromMaterial(Material.SCULK_VEIN), r.mCFromMaterial(Material.IRON_CHESTPLATE), r.mCFromMaterial(Material.SCULK_CATALYST), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§7Bat Cape";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Grants invisibility and strength when",
            "§6in low light levels or affected by",
            "§6blindness/darkness effects.",
            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+5 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DYE);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.TIDE);
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(0, 0, 0);
    }

    @Override
    public int getArmor(){
        return 5;
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

