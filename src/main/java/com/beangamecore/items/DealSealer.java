package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.death.BGGlobalDeath;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class DealSealer extends BeangameItem implements BGRClickableI, BGGlobalDeath {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        dealsealerOpenMenu(event.getPlayer());
        return true;
    }

    @SuppressWarnings("deprecation")
    private void dealsealerOpenMenu(Player player){
        Merchant merchant = Bukkit.createMerchant("Deal Sealer Merchant");
        List<MerchantRecipe> recipes = loadMerchant(merchant);
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, false);
    }

    private List<MerchantRecipe> loadMerchant(Merchant merchant){
        List<MerchantRecipe> recipes = new ArrayList<>();
        ItemStack coin = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:coin")).asItem();
        coin.setAmount(1);

        ItemStack item = new ItemStack(Material.DIRT, 64);
        MerchantRecipe dirtrecipe = new MerchantRecipe(coin, 0, 9999, true);
        dirtrecipe.addIngredient(item);
        dirtrecipe.addIngredient(item);
        recipes.add(dirtrecipe);

        item = new ItemStack(Material.COBBLESTONE, 48);
        MerchantRecipe cobblestonerecipe = new MerchantRecipe(coin, 0, 9999, true);
        cobblestonerecipe.addIngredient(item);
        recipes.add(cobblestonerecipe);

        item = new ItemStack(Material.COPPER_INGOT, 12);
        MerchantRecipe copperrecipe = new MerchantRecipe(coin, 0, 9999, true);
        copperrecipe.addIngredient(item);
        recipes.add(copperrecipe);
        
        item = new ItemStack(Material.IRON_INGOT, 6);
        MerchantRecipe ironrecipe = new MerchantRecipe(coin, 0, 9999, true);
        ironrecipe.addIngredient(item);
        recipes.add(ironrecipe);

        item = new ItemStack(Material.DIAMOND, 1);
        MerchantRecipe diamondrecipe = new MerchantRecipe(coin, 0, 9999, true);
        diamondrecipe.addIngredient(item);
        recipes.add(diamondrecipe);

        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 16);
        coin.setAmount(1);
        MerchantRecipe steakrecipe = new MerchantRecipe(steak, 0, 9999, true);
        steakrecipe.addIngredient(coin);
        recipes.add(steakrecipe);

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION, 1);
        coin.setAmount(6);
        MerchantRecipe helmetrecipe = new MerchantRecipe(helmet, 0, 9999, true);
        helmetrecipe.addIngredient(coin);
        recipes.add(helmetrecipe);

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION, 2);
        coin.setAmount(9);
        MerchantRecipe chestplaterecipe = new MerchantRecipe(chestplate, 0, 9999, true);
        chestplaterecipe.addIngredient(coin);
        recipes.add(chestplaterecipe);

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION, 2);
        coin.setAmount(8);
        MerchantRecipe leggingsrecipe = new MerchantRecipe(leggings, 0, 9999, true);
        leggingsrecipe.addIngredient(coin);
        recipes.add(leggingsrecipe);

        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION, 1);
        coin.setAmount(5);
        MerchantRecipe bootsrecipe = new MerchantRecipe(boots, 0, 9999, true);
        bootsrecipe.addIngredient(coin);
        recipes.add(bootsrecipe);

        ItemStack revive = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:revive")).asItem();
        coin.setAmount(24);
        MerchantRecipe reviverecipe = new MerchantRecipe(revive, 0, 9999, true);
        reviverecipe.addIngredient(coin);
        recipes.add(reviverecipe);

        ItemStack beanchronicles = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:beanchronicles")).asItem();
        coin.setAmount(48);
        MerchantRecipe beanchroniclesrecipe = new MerchantRecipe(beanchronicles, 0, 9999, true);
        beanchroniclesrecipe.addIngredient(coin);
        recipes.add(beanchroniclesrecipe);

        coin.setAmount(1);

        return recipes;
    }

    @Override
    public void onGlobalDeath(Player player, ItemStack item){
        double chance = Math.random();
        if(chance >= 0.6){
            return;
        }
        ItemStack coin = BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:coin")).asItem();
        coin.setAmount(1);
        Inventory playerInventory = player.getInventory();
        if (player.getInventory().firstEmpty() != -1) {
            playerInventory.addItem(coin);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), coin);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 102;
    }

    @Override
    public String getId() {
        return "dealsealer";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§aDeal Sealer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to open a merchant menu",
            "§awith various trades. Exchange resources",
            "§afor coins or spend coins on items like",
            "§afood, diamond armor, and beangame items.",
            "§a40% chance to drop a coin on death.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.SUNFLOWER;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

