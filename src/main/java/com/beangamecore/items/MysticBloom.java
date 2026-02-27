package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.BlockCategories;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class MysticBloom extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        Location location = player.getLocation();
        Location flowerLocation = location.clone();

        if (isInvalidFlowerLocation(flowerLocation)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§cYou cannot use the Mystic Flower here!"));
            return false;
        }

        placeMysticFlower(flowerLocation, player, uuid, location);

        startHealingAndRegenerationEffect(player, location, flowerLocation.getBlock());

        return true;
    }

    private boolean isInvalidFlowerLocation(Location flowerLocation) {
        return BlockCategories.getFunctionalBlocks().contains(flowerLocation.getBlock().getType());
    }

    private void placeMysticFlower(Location flowerLocation, Player player, UUID uuid, Location location) {
        flowerLocation.getBlock().setType(getMaterial());
        applyCooldown(uuid);
        player.getWorld().playSound(location, Sound.BLOCK_GRASS_PLACE, 1, 1);
        player.getWorld().spawnParticle(Particle.HEART, location, 20, 1, 1, 1, 0.1);
    }

    private void startHealingAndRegenerationEffect(Player player, Location location, Block flowerBlock) {
        // Use arrays for mutable variables
        int[] ticks = {0};
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (flowerBlock.getType() != Material.BLUE_ORCHID) {
                Bukkit.getScheduler().cancelTask(taskId[0]); // Stop the task if the flower is broken
                return;
            }

            if (ticks[0] >= 400) { // 20 seconds
                Bukkit.getScheduler().cancelTask(taskId[0]);
                if (flowerBlock.getType().equals(Material.BLUE_ORCHID)) {
                    flowerBlock.setType(Material.AIR);
                }
                return;
            }

            player.getWorld().spawnParticle(Particle.HEART, flowerBlock.getLocation().clone().add(0.5, 0.25, 0.5),
                    2);

            applyEffectsToNearbyPlayers(player, location);

            ticks[0] += 20;
        }, 0, 20).getTaskId();
    }

    private void applyEffectsToNearbyPlayers(Player player, Location location) {
        for (Player nearbyPlayer : Bukkit.getOnlinePlayers()) {
            if (nearbyPlayer.getWorld() == location.getWorld() && nearbyPlayer.getLocation().distance(location) <= 9) {
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1, false, true, true));
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 70, 0, false, true, true));
                nearbyPlayer.getWorld().spawnParticle(Particle.HEART, nearbyPlayer.getLocation().add(0, 1, 0), 2);
            }
        }
    }
    @Override
    public long getBaseCooldown() {
        return 30000L;
    }

    @Override
    public String getId() {
        return "mysticbloom";
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
        return "§bMystic Bloom";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to place a flower that",
            "§agrants regeneration II and resistance",
            "§ato all players within 9 blocks for",
            "§a20 seconds. Flower breaks after",
            "§aduration ends.",
            "",
            "§aSupport",
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
        return Material.BLUE_ORCHID;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

}

