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

public class HeartOfIron extends BeangameItem implements BGLPTalismanI {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0, false, false));
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "heartofiron";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "RIN", "IBI", "CIW", r.mCFromMaterial(Material.RED_MUSHROOM), r.mCFromMaterial(Material.IRON_BLOCK), r.mCFromMaterial(Material.BROWN_MUSHROOM), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.CRIMSON_FUNGUS), r.mCFromMaterial(Material.WARPED_FUNGUS));
        return null;
    }

    @Override
    public String getName() {
        return "§f§lHeart of Iron";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Grants Resistance I effect while held.",
            "§3Reduces incoming damage by 20%",
            "§3for increased survivability.",
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
        return Material.IRON_NUGGET;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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
