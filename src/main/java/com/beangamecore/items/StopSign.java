package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.util.Cooldowns;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class StopSign extends BeangameItem implements BGDDealerHeldI {

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if(event.getEntity() instanceof Player victim){
            UUID vuuid = victim.getUniqueId();
            if(onCooldown(vuuid)){
                return;
            }
            setCooldown(vuuid, 4000);
            Cooldowns.setCooldown("use_item", vuuid, 4500L);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "stopsign";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " RW", " SR", "T  ", r.mCFromMaterial(Material.RED_CONCRETE), r.mCFromMaterial(Material.WHITE_CONCRETE), r.eCFromBeangame(Key.bg("slotenforcer")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§cStop Sign";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cPrevents enemies from right-clicking",
            "§cfor 4.5 seconds when you hit them.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

