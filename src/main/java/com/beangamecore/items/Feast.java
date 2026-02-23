package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class Feast extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        handleFeastForAllPlayers(loc);
        feastParticles(loc);
        return true;
    }

    private void handleFeastForAllPlayers(Location loc) {
        for (Player feastPlayer : Bukkit.getOnlinePlayers()) {
            if (isEligibleForFeast(feastPlayer, loc)) {
                scheduleFeastEffects(feastPlayer);
            }
        }
    }

    private void scheduleFeastEffects(Player feastPlayer) {
        for (int i = 0; i < 7; i++) {
            final Player affected = feastPlayer; // Use a final or effectively final variable for inner classes
            final int delay = i * 3; // Calculate delay

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
                public void run() {
                    // Update food level
                    if (affected.getFoodLevel() < 20) {
                        affected.setFoodLevel(Math.min(affected.getFoodLevel() + 1, 20)); // Cap at 20
                    }

                    // Update saturation with 40% chance
                    affected.setSaturation(Math.min(affected.getSaturation() + 1, 20)); // Cap at 20
                }
            }, delay);
        }
    }

    private boolean isEligibleForFeast(Player feastPlayer, Location loc) {
        Location vloc = feastPlayer.getLocation();
        return vloc.getWorld().equals(loc.getWorld()) &&
                vloc.distance(loc) < 24.0D &&
                feastPlayer.getGameMode().equals(GameMode.SURVIVAL);
    }

    private static final double FINAL_RADIUS = 24; // Final radius of the spiral
    private static final double CYCLES = 2.5; // Number of cycles
    private static final double TICKS_PER_CYCLE = 10; // Ticks per cycle

    public void feastParticles(Location center) {
        // Use arrays for mutable variables
        int[] tick = {0};
        final int stepsPerTick = 3; // More spiral points per tick
        final int totalTicks = (int) (CYCLES * TICKS_PER_CYCLE);
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(Main.class), () -> {
            if (tick[0] >= totalTicks) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            for (int i = 0; i < stepsPerTick; i++) {
                double step = tick[0] + (i / (double) stepsPerTick);
                double angle = 2 * Math.PI * step / TICKS_PER_CYCLE;
                double radius = FINAL_RADIUS * step / totalTicks;

                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);

                Location loc = center.clone().add(x, 0, z);
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 15, 0.1, 0.1, 0.1, 0.01); // Adjust spread if needed
            }

            tick[0]++;
        }, 0L, 1L).getTaskId();
    }

    @Override
    public long getBaseCooldown() {
        return 16000L;
    }

    @Override
    public String getId() {
        return "feast";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }
     
    @Override
    public boolean isInFoodItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " AA", "CBA", "CC ", r.mCFromMaterial(Material.GOLDEN_APPLE), r.mCFromMaterial(Material.GOLDEN_CARROT), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§2Feast";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aFeeds you and nearby players on Right-click",
            "§aRestores hunger and saturation in a",
            "§a24 block radius.",
            "",
            "§aSupport",
            "§2Food",

            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.RED_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 103;
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

