package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Stray;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;

public class DayForTheErased extends BeangameItem implements BGProjectileI, BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
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
        Location loc = player.getEyeLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_POLAR_BEAR_WARNING, 1.0F, 0);
        launchProjectile(this, player, Snowball.class);
        return true;
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        event.getEntity().remove();
        Player player = (Player) event.getEntity().getShooter();
        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();
        
        // Store references to the spawned strays
        List<Stray> spawnedStrays = new ArrayList<>();
        
        // Define spawn positions and equipment configurations
        SpawnConfig[] configs = {
            new SpawnConfig(0, 0, true),    // Center with sword
            new SpawnConfig(1, 0, false),   // Right without sword
            new SpawnConfig(-1, 0, true),   // Left with sword  
            new SpawnConfig(0, 1, true),    // Forward with sword
            new SpawnConfig(0, -1, true)    // Back with sword
        };
        
        for (SpawnConfig config : configs) {
            Location spawnLoc = loc.clone().add(config.xOffset, 0, config.zOffset);
            Stray stray = spawnConfiguredStray(world, spawnLoc, player, config.hasSword);
            spawnedStrays.add(stray);
        }
        
        // Schedule cleanup for only these specific strays
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                for (Stray stray : spawnedStrays) {
                    // Check if the stray still exists and is valid before trying to kill it
                    if (stray.isValid() && !stray.isDead()) {
                        stray.setHealth(0);
                    }
                }
            }
        }, 320L);
    }

    private Stray spawnConfiguredStray(World world, Location location, Player owner, boolean hasSword) {
        Stray stray = (Stray) world.spawnEntity(location, EntityType.STRAY);
        stray.setCustomName(owner.getName() + "'s stray");
        
        EntityEquipment equipment = stray.getEquipment();
        equipment.setHelmet(new ItemStack(Material.LEATHER_HELMET));
        
        if (hasSword) {
            equipment.setItemInMainHand(new ItemStack(Material.STONE_SWORD));
        }
        
        stray.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0));
        
        return stray;
    }

    private static class SpawnConfig {
        final int xOffset;
        final int zOffset;
        final boolean hasSword;
        
        SpawnConfig(int xOffset, int zOffset, boolean hasSword) {
            this.xOffset = xOffset;
            this.zOffset = zOffset;
            this.hasSword = hasSword;
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 32000L;
    }

    @Override
    public String getId() {
        return "dayfortheerased";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " SB", " KS", "C  ", r.mCFromMaterial(Material.SCULK_CATALYST), r.mCFromMaterial(Material.SCULK_SENSOR), r.eCFromBeangame(Key.bg("dayfortheerased")), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§8Day For The Erased";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to launch a snowball that",
            "§9summons 5 strays on impact. 4 strays",
            "§9wield stone swords while 1 has a bow.",
            "§9All strays have speed and despawn",
            "§9after 16 seconds.",
            "",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.ECHO_SHARD;
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
