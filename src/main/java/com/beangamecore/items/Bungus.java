package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Bungus extends BeangameItem implements BGMPTalismanI, BGInvUnstackable {
    
    private static Map<UUID, Double[]> bunguspos = new HashMap<>();
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        Double x = loc.getX();
        Double y = loc.getY();
        Double z = loc.getZ();
        if (player.isDead()) {
            return;
        }
        int numBungusExtra = count(player) - 1;
        int heal = (int) Math.floor(numBungusExtra / 2) + 2;

        if (isPlayerAtStoredLocation(uuid, x, y, z)) {
            AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
            double health = attribute.getValue();
            if (player.getHealth() + heal >= health) {
                player.setHealth(health);
            } else {
                player.setHealth(player.getHealth() + heal);
            }
            player.setFoodLevel(player.getFoodLevel() + heal);
            player.setSaturation(5);
        }
        bunguspos.put(uuid, new Double[] { x, y, z });
    }

    private boolean isPlayerAtStoredLocation(UUID uuid, Double x, Double y, Double z) {
        return bunguspos.containsKey(uuid) &&
                bunguspos.get(uuid)[0].equals(x) &&
                bunguspos.get(uuid)[1].equals(y) &&
                bunguspos.get(uuid)[2].equals(z);
    }

    public int count(Player player){
        AtomicInteger force = new AtomicInteger(0);
        for(ItemStack item : player.getInventory().getContents()){
            if(this.asItem().isSimilar(item)){
                force.set(force.get() + 1);
            }
        }
        return force.get();
    }

    @Override
    public boolean isInFoodItemRotation(){
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "   ", "BFR", "GHI", r.mCFromMaterial(Material.BROWN_MUSHROOM), r.eCFromBeangame(Key.bg("feast")), r.mCFromMaterial(Material.RED_MUSHROOM), r.mCFromMaterial(Material.BROWN_MUSHROOM_BLOCK), r.mCFromMaterial(Material.RABBIT_STEW), r.mCFromMaterial(Material.RED_MUSHROOM_BLOCK));
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "bungus";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§3Bungus";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Heals and saturates you while standing",
            "§3still. Healing amount scales with",
            "§3additional Bungus items in inventory",
            "§3(2 HP + 1 per 2 extra Bungus).",
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
        return Material.BROWN_MUSHROOM;
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

