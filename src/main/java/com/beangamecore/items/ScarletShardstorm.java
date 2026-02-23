package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class ScarletShardstorm extends BeangameItem implements BGLClickableI {
    
    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(player.getGameMode().equals(GameMode.SPECTATOR)){
            return;
        }

        if (onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);

        World world = player.getWorld();
        Location eyeLoc = player.getEyeLocation();

        // item effect
        boolean nearby = world.getNearbyEntities(player.getLocation(), 5, 5, 5).stream()
            .anyMatch(e -> e instanceof Player && !e.getUniqueId().equals(uuid) && !((Player)e).getGameMode().equals(GameMode.SPECTATOR));
        
        int shrapnelCount = nearby ? 12 : 6;
        double radius = 1.5;
        double range = nearby ? 4 : 12;

        List<Vector> offsets = new ArrayList<>();

        Vector forward = eyeLoc.getDirection().normalize(); // Player's view direction
        Vector base = getAnyPerpendicular(forward).normalize().multiply(radius); // A vector 90° to forward

        for (int i = 0; i < shrapnelCount; i++) {
            double angle = 2 * Math.PI * i / shrapnelCount;
            Vector rotated = rotateAroundAxis(base, forward, angle);
            offsets.add(rotated);
        }

        Plugin plugin = Main.getPlugin();

        // Use arrays for mutable variables
        int[] index = {0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if(index[0] >= offsets.size()){
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Vector offset = offsets.get(index[0]);
            new ScarletShardstormShrapnel(player, offset, range, 1, plugin);
            index[0]++;
        }, shrapnelCount, shrapnelCount).getTaskId();

    }

    private Vector getAnyPerpendicular(Vector v) {
        if (Math.abs(v.getX()) < 0.99) {
            return new Vector(1, 0, 0).crossProduct(v);
        } else {
            return new Vector(0, 1, 0).crossProduct(v);
        }
    }

    private Vector rotateAroundAxis(Vector vec, Vector axis, double angle) {
        Vector k = axis.clone().normalize();
        Vector v = vec.clone();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return v.multiply(cos)
                .add(k.clone().crossProduct(v).multiply(sin))
                .add(k.clone().multiply(k.dot(v) * (1 - cos)));
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 1800L;
    }

    @Override
    public String getId() {
        return "scarletshardstorm";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public String getName() {
        return "§cScarlet Shardstorm";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§dLeft-click to fire shrapnel",
            "§dforward from you. Each shard",
            "§dapplies on hit effects.",
            "",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 106;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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
