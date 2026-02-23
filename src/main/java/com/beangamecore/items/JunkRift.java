package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.entities.rift.JunkRiftEntity;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class JunkRift extends BeangameItem implements BGRClickableI {
    private static final int RIFT_DURATION = 100;
    private static final int MAX_RANGE = 40;
    private static final double BLADE_DAMAGE = 7.5;

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();

        shootRealityShard(player, startLoc, direction);
        return true;
    }

    private void shootRealityShard(Player player, Location origin, Vector direction) {
        World world = player.getWorld();
        ArmorStand projectile = world.spawn(origin, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setAI(false);
            stand.setSmall(true);
            stand.setInvulnerable(true);
        });

        Bukkit.getOnlinePlayers().forEach(p -> p.hideEntity(Main.getPlugin(), projectile));

        // Use arrays for mutable variables
        int[] ticks = {0};
        boolean[] hit = {false};
        Vector[] directionUsed = {direction.clone()}; // Clone to avoid modifying original
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if(hit[0] || ticks[0] > MAX_RANGE || !projectile.isValid()) {
                if (!hit[0]) {
                    createRift(projectile.getLocation(), player, directionUsed[0]);
                }
                projectile.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            directionUsed[0] = directionUsed[0].add(new Vector(0, -0.02, 0)).normalize();

            Location newLoc = projectile.getLocation().add(directionUsed[0].clone().multiply(1.2));

            projectile.teleport(newLoc);

            if (newLoc.getBlock().getType().isSolid()) {
                createRift(newLoc, player, directionUsed[0]);
                hit[0] = true;
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            for (Entity entity : projectile.getNearbyEntities(0.5, 0.5, 0.5)) {
                if (entity instanceof LivingEntity target && target != player) {
                    createRift(projectile.getLocation(), player, directionUsed[0]);
                    hit[0] = true;
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
            }

            world.spawnParticle(Particle.REVERSE_PORTAL, newLoc, 2, 0.1, 0.1, 0.1, 0.02);
            world.spawnParticle(Particle.END_ROD, newLoc, 1, 0.1, 0.1, 0.1, 0.01);

            ticks[0]++;
        }, 0L, 1L).getTaskId();
    }

    private void createRift(Location impactLoc, Player player, Vector direction) {
        Random random = new Random();

        double heightOffset = 5 + random.nextDouble() * 2;
        double horizontalOffsetX = (random.nextDouble() - 0.5) * 0.6;
        double horizontalOffsetZ = (random.nextDouble() - 0.5) * 0.6;

        Location riftLoc = impactLoc.clone().add(horizontalOffsetX, heightOffset, horizontalOffsetZ);

        double riftLength = 9 + random.nextDouble() * 3;

        JunkRiftEntity rift = new JunkRiftEntity(Main.getPlugin(), player, riftLoc, riftLength, RIFT_DURATION, BLADE_DAMAGE, direction);

        rift.start();

        player.getWorld().playSound(riftLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.7f);
    }

    @Override
    public long getBaseCooldown() {
        return 16000;
    }

    @Override
    public String getId() {
        return "junkrift";
    }

    @Override
    public String getName() {
        return "§5Junk Rift";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Shoots a projectile that opens a rift",
            "§9above the impact location. The rift",
            "§9drops beangame items that apply their",
            "§9on-hit effects when they hit players.",
            "",
            "§dOn Hit Extender",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
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

}

