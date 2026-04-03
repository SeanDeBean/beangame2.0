package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class TestamentToTheDragon extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0F, 1.0F);
        DragonFireball testamenttothedragonfireball = (DragonFireball)player.launchProjectile(DragonFireball.class);
        loc.setY(loc.getY() + 1.5D);
        testamenttothedragonfireball.setVelocity(player.getEyeLocation().getDirection().multiply(1));
        // despawn fireballs
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                if(testamenttothedragonfireball != null){
                    testamenttothedragonfireball.remove();
                }
            }
        }, 240L);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 14500L;
    }

    @Override
    public String getId() {
        return "testamenttothedragon";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "PD ", "BW ", "   ", r.mCFromMaterial(Material.BLAZE_POWDER), r.mCFromMaterial(Material.DRAGON_BREATH), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.WRITABLE_BOOK));
        return null;
    }

    @Override
    public String getName() {
        return "§5Testament to The Dragon";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to shoot a powerful",
            "§9dragon fireball that travels forward.",
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
        return Material.ENCHANTED_BOOK;
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

