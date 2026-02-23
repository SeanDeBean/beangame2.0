package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGToolI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CookedMeatPickaxe extends BeangameItem implements BGToolI {
    
    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item){
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location loc = event.getBlock().getLocation();
        // item event
        double random = Math.random();
        if(random > 0.100001){
            return;
        } else if (random < 0.02D) {
            world.dropItemNaturally(loc, new ItemStack(Material.COOKED_PORKCHOP, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cPork!"));
        } else if (random < 0.04D) {
            world.dropItemNaturally(loc, new ItemStack(Material.COOKED_BEEF, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cBeef!"));
        } else if (random < 0.06D) {
            world.dropItemNaturally(loc, new ItemStack(Material.COOKED_CHICKEN, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cChicken!"));
        } else if (random < 0.08D) {
            world.dropItemNaturally(loc, new ItemStack(Material.COOKED_RABBIT, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cRabbit!"));
        } else if (random < 0.1D) {
            world.dropItemNaturally(loc, new ItemStack(Material.COOKED_MUTTON, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cMutton!"));
        } else {
            world.dropItemNaturally(loc, new ItemStack(Material.CAKE, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Baked?"));
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "cookedmeatpickaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public boolean isInFoodItemRotation(){
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§cCooked Meat Pickaxe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Breaking blocks has a 10% chance to",
            "§2drop random cooked meats or cake.",
            "§2Chance for pork, beef, chicken, rabbit",
            "§2or mutton",
            "",
            "§2Food",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:efficiency", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_PICKAXE;
    }

    @Override
    public int getCustomModelData() {
        return 105;
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

