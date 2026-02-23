package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class WardenLauncher extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // Item event - play sound and action bar message
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ITEM_GOAT_HORN_SOUND_6, 3.0F, 1.0F);

        // Warden summon
        Warden warden = (Warden) player.getWorld().spawnEntity(loc, EntityType.WARDEN);
        warden.setCustomName(player.getName() + "'s warden");
        warden.setVelocity(loc.getDirection().multiply(3));

        // Timed task to despawn this specific warden after 48 seconds (960 ticks)
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (warden.isValid() && !warden.isDead()) {
                warden.setHealth(0);
            }
        }, 960L);
        
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 65000L;
    }

    @Override
    public String getId() {
        return "wardenlauncher";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " SB", " KS", "C  ", r.mCFromMaterial(Material.SNOWBALL), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.BONE), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§3Warden Launcher";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon and launch a",
            "§9warden forward that will fight for you.",
            "§9It will automatically despawn after 48",
            "§9seconds.",
            "",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.ECHO_SHARD;
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

