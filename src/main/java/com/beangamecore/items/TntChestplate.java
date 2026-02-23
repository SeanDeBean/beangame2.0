package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGSneakArmorI;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class TntChestplate extends BeangameItem implements BGSneakArmorI {

    @Override
    public void onSneakArmor(PlayerToggleSneakEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        if(player.isSneaking() || player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);
        // item event
        World world = player.getWorld();
        Location loc = player.getLocation();
        world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
        world.spawn(loc, TNTPrimed.class);
    }

    @Override
    public long getBaseCooldown() {
        return 650L;
    }

    @Override
    public String getId() {
        return "tntchestplate";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "T T", "TCT", "TRT", r.mCFromMaterial(Material.TNT), r.mCFromMaterial(Material.IRON_CHESTPLATE), r.eCFromBeangame(Key.bg("tntimer")));
        return null;
    }

    @Override
    public String getName() {
        return "§4TNT Chestplate";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Sneak to spawn primed TNT",
            "§6at your location.",
            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+4 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 3);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DYE);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.REDSTONE, TrimPattern.SENTRY);
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(255, 255, 255);
    }

    @Override
    public int getArmor(){
        return 4;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.CHEST;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

