package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.BGDamageInvI;

public class MedievalVaccine extends BeangameItem implements BGDamageInvI {

    @Override
    public void onDamageInventory(EntityDamageEvent event, ItemStack item){
        DamageCause cause = event.getCause();
        if(cause.equals(DamageCause.POISON) || cause.equals(DamageCause.WITHER)){
            event.setCancelled(true);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0L;
    }

    @Override
    public String getId() {
        return "medievalvaccine";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "   ", "WCP", "   ", r.mCFromMaterial(Material.WITHER_ROSE), r.eCFromBeangame(Key.bg("cleanse")), r.mCFromMaterial(Material.PUFFERFISH));
        return null;
    }

    @Override
    public String getName() {
        return "§2Medieval Vaccine";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Grants complete immunity to poison",
            "§3and wither damage while carried",
            "§3in inventory.",
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
        return Material.POISONOUS_POTATO;
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
