package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.BlockCategories;
import org.bukkit.*;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Drit extends BeangameItem implements BGProjectileI, BGRClickableI {
    
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
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1.0F, 1.0F);
        launchProjectile(this, player, Egg.class);
        return true;
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        event.getEntity().remove();

        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();
        int radius = 2;

        // Round the location coordinates once
        int xStart = Math.round(loc.getBlockX()) - radius;
        int yStart = Math.round(loc.getBlockY()) - radius;
        int zStart = Math.round(loc.getBlockZ()) - radius;

        // Play sound
        world.playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0F, 1.0F);

        // Iterate over the area around the projectile hit location
        transformBlocksInRadius(world, xStart, yStart, zStart, radius);
    }

    private void transformBlocksInRadius(World world, int xStart, int yStart, int zStart, int radius) {
        for (int x = xStart; x <= xStart + 2 * radius; x++) {
            for (int y = yStart; y <= yStart + 2 * radius; y++) {
                for (int z = zStart; z <= zStart + 2 * radius; z++) {
                    processBlock(world, x, y, z);
                }
            }
        }
    }

    private void processBlock(World world, int x, int y, int z) {
        // Get the block at the current location
        Material blockType = world.getBlockAt(x, y, z).getType();

        // Check if the block is functional (using Set for constant-time lookup)
        if (BlockCategories.functionalblock.contains(blockType)) {
            return; // Skip functional blocks
        }

        // Change the block to dirt or coarse dirt
        Material newMaterial = (Math.random() >= 0.4) ? Material.DIRT : Material.COARSE_DIRT;
        world.getBlockAt(x, y, z).setType(newMaterial);
    }

    @Override
    public long getBaseCooldown() {
        return 4500L;
    }

    @Override
    public String getId() {
        return "drit";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "DCD", "CBC", "DCD", r.mCFromMaterial(Material.DIRT), r.mCFromMaterial(Material.COARSE_DIRT), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§f§odrit";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to launch an egg projectile",
            "§9that transforms all non-functional blocks",
            "§9in a 5x5x5 area into dirt or coarse dirt",
            "§9on impact.",
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
        return Material.DIRT;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

