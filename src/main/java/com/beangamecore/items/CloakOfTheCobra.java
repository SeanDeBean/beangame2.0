package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CloakOfTheCobra extends BeangameItem implements BGDDealerInvI {
    
    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity entity = (LivingEntity) event.getEntity();
        entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 120, 0));
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "cloakofthecobra";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "WSW", "WDW", "S S", r.mCFromMaterial(Material.LIME_WOOL), r.mCFromMaterial(Material.SLIME_BALL), r.eCFromBeangame(Key.bg("drunicedge")));
        return null;
    }

    @Override
    public String getName() {
        return "§aCloak of The Cobra";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3All attacks apply Poison I for 4.5",
            "§3seconds to enemies. Carrying this item",
            "§3makes your strikes venomous.",
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
        return Material.LIME_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

