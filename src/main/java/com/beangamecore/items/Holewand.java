package com.beangamecore.items;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.BlockCategories;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class Holewand extends BeangameItem implements BGProjectileI, BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack itemStack){
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // cooldown system
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.0F);
        launchProjectile(this, player, Snowball.class);
        return true;
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        event.getEntity().remove();
        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0F, 1.0F);

        // Round the location (this can be moved out of the loop if no need to update
        // every block)
        roundLocation(loc);

        // Use a Set for faster lookups (only need to do this once)
        Set<Material> functionalBlockSet = new HashSet<>(BlockCategories.functionalblock);

        processBlocksInRadius(loc, world, functionalBlockSet);
    }

    private void roundLocation(Location loc) {
        loc.setX(Math.round(loc.getX()));
        loc.setY(Math.round(loc.getY()));
        loc.setZ(Math.round(loc.getZ()));
    }

    private void processBlocksInRadius(Location loc, World world, Set<Material> functionalBlockSet) {
        // Iterate over the defined radius (x, y, z)
        for (int x = loc.getBlockX() - 1; x <= loc.getBlockX() + 1; x++) {
            for (int z = loc.getBlockZ() - 1; z <= loc.getBlockZ() + 1; z++) {
                for (int y = loc.getBlockY() - 33; y <= loc.getBlockY() + 1; y++) {
                    Block block = world.getBlockAt(x, y, z);
                    processBlock(block, loc, functionalBlockSet);
                }
            }
        }
    }

    private void processBlock(Block block, Location loc, Set<Material> functionalBlockSet) {
        Material blockType = block.getType();

        // Check if the block is a functional block
        if (!functionalBlockSet.contains(blockType)) {
            int y = block.getY();
            if (y <= loc.getBlockY() - 31) {
                setTemporaryLava(block);
            } else if (y == loc.getBlockY() - 30) {
                // Set cobweb for specific height
                block.setType(Material.COBWEB);
            } else {
                // Clear air for all other heights
                block.setType(Material.AIR);
            }
        }
    }

    private void setTemporaryLava(Block block) {
        // Replace with lava and schedule removal after a delay
        block.setType(Material.LAVA);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (block.getType() == Material.LAVA) {
                block.setType(Material.AIR);
            }
        }, 1200L);
    }

    @Override
    public long getBaseCooldown() {
        return 12000L;
    }

    @Override
    public String getId() {
        return "holewand";
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
        return "§6Hole Wand";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Shoots a projectile that creates",
            "§9a 3x3 hole. The bottom fills",
            "§9with temporary lava that clears",
            "§9after 60 seconds.",
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
        return Material.WOODEN_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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

