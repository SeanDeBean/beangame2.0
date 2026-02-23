package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
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
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.util.Cooldowns;

import net.md_5.bungee.api.ChatColor;

public class SilenceOfTheLambs extends BeangameItem implements BGRClickableI, BGMPTalismanI {

    // runs every 1 second
    @Override
    public void applyTalismanEffects(Player player, ItemStack stack) {
        // Get the block directly below the player
        Location belowPlayer = player.getLocation().subtract(0, 1, 0);
        Block blockBelow = belowPlayer.getBlock();
        
        // Check if the block is grass
        if (blockBelow.getType() == Material.GRASS_BLOCK) {
            // Turn grass to dirt
            blockBelow.setType(Material.DIRT);
            
            // Spawn grass-eating particles
            player.spawnParticle(Particle.WITCH, 
                belowPlayer.add(0, 1.1, 0), // Slightly above the ground
                7, // Count
                0.3, // Offset X
                0.1, // Offset Y
                0.3, // Offset Z
                0.1 // Speed
            );
            
            // Play sheep eat grass sound
            player.getWorld().playSound(
                player.getLocation(),
                Sound.BLOCK_GRASS_BREAK,
                0.7f, // Volume
                1.0f  // Pitch
            );
            
            // Give the player Speed 1 for 3 seconds (60 ticks)
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                60, // Duration in ticks (3 seconds)
                0,  // Amplifier (0 = Speed I)
                true, // Ambient
                false, // Show particles
                false  // Show icon
            ));
        }
    }

    private final ConcurrentHashMap<UUID, List<SheepData>> playerSheepMap = new ConcurrentHashMap<>();
    
    private class SheepData {
        public final Sheep sheep;
        public final int colorTask;
        public final int silenceTask;
        
        public SheepData(Sheep sheep, int colorTask, int silenceTask) {
            this.sheep = sheep;
            this.colorTask = colorTask;
            this.silenceTask = silenceTask;
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        event.setCancelled(true);

        // Check if player is on cooldown
        if (onCooldown(playerId)) {
            sendCooldownMessage(player);
            return false;
        }

        // Apply cooldown
        applyCooldown(playerId);

        // Spawn the sheep
        Sheep sheep = player.getWorld().spawn(player.getLocation(), Sheep.class);
        sheep.setCustomName(player.getName() + "'s Lamb");
        sheep.setCustomNameVisible(false);
        sheep.setAI(true);
        sheep.setSilent(true); // Silence of the Lambs!
        
        // Initialize player's sheep list if needed
        playerSheepMap.putIfAbsent(playerId, Collections.synchronizedList(new ArrayList<>()));

        // Create and start color cycling task (lambda version)
        int[] colorTaskId = new int[1];
        int[] colorIndex = {0};
        DyeColor[] colors = {
            DyeColor.WHITE, DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.GRAY,
            DyeColor.LIGHT_GRAY, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE,
            DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED, DyeColor.BLACK
        };
        
        colorTaskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (isSheepInvalid(sheep)) {
                Bukkit.getScheduler().cancelTask(colorTaskId[0]);
                return;
            }
            
            sheep.setColor(colors[colorIndex[0]]);
            colorIndex[0] = (colorIndex[0] + 1) % colors.length;
            
            // Visual effect particles
            sheep.getWorld().spawnParticle(Particle.WITCH, 
                sheep.getLocation().add(0, 1, 0), 
                10, 0.3, 0.5, 0.3, 0);
        }, 0L, 4L).getTaskId(); // Cycle colors every 4 ticks (0.2 seconds)

        // Create and start silence application task (lambda version)
        int[] silenceTaskId = new int[1];
        silenceTaskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (isSheepInvalid(sheep)) {
                Bukkit.getScheduler().cancelTask(silenceTaskId[0]);
                return;
            }

            // Apply silence to nearby players
            for (Player nearbyPlayer : sheep.getWorld().getPlayers()) {
                // Skip creative/spectator players
                if (nearbyPlayer.getGameMode() == GameMode.CREATIVE || 
                    nearbyPlayer.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }

                // Check if player is within 5 blocks
                if (nearbyPlayer.getLocation().distance(sheep.getLocation()) <= 5.0) {
                    Cooldowns.setCooldown("silenced", nearbyPlayer.getUniqueId(), 2000L);

                    nearbyPlayer.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,
                        nearbyPlayer.getLocation().add(0, 1, 0),
                        5, 0.3, 0.5, 0.3, 0);
                }
            }
        }, 0L, 20L).getTaskId(); // Check every second

        SheepData sheepData = new SheepData(sheep, colorTaskId[0], silenceTaskId[0]);
        playerSheepMap.get(playerId).add(sheepData);

        // Schedule despawn after 12 seconds (lambda version)
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            cleanupSheep(playerId, sheepData);
        }, 12 * 20L); // 12 seconds * 20 ticks

        // Play sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHEEP_AMBIENT, 1.0f, 0.5f);
        
        return true;
    }

    private boolean isSheepInvalid(Sheep sheep) {
        return sheep == null || !sheep.isValid() || sheep.isDead();
    }

    private void cleanupSheep(UUID playerId, SheepData sheepData) {
        // Cancel tasks - FIXED: Use Bukkit.getScheduler().cancelTask() for task IDs
        if (sheepData.colorTask > 0) {
            Bukkit.getScheduler().cancelTask(sheepData.colorTask);
        }
        
        if (sheepData.silenceTask > 0) {
            Bukkit.getScheduler().cancelTask(sheepData.silenceTask);
        }
        
        // Remove sheep
        if (sheepData.sheep != null && sheepData.sheep.isValid()) {
            // Death effect
            sheepData.sheep.getWorld().spawnParticle(Particle.CLOUD, 
                sheepData.sheep.getLocation(), 20, 0.3, 0.5, 0.3, 0.1);
            sheepData.sheep.getWorld().playSound(sheepData.sheep.getLocation(), 
                Sound.ENTITY_SHEEP_DEATH, 1.0f, 1.5f);
            sheepData.sheep.remove();
        }
        
        // Remove from player's sheep list
        if (playerSheepMap.containsKey(playerId)) {
            playerSheepMap.get(playerId).remove(sheepData);
            
            // Clean up empty lists
            if (playerSheepMap.get(playerId).isEmpty()) {
                playerSheepMap.remove(playerId);
            }
        }
    }

    // Method to cleanup a specific sheep if it dies prematurely
    public void onSheepDeath(Sheep sheep) {
        for (Map.Entry<UUID, List<SheepData>> entry : playerSheepMap.entrySet()) {
            Iterator<SheepData> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                SheepData sheepData = iterator.next();
                if (sheepData.sheep != null && sheepData.sheep.equals(sheep)) {
                    cleanupSheep(entry.getKey(), sheepData);
                    break;
                }
            }
        }
    }

    // Cleanup method to call on server shutdown or reload
    public void cleanupAll() {
        for (UUID playerId : playerSheepMap.keySet()) {
            // Create a copy to avoid ConcurrentModificationException
            List<SheepData> sheepList = new ArrayList<>(playerSheepMap.get(playerId));
            for (SheepData sheepData : sheepList) {
                cleanupSheep(playerId, sheepData);
            }
        }
        playerSheepMap.clear();
    }

    // Rest of the methods remain the same...
    @Override
    public long getBaseCooldown() {
        return 15000L; // Reduced to 15 seconds to encourage multiple lamb usage
    }

    @Override
    public String getId() {
        return "silenceofthelambs";
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
        return ChatColor.LIGHT_PURPLE + "Silence of the Lambs";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon a chromatic lamb",
            "§9that cycles through all wool colors.",
            "§9The lamb emits a silencing aura that",
            "§9affects all players within 5 blocks.",
            "§9The lamb disappears after 12 seconds.",
            "",
            "§9Castable",
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
        return Material.SHEEP_SPAWN_EGG;
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
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot() {
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}