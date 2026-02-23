package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;
import com.beangamecore.events.ServerLoad;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class BeefCity extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        //cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        AttributeInstance attribute = player.getAttribute(Attribute.SCALE);
        attribute.setBaseValue(attribute.getBaseValue() + 1);
        attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        attribute.setBaseValue(attribute.getBaseValue() + 2.5);
        attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        attribute.setBaseValue(attribute.getBaseValue() + 2.5);
        attribute = player.getAttribute(Attribute.MAX_HEALTH);
        attribute.setBaseValue(attribute.getBaseValue() + 20);
        player.setHealth(player.getHealth() + 15);
        ServerLoad.sizeadjusted.put(uuid, true);

        // reset item event
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                AttributeInstance attribute = player.getAttribute(Attribute.SCALE);
                attribute.setBaseValue(attribute.getBaseValue() - 1);
                attribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
                attribute.setBaseValue(attribute.getBaseValue() - 2.5);
                attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
                attribute.setBaseValue(attribute.getBaseValue() - 2.5);
                attribute = player.getAttribute(Attribute.MAX_HEALTH);
                attribute.setBaseValue(attribute.getBaseValue() - 20);
                ServerLoad.sizeadjusted.remove(uuid);
            }
        }, 600L);

        return true;
    }
    
    @Override
    public long getBaseCooldown() {
        return 55000L;
    }

    @Override
    public String getId() {
        return "beefcity";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " D ", "SBS", " H ", r.mCFromMaterial(Material.DRAGON_BREATH), r.mCFromMaterial(Material.SUGAR), r.eCFromBeangame(Key.bg("bean")), r.eCFromBeangame(Key.bg("heartofiron")));
        return null;
    }

    @Override
    public String getName() {
        return "§aBeef City";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to temporarily grow in",
            "§9size, increasing your health by 10",
            "§9hearts and extending your reach for",
            "§930 seconds.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BREEZE_ROD;
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
        return 1;
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

