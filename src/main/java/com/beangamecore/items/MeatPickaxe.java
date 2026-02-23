package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGSmeltableI;
import com.beangamecore.items.type.BGToolI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MeatPickaxe extends BeangameItem implements BGToolI, BGSmeltableI {

    @Override
    public void onBlockBreak(BlockBreakEvent event, ItemStack item) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location loc = event.getBlock().getLocation();
        // item event
        Double random = Math.random();
        if(random > 0.101){
            return;
        } else if (random < 0.02D) {
            world.dropItemNaturally(loc, new ItemStack(Material.PORKCHOP, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cPork!"));
        } else if (random < 0.04D) {
            world.dropItemNaturally(loc, new ItemStack(Material.BEEF, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cBeef!"));
        } else if (random < 0.06D) {
            world.dropItemNaturally(loc, new ItemStack(Material.CHICKEN, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cChicken!"));
        } else if (random < 0.08D) {
            world.dropItemNaturally(loc, new ItemStack(Material.RABBIT, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cRabbit!"));
        } else if (random < 0.1D) {
            world.dropItemNaturally(loc, new ItemStack(Material.MUTTON, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cMutton!"));
        } else {
            world.dropItemNaturally(loc, new ItemStack(Material.EGG, 1));
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§6Too soon!"));
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }
    @Override
    public void onSmelt(FurnaceSmeltEvent event){
        event.setResult(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cookedmeatpickaxe")).asItem());
    }
    @Override
    public String getId() {
        return "meatpickaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }
    @Override
    public boolean isInFoodItemRotation(){
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "CCC", " B ", " S ", r.mCFromMaterial(Material.BEEF), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§cMeat Pickaxe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Has a 10% chance to drop random",
            "§2raw meat when mining blocks.",
            "§2Can be smelted into cooked version.",
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
        return 103;
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

