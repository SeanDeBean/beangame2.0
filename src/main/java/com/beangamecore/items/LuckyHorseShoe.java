package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.BGDamageInvI;

public class LuckyHorseShoe extends BeangameItem implements BGDamageInvI {

    @Override
    public void onDamageInventory(EntityDamageEvent event, ItemStack item){
        if(event.getCause().equals(DamageCause.FALL)){
            event.getEntity().getWorld().spawnParticle(Particle.CLOUD, event.getEntity().getLocation(), 10);
            event.setCancelled(true);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0L;
    }

    @Override
    public String getId() {
        return "luckyhorseshoe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " B ", "GWG", "H H", r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.GOLD_BLOCK), r.mCFromMaterial(Material.WATER_BUCKET), r.mCFromMaterial(Material.HAY_BLOCK));
        return null;
    }

    @Override
    public String getName() {
        return "§6Lucky Horseshoe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Grants complete immunity to fall",
            "§3damage while carried in inventory.",
            "§3Spawns cloud particles when",
            "§3preventing fall damage.",
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
        return Material.FEATHER;
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

