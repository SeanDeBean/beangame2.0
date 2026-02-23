package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.damage.BGLateDamageInvI;
import com.beangamecore.items.type.talisman.BGInvUnstackable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class SpoonsChance extends BeangameItem implements BGLateDamageInvI, BGInvUnstackable {
    
    @Override
    public boolean onLateDamageInventory(EntityDamageEvent event, ItemStack item) {
        if(!(event.getEntity() instanceof Player victim)){
            return false;
        }
        if(victim.getGameMode().equals(GameMode.SPECTATOR)){
            return false;
        }
        if(event.isCancelled()){
            return false;
        }
        if(event.getFinalDamage() >= victim.getHealth() + victim.getAbsorptionAmount()){
            ItemStack offhandsave = new ItemStack(victim.getEquipment().getItemInOffHand());
            if(offhandsave.getType().equals(Material.TOTEM_OF_UNDYING)) return false;
            victim.getEquipment().setItemInOffHand(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:spoonschance")).asItem());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> victim.getEquipment().setItemInOffHand(offhandsave), 1L);
            item.setAmount(0);
            activeSpoonsChance.add(victim.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                activeSpoonsChance.remove(victim.getUniqueId());
            }, 2);
        }
        return true;
    }

    private static List<UUID> activeSpoonsChance = new ArrayList<>();

    public static boolean hasActivatedSpoonsChance(Player player){
        if(activeSpoonsChance.contains(player.getUniqueId())){
            return true;
        }
        return false;
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "spoonschance";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " TB", "  G", "   ", r.mCFromMaterial(Material.TOTEM_OF_UNDYING), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.GOLDEN_APPLE));
        return null;
    }

    @Override
    public String getName() {
        return "§8Spoon's Chance";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Acts as a Totem of Undying.",
            "§3Works from anywhere in your inventory.",
            "",
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
        return Material.TOTEM_OF_UNDYING;
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

