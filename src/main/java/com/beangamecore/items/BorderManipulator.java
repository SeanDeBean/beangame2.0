package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class BorderManipulator extends BeangameItem implements BGRClickableI {
    
    public void setActive(boolean active){
        BorderManipulatorActive = active;
        target = null;
    }

    public static boolean BorderManipulatorActive = true;

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        if(onCooldown(event.getPlayer().getUniqueId())){
            sendCooldownMessage(event.getPlayer());
            return false;
        }
        Location l = event.getPlayer().getWorld().getWorldBorder().getCenter();
        setSpeed(0.03);
        setTarget(borderManipulatorCenter(l, event.getPlayer()));
        // new Location(event.getPlayer().getWorld(), l.getX() + random.nextInt(-100, 101), l.getY(), l.getZ() + random.nextInt(-100, 101))
        applyCooldown(event.getPlayer().getUniqueId());
        return true;
    }

    private Location borderManipulatorCenter(Location center, Player player) {
        World world = center.getWorld();
        Vector facing = player.getLocation().getDirection().normalize();

        for (int i = 0; i < 5; i++) {
            double randomX = random.nextDouble() * 100 - 50;
            double randomZ = random.nextDouble() * 100 - 50;

            double biasX = facing.getX() * 50;
            double biasZ = facing.getZ() * 50;

            double finalX = center.getX() + randomX + biasX;
            double finalZ = center.getZ() + randomZ + biasZ;

            // Check if target would be outside world border limits
            if (Math.abs(finalX) > 29999984 || Math.abs(finalZ) > 29999984) {
                // Return current center instead of invalid target
                return new Location(world, center.getX(), center.getY(), center.getZ());
            }

            Location loc = new Location(world, finalX, center.getY(), finalZ);
            if (!world.getHighestBlockAt(loc).getType().equals(Material.WATER)) {
                return loc;
            }
        }

        // Fallback if all 5 were water
        return new Location(world, center.getX(), center.getY(), center.getZ());
    }

    public static Location target;
    double speed = 1;
    Random random = new Random();
    
    public void tick(World w) {
        if (!isWorldAndChunkLoaded(w)) {
            return;
        }

        if (target != null) {
            if (!isTargetWorldValid(w)) {
                return;
            }

            WorldBorder border = w.getWorldBorder();
            Location current = border.getCenter();

            if (!isCurrentWorldValid(current)) {
                return;
            }

            moveBorderTowardsTarget(border, current);
            w.setSpawnLocation(current);

            if (BorderManipulatorActive) {
                shrinkBorder(border);
            }

            if (isBorderCloseToTarget(current)) {
                setTarget(null);
            }
        }
    }

    private boolean isWorldAndChunkLoaded(World w) {
        return w != null
                && w.isChunkLoaded((int) w.getSpawnLocation().getX() >> 4, (int) w.getSpawnLocation().getZ() >> 4);
    }

    private boolean isTargetWorldValid(World w) {
        return target.getWorld() != null && target.getWorld().equals(w);
    }

    private boolean isCurrentWorldValid(Location current) {
        return current.getWorld() != null;
    }

    private void moveBorderTowardsTarget(WorldBorder border, Location current) {
        Vector dir = border.getCenter().subtract(target).toVector().normalize().multiply(speed * -1);
        current.add(dir);
        
        // Check if new center is outside world border limits
        if (Math.abs(current.getX()) > 29999984 || Math.abs(current.getZ()) > 29999984) {
            // Stop the effect by clearing the target
            setTarget(null);
            return;
        }
        
        border.setCenter(current);
    }

    private void shrinkBorder(WorldBorder border) {
        // Only shrink if not currently resizing
        border.setSize(border.getSize() - 0.0001, 1);
    }

    private boolean isBorderCloseToTarget(Location current) {
        return current.distance(target) < speed + 1;
    }

    public double getSpeed(){
        return speed;
    }

    public void setSpeed(double s){
        speed = s;
    }

    public Location getTarget(){
        return target;
    }

    public void setTarget(Location t){
        target = t;
    }

    @Override
    public long getBaseCooldown() {
        return 35000L;
    }

    @Override
    public String getId() {
        return "bordermanipulator";
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
        return ChatColor.AQUA + "Border Manipulator";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to move the world border",
            "§9center in your facing direction with",
            "§9random offset. The border slowly shrinks",
            "§9over time and moves toward the target.",
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
        return Material.BREEZE_ROD;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

