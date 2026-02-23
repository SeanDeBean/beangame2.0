package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class BastionBarracks extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        applyCooldown(uuid);

        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);

        List<PiglinBrute> spawnedBrutes = spawnBrutes(player, loc);

        scheduleBruteCleanup(spawnedBrutes);

        return true;
    }

    private List<PiglinBrute> spawnBrutes(Player player, Location loc) {
        // Store references to the spawned brutes
        List<PiglinBrute> spawnedBrutes = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Entity entity = player.getWorld().spawnEntity(loc, EntityType.PIGLIN_BRUTE);
            if (entity instanceof PiglinBrute) {
                PiglinBrute brute = (PiglinBrute) entity;
                brute.setCustomName(player.getName() + "'s brute");
                brute.setVelocity(loc.getDirection().multiply(3));
                spawnedBrutes.add(brute);
            }
        }
        return spawnedBrutes;
    }

    private void scheduleBruteCleanup(List<PiglinBrute> spawnedBrutes) {
        // Schedule cleanup for only these specific brutes
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                for (PiglinBrute brute : spawnedBrutes) {
                    // Check if the brute still exists and is valid before trying to kill it
                    if (brute.isValid() && !brute.isDead()) {
                        brute.setHealth(0);
                    }
                }
            }
        }, 230L);
    }
    
    @Override
    public long getBaseCooldown() {
        return 35000L;
    }

    @Override
    public String getId() {
        return "bastionbarracks";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " H ", "GBG", " S ", r.mCFromMaterial(Material.PIGLIN_HEAD), r.mCFromMaterial(Material.GILDED_BLACKSTONE), r.eCFromBeangame(Key.bg("bean")), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§6Bastion Barracks";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon 3 piglin brutes",
            "§9that charge forward and fight for you.",
            "§9They automatically despawn after 11.5",
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
        return Material.GILDED_BLACKSTONE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

