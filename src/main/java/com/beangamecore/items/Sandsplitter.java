package com.beangamecore.items;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Sandsplitter extends BeangameItem implements BGRClickableI {
    
    private static final double ZONE_RADIUS = 6.0;
    private static final int CAST_DURATION = 200;
    private static final Color PARTICLE_COLOR = Color.fromRGB(225, 193, 110);

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player caster = event.getPlayer();
        UUID casterId = caster.getUniqueId();
        Location center = caster.getLocation();
        World world = caster.getWorld();

        if (onCooldown(casterId)) {
            sendCooldownMessage(caster);
            return false;
        }
        applyCooldown(casterId);

        Set<Player> hidden = new HashSet<>();
        DustOptions dustOptions = new DustOptions(PARTICLE_COLOR, 1.5F);
        double maxRadius = ZONE_RADIUS;
        double expansionRate = maxRadius / CAST_DURATION;

        // Use arrays for mutable variables
        int[] ticks = {0};
        double[] radius = {1.5};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (ticks[0] >= CAST_DURATION || !caster.isOnline()) {
                revealHiddenPlayers(hidden);
                hidden.clear();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            radius[0] = Math.min(maxRadius, radius[0] + expansionRate);
            world.spawnParticle(Particle.DUST, center, 15, radius[0], radius[0], radius[0], 0, dustOptions);

            Set<Player> currentlyInside = getPlayersInsideZone(center, world, radius[0]);

            handlePlayersInsideZone(currentlyInside, hidden, caster, ticks[0]);

            Iterator<Player> iterator = hidden.iterator();
            while (iterator.hasNext()) {
                Player target = iterator.next();
                if (!currentlyInside.contains(target)) {
                    for (Player viewer : Bukkit.getOnlinePlayers()) {
                        viewer.showPlayer(Main.getPlugin(), target);
                    }
                    iterator.remove();
                }
            }

            ticks[0]++;
        }, 0L, 1L).getTaskId();

        return true;
    }

    private void handlePlayersInsideZone(Set<Player> currentlyInside, Set<Player> hidden, Player caster, int ticks) {
        for (Player target : currentlyInside) {
            handleParticleEffect(target, ticks);
            handlePlayerHiding(hidden, target, caster);
        }
    }

    private void handleParticleEffect(Player target, int ticks) {
        if (ticks % 2 == 0) {
            DustOptions innerDust = new DustOptions(Color.fromRGB(252, 245, 95), 2);
            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 0.5, 0), 1, innerDust);
        }
    }

    private void handlePlayerHiding(Set<Player> hidden, Player target, Player caster) {
        if (hidden.add(target)) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!viewer.equals(target) && viewer.getGameMode() != GameMode.SPECTATOR) {
                    viewer.hidePlayer(Main.getPlugin(), target);
                }
            }
            caster.showPlayer(Main.getPlugin(), target); // caster still sees them
        }
    }

    private Set<Player> getPlayersInsideZone(Location center, World world, double radius) {
        Set<Player> currentlyInside = new HashSet<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getGameMode() == GameMode.SPECTATOR)
                continue;
            if (!target.getWorld().equals(world))
                continue;
            if (target.getLocation().distanceSquared(center) <= radius * radius) {
                currentlyInside.add(target);
            }
        }
        return currentlyInside;
    }

    private void revealHiddenPlayers(Set<Player> hidden) {
        for (Player target : hidden) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.showPlayer(Main.getPlugin(), target);
            }
        }
    }
            
    @Override
    public long getBaseCooldown() {
        return 24000L;
    }

    @Override
    public String getId() {
        return "sandsplitter";
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
        return "§6Sandsplitter";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to create an expanding",
            "§9sandstorm that lasts 10 seconds.",
            "§9Players inside become invisible to",
            "§9others but remain visible to you.",
            "§9Expands to 6 block radius.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.YELLOW_DYE;
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

