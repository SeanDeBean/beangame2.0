package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;
import com.beangamecore.util.Key;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class TNFreeze extends BeangameItem implements BGRClickableI {
    
    private static final double MAX_RADIUS = 16.0;
    private static final long EXPANSION_DURATION = 30L;

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Early return for cooldown check
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        // Apply cooldown
        applyCooldown(uuid);

        // Handle the item event
        handleItemEvent(player);

        // Run area expansion over 1 second
        startAreaExpansion(player);

        return true;
    }

    private void handleItemEvent(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SKELETON_CONVERTED_TO_STRAY, 1f, 1f);
    }

    private void startAreaExpansion(Player player) {
        // Lambda version
        int[] taskId = new int[1];
        double[] radius = {0};
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (radius[0] >= MAX_RADIUS) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            radius[0] += MAX_RADIUS / EXPANSION_DURATION;

            // Visualize the area
            DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 255, 255), 0.9f);
            Main.getPlugin().getParticleManager().spawnParticleSphere(player.getLocation(), radius[0], Particle.DUST,
                    dustOptions, 80);

            // Exterminate unprotected entities in this radius
            handleEntitiesInRadius(player, radius[0]);
        }, 0L, 1L).getTaskId(); // Run every tick for EXPANSION_DURATION ticks
    }

    private void handleEntitiesInRadius(Player player, double radius) {
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        nearbyEntities.add(player);
        for (Entity entity : nearbyEntities) {
            Location origin = entity.getLocation();

            if (entity instanceof TNTPrimed || entity instanceof Minecart) {
                handleTNTOrMinecart(entity, origin);
            } else if (entity instanceof EnderCrystal) {
                handleEnderCrystal(entity, origin);
            } else if (entity instanceof Creeper) {
                handleCreeper(entity, origin);
            } else if (entity instanceof Player) {
                handlePlayer(entity, origin);
            }
        }
    }

    private void handlePlayer(Entity entity, Location origin) {
        Player targetPlayer = (Player) entity;
        ItemStack chestplate = targetPlayer.getEquipment().getChestplate();
        if(chestplate != null && ItemNBT.hasBeanGameTag(chestplate) && ItemNBT.isBeanGame(chestplate, BeangameItemRegistry.getRaw(Key.bg("suicidevest"), SuicideVest.class).getKey())){
            if(Cooldowns.onCooldown("immobilized", targetPlayer.getUniqueId())){
                return;
            }
            Cooldowns.setCooldown("immobilized", targetPlayer.getUniqueId(), 1300L);
            targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 0);
            return;
        }
    }

    private void handleTNTOrMinecart(Entity entity, Location origin) {
        Location air = findNearestAir(origin);
        if (air != null) {
            air.getBlock().setType(Material.RED_STAINED_GLASS);
            // Bukkit.getLogger().info("Placing TNT at: " + air);
        }
        entity.remove();
    }

    private void handleEnderCrystal(Entity entity, Location origin) {
        Location air = findNearestAir(origin);
        if (air != null) {
            air.getBlock().setType(Material.MAGENTA_STAINED_GLASS);
            // Bukkit.getLogger().info("Placing MAGENTA_STAINED_GLASS at: " + air);
        }
        entity.remove();
    }

    private void handleCreeper(Entity entity, Location origin) {
        List<Location> airBlocks = findNearestAirBlocks(origin, 2);
        for (Location loc : airBlocks) {
            loc.getBlock().setType(Material.LIME_STAINED_GLASS);
            // Bukkit.getLogger().info("Placing LIME_STAINED_GLASS at: " + airBlocks.get(0)
            // + airBlocks.get(1));
        }
        entity.remove();
    }

    private Location findNearestAir(Location origin) {
        int radius = 3;
        World world = origin.getWorld();
        if (world == null)
            return null;

        Location closest = null;
        double closestDistanceSquared = Double.MAX_VALUE;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Location check = origin.clone().add(x, y, z);
                    if (shouldSkipLocation(check))
                        continue;

                    if (isBetterAirLocation(origin, closestDistanceSquared, check)) {
                        closest = updateClosest(origin, check);
                        closestDistanceSquared = closest.distanceSquared(origin);
                    }
                }
            }
        }

        return closest;
    }

    private boolean shouldSkipLocation(Location check) {
        // if chunk isn't loaded, skip this location
        return !check.getChunk().isLoaded();
    }

    private Location updateClosest(Location origin, Location candidate) {
        // Preserve logic for updating the closest location
        return candidate;
    }

    private boolean isBetterAirLocation(Location origin, double closestDistanceSquared, Location check) {
        if (isLocationValid(check)) {
            double distSq = check.distanceSquared(origin);
            return distSq < closestDistanceSquared;
        }
        return false;
    }

    private boolean isLocationValid(Location check) {
        return check.getBlock().getType() == Material.AIR || check.getBlock().getType() == Material.WATER
                || check.getBlock().getType() == Material.BEETROOT_SEEDS;
    }

    private List<Location> findNearestAirBlocks(Location origin, int count) {
        int radius = 3;
        World world = origin.getWorld();
        if (world == null)
            return Collections.emptyList();

        List<Location> airBlocks = findAirBlocksInRadius(origin, radius);

        airBlocks.sort(Comparator.comparingDouble(loc -> loc.distanceSquared(origin)));

        return airBlocks.subList(0, Math.min(count, airBlocks.size()));
    }

    private List<Location> findAirBlocksInRadius(Location origin, int radius) {
        List<Location> airBlocks = new ArrayList<>();
        addAirBlocksInRadius(origin, radius, airBlocks);
        return airBlocks;
    }

    private void addAirBlocksInRadius(Location origin, int radius, List<Location> airBlocks) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    addIfAirBlock(origin, x, y, z, airBlocks);
                }
            }
        }
    }

    private void addIfAirBlock(Location origin, int x, int y, int z, List<Location> airBlocks) {
        Location check = origin.clone().add(x, y, z);
        if (isAirBlockOrEquivalent(check)) {
            airBlocks.add(check);
        }
    }

    private boolean isAirBlockOrEquivalent(Location location) {
        // Existing comments preserved
        return location.getChunk().isLoaded() &&
                (location.getBlock().getType() == Material.AIR
                        || location.getBlock().getType() == Material.WATER
                        || location.getBlock().getType() == Material.BEETROOT_SEEDS);
    }
    
    @Override
    public long getBaseCooldown() {
        return 12000L;
    }

    @Override
    public String getId() {
        return "tnfreeze";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§cTNFreeze";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to create an expanding area",
            "§9that freezes nearby TNT, Creepers, and",
            "§9End Crystals back into solid blocks.",
            "§9Temporarily immobilizes suicide vest users.",
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
        return Material.BROWN_DYE;
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

