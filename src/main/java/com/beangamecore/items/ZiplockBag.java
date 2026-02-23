package com.beangamecore.items;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class ZiplockBag extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {

        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)){
            return false;
        }
        applyCooldown(uuid);

        Material randomFood = FOOD_ITEMS.get(ThreadLocalRandom.current().nextInt(FOOD_ITEMS.size()));
        ItemStack item = new ItemStack(randomFood);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        return true;
    }

    private static final List<Material> FOOD_ITEMS = Collections.unmodifiableList(Arrays.asList(
        Material.APPLE,
        Material.BAKED_POTATO,
        Material.BEETROOT,
        Material.BEETROOT_SOUP,
        Material.BREAD,
        Material.CAKE,
        Material.CARROT,
        Material.COOKED_BEEF,
        Material.COOKED_CHICKEN,
        Material.COOKED_COD,
        Material.COOKED_MUTTON,
        Material.COOKED_PORKCHOP,
        Material.COOKED_RABBIT,
        Material.COOKIE,
        Material.DRIED_KELP,
        Material.GOLDEN_CARROT,
        Material.GOLDEN_APPLE,
        Material.HONEY_BOTTLE,
        Material.MELON_SLICE,
        Material.MUSHROOM_STEW,
        Material.POTATO,
        Material.PUMPKIN_PIE,
        Material.RABBIT_STEW,
        Material.ROTTEN_FLESH,
        Material.SPIDER_EYE,
        Material.SWEET_BERRIES
    ));
    
    @Override
    public long getBaseCooldown() {
        return 320L;
    }

    @Override
    public String getId() {
        return "ziplockbag";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public boolean isInFoodItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§fZiplock Bag";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Right-click to receive a random food item.",
            "§2An endless bag of edible treasures.",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.RABBIT_HIDE;
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

