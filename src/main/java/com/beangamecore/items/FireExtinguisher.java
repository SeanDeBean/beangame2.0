package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.Key;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class FireExtinguisher extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        event.setCancelled(true);
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // item event
        Cooldowns.setCooldown("fall_damage_immunity", uuid, 5000L);
        sprayFoam(player);
        
        return true;
    }

    public void sprayFoam(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return; // do nothing in spectator mode
        }

        final World world = player.getWorld();
        final int sprayTicks = 5 * 20; // 5 seconds
        final double sprayRange = 10.0;
        final double stepSize = 1.0;
        
        // Use arrays for mutable variables
        int[] ticks = {0};
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (world == null || !Bukkit.getWorlds().contains(player.getWorld())) {
                Bukkit.getScheduler().cancelTask(taskId[0]); // World is gone, just stop the task
                return;
            }

            ticks[0]++;

            if (shouldCancelSpray(player, ticks[0], sprayTicks)) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Location eyeLoc = player.getEyeLocation();
            Vector direction = eyeLoc.getDirection().normalize();

            // Push player backward (reduced strength)
            Vector backward = direction.clone().multiply(-0.08);

            if(!(player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
                player.setVelocity(player.getVelocity().add(backward));
            }

            sprayFoamStep(world, player, eyeLoc, direction, sprayRange, stepSize, ticks[0]);

            // Sound effect at player location
            world.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.2f, 1.0f);
        }, 2L, 1L).getTaskId(); // delay 2 ticks, repeat every tick
    }

    private boolean shouldCancelSpray(Player player, int ticks, int sprayTicks) {
        return ticks++ >= sprayTicks || !player.isOnline() || player.getGameMode() == GameMode.SPECTATOR;
    }

    private void sprayFoamStep(World world, Player player, Location eyeLoc, Vector direction, double sprayRange,
            double stepSize, int ticks) {
        for (double i = 0; i < sprayRange; i += stepSize) {
            Location point = eyeLoc.clone().add(direction.clone().multiply(i));

            handleBlockEffects(point.getBlock());

            extinguishAndDamageEntities(world, player, point, ticks);

            // Particle spray at each foam step
            world.spawnParticle(Particle.CLOUD, point, 3, 0.2, 0.2, 0.2, 0.01);
        }
    }

    private void handleBlockEffects(Block block) {
        // Block effects
        if (block.getType() == Material.FIRE) {
            block.setType(Material.AIR);
        } else if (block.getType() == Material.LAVA || block.getType() == Material.MAGMA_BLOCK) {
            block.setType(Material.OBSIDIAN);
        } else if (block.getType() == Material.LAVA_CAULDRON) {
            block.setType(Material.CAULDRON);
        } else if (block.getType() == Material.SOUL_CAMPFIRE || block.getType() == Material.CAMPFIRE) {
            block.setType(Material.AIR);
        }
    }

    private void extinguishAndDamageEntities(World world, Player player, Location point, int ticks) {
        // Extinguish entities
        for (Entity entity : world.getNearbyEntities(point, 1.5, 1.5, 1.5)) {
            if (entity.getFireTicks() > 0) {
                entity.setFireTicks(0);
            }
            handleEntityDamage(entity, player, ticks);
        }
    }

    private void handleEntityDamage(Entity entity, Player player, int ticks) {
        if (shouldDamagePlayer(entity, player, ticks)) {
            Player v = (Player) entity;
            String lower = v.getName().toLowerCase();
            boolean isFireRelated = FIRE_KEYWORDS.stream().anyMatch(lower::contains);

            if (isFireRelated) {
                boolean hasFireGauntlet = BeangameItemRegistry.get(Key.bg("firegauntlet"), FireGauntlet.class)
                        .map(fg -> fg.hasFireGauntlet(v))
                        .orElse(false);

                if (!hasFireGauntlet) {
                    v.damage(1.0, player);
                }
            }
        } else if (entity instanceof Blaze v && ticks % 10 == 0) {
            v.damage(1.0, player);
        }
    }

    private boolean shouldDamagePlayer(Entity entity, Player player, int ticks) {
        return entity instanceof Player && entity != player && ticks % 10 == 0;
    }

    private static final Set<String> FIRE_KEYWORDS = Set.of("fire", "flame", "inferno", "lava", "magma", "fier", "blaze", "heat", "hot");

    @Override
    public long getBaseCooldown() {
        return 19000L;
    }

    @Override
    public String getId() {
        return "fireextinguisher";
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
        return "§cFire Extinguisher";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§7Sprays foam that extinguishes fires,",
            "§7converts lava to obsidian, and damages",
            "§7fire-themed players. Propels you backward.",
            "",
            "§7Movement",
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
        return Material.BRICK;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

