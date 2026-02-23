package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.items.type.damage.entity.BGDReceiverArmorI;

public class ShrapnelShirt extends BeangameItem implements BGArmorI, BGDReceiverArmorI{
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        player.damage(1, player);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                Double health = player.getHealth() + 1;
                if(health <= player.getAttribute(Attribute.MAX_HEALTH).getValue()){
                    player.setHealth(health);
                }
            }
        }, 1L);
    }

    @Override
    public void victimOnHitArmor(EntityDamageByEntityEvent event, ItemStack armor) {
        if(event.getCause().equals(DamageCause.BLOCK_EXPLOSION) || event.getCause().equals(DamageCause.ENTITY_EXPLOSION)){
            // half the damage
            event.setDamage(event.getDamage() * 0.5);
        }  
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "shrapnelshirt";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "T T", "DCD", "DHD", r.mCFromMaterial(Material.TNT), r.mCFromMaterial(Material.POINTED_DRIPSTONE), r.mCFromMaterial(Material.IRON_CHESTPLATE), r.eCFromBeangame(Key.bg("heartofiron")));
        return null;
    }

    @Override
    public String getName() {
        return "§4Shrapnel Shirt";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Takes 0.5 hearts of damage every",
            "§63 seconds then immediately heals it.",
            "§6Reduces explosion damage by 50%",
            "§6before the effect of Blast Protection",
            "",
            "§6Armor",
            "§3Talisman",
            "§aSupport",
            "§dOn Hit Extender",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+4 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:blast_protection", 8);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_CHESTPLATE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.REDSTONE, TrimPattern.BOLT);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.CHEST;
    }

    @Override
    public int getArmor(){
        return 4;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

