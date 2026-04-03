package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import com.beangamecore.Main;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.items.generic.BeangameItem;

public class SuctionCupShoes extends BeangameItem {

    @Override
    public ItemStack asItem(){
        ItemStack stack = super.asItem();
        ItemMeta meta = stack.getItemMeta();
        NamespacedKey nsk1 = new NamespacedKey(Main.getPlugin(), "beangame.suctioncupshoes1");
        NamespacedKey nsk2 = new NamespacedKey(Main.getPlugin(), "beangame.suctioncupshoes2");
        NamespacedKey nsk3 = new NamespacedKey(Main.getPlugin(), "beangame.suctioncupshoes3");
        meta.addAttributeModifier(Attribute.STEP_HEIGHT, new AttributeModifier(nsk1, 7.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET));
        meta.addAttributeModifier(Attribute.JUMP_STRENGTH, new AttributeModifier(nsk2, -0.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET));
        meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(nsk3, 2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET));
        stack.setItemMeta(meta);
        return stack;
    }

    // OLD EFFECT
    // @Override
    // public void onMoveArmor(PlayerMoveEvent event, ItemStack item) {
    //     if (isNearWall(event.getPlayer().getLocation().getBlock())) {
    //         event.getPlayer().setVelocity(new Vector(0, 0.2, 0)); // Ascend when near a wall
    //         event.getPlayer().spawnParticle(Particle.ENCHANTED_HIT, event.getPlayer().getLocation(), 2);
    //     }
    // }

    // private boolean isNearWall(Block block) {
    //     return block.getRelative(1, 0, 0).getType().isSolid() ||  // East
    //            block.getRelative(-1, 0, 0).getType().isSolid() || // West
    //            block.getRelative(0, 0, 1).getType().isSolid() ||  // South
    //            block.getRelative(0, 0, -1).getType().isSolid();   // North
    // }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "suctioncupshoes";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }
    
    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "LBL", "S S", "   ", r.mCFromMaterial(Material.LADDER), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.OAK_STAIRS));
        return null;
    }

    @Override
    public String getName() {
        return "§cSuction Cup Shoes";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Allows you to climb walls.",
            "§6Grants +7.5 Step Height and",
            "§6keeps you planted to the ground.",
            "",
            "§6Armor",
            "§fMovement",
            "§9§obeangame",
            "§9", "§7When on Feet:", "§9+2 Armor", "§9+7.5 Step Height", "§9-0.5 Jump Strength"
        );
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_BOOTS;
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
    public Color getColor() {
        return Color.fromRGB(255, 255, 255);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.REDSTONE, TrimPattern.DUNE);
    }

    @Override
    public int getArmor(){
        return 2;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.FEET;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }


}

