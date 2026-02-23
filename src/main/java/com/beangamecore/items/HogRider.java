package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;

public class HogRider extends BeangameItem implements BGMPTalismanI, BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack itemStack){
        event.setCancelled(true);
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        // item event
        applyCooldown(uuid);
        Location loc = player.getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ITEM_GOAT_HORN_SOUND_0, 0, 0);
        
        // Store references to the spawned pigs
        List<Pig> spawnedPigs = new ArrayList<>();
        
        // Define spawn positions
        PigSpawnConfig[] configs = {
            new PigSpawnConfig(1, 0),   // Right
            new PigSpawnConfig(-1, 0),  // Left
            new PigSpawnConfig(0, 1),   // Forward
            new PigSpawnConfig(0, -1)   // Back
        };
        
        for (PigSpawnConfig config : configs) {
            Location spawnLoc = loc.clone().add(config.xOffset, 0, config.zOffset);
            Pig pig = spawnConfiguredPig(world, spawnLoc, player);
            spawnedPigs.add(pig);
        }
        
        // Schedule cleanup for only these specific pigs
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                for (Pig pig : spawnedPigs) {
                    // Check if the pig still exists and is valid before trying to kill it
                    if (pig.isValid() && !pig.isDead()) {
                        pig.setSaddle(false);
                        pig.setHealth(0);
                    }
                }
            }
        }, 500L);
        
        return true;
    }

    private Pig spawnConfiguredPig(World world, Location location, Player owner) {
        Pig pig = (Pig) world.spawnEntity(location, EntityType.PIG);
        pig.setCustomName(owner.getName() + "'s hog");
        pig.setSaddle(true);
        return pig;
    }

    private static class PigSpawnConfig {
        final int xOffset;
        final int zOffset;
        
        PigSpawnConfig(int xOffset, int zOffset) {
            this.xOffset = xOffset;
            this.zOffset = zOffset;
        }
    }

    @Override
    public long getBaseCooldown() {
        return 30000L;
    }

    @Override
    public String getId() {
        return "hogrider";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "  G", " PS", "W E", r.mCFromMaterial(Material.GLOWSTONE_DUST), r.mCFromMaterial(Material.PORKCHOP), r.mCFromMaterial(Material.STRING), r.eCFromBeangame(Key.bg("spawncore")), r.eCFromBeangame(Key.bg("berserkersessence")));
        return null;
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        if(player.getVehicle() != null){
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0));
        }
    }

    @Override
    public String getName() {
        return "§dHOG RIDER";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Summons four saddled pigs around you",
            "§9that despawn after 25 seconds. Grants",
            "§9Strength I while riding any mob.",
            "",
            "§9Summon",
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
        return Material.CARROT_ON_A_STICK;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
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

