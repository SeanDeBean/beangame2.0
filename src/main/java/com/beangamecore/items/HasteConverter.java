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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGLPTalismanI;

public class HasteConverter extends BeangameItem implements BGLPTalismanI {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 1, false, false));
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "hasteconverter";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "NCI", "DBS", "GEW", r.mCFromMaterial(Material.NETHERITE_PICKAXE), r.eCFromBeangame(Key.bg("converterpickaxe")), r.mCFromMaterial(Material.IRON_PICKAXE), r.mCFromMaterial(Material.DIAMOND_PICKAXE), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STONE_PICKAXE), r.mCFromMaterial(Material.GOLDEN_PICKAXE), r.eCFromBeangame(Key.bg("explosivepickaxe")), r.mCFromMaterial(Material.WOODEN_PICKAXE));
        return null;
    }

    @Override
    public String getName() {
        return "§8Haste Converter";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Grants Haste II effect while held.",
            "§3Haste reduces item cooldowns and",
            "§3improves mining speed.",
            "",
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
        return Material.BLAST_FURNACE;
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

