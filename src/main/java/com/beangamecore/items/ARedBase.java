package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import com.beangamecore.util.BlockCategories;

public class ARedBase extends BeangameItem implements BGRClickableI {
    @Override
    public long getBaseCooldown() {
        return 12000;
    }

    @Override
    public String getId() {
        return "aredbase";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "LLL", "LBL", "LLL", r.eCFromBeangame(Key.bg("launchpad")), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§cA Red Base";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to instantly construct",
            "§9a fortified red wool base around",
            "§9yourself with defensive walls and",
            "§9towers for protection.",
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
        return Material.RED_CANDLE;
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

    public List<Block> getBlocksBetween(Location loc1, Location loc2) {
        List<Block> blocks = new ArrayList<>();
    
        int x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        
        int x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
    
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    Location loc = new Location(loc1.getWorld(), x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }
    
        return blocks;
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        World world = player.getWorld();
        Location center = player.getLocation();
        ArrayList<Block> redwool = new ArrayList<>();
        ArrayList<Block> ladder = new ArrayList<>();

        fillRedwoolAndLadderBlocks(center, world, redwool, ladder);

        int y = center.getBlockY();

        // building
        buildStructure(world, center, y, redwool, ladder);
        return true;
    }

    private void buildStructure(World world, Location center, int y, ArrayList<Block> redwool,
            ArrayList<Block> ladder) {
        AtomicInteger j = new AtomicInteger(y);
        for (int i = y; i < y + 7; i++) {
            playBuildingSound(world, center);
            ArrayList<Block> toRemove = new ArrayList<>();
            ArrayList<Block> toRemoveLadder = new ArrayList<>();
            scheduleDelayedTask(world, j, i, y, redwool, ladder, toRemove, toRemoveLadder);
            removeBlocks(redwool, toRemove);
            removeBlocks(ladder, toRemoveLadder);
        }
    }

    private void playBuildingSound(World world, Location center) {
        world.playSound(center, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1, 1);
    }

    private void scheduleDelayedTask(World world, AtomicInteger j, int i, int y, ArrayList<Block> redwool,
            ArrayList<Block> ladder, ArrayList<Block> toRemove, ArrayList<Block> toRemoveLadder) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                processBlocks(redwool, j, toRemove, Material.RED_WOOL);
                processBlocks(ladder, j, toRemoveLadder, Material.LADDER);
                j.set(j.get() + 1);
            }
        }, 2 * (i - y));
    }

    private void processBlocks(ArrayList<Block> blocks, AtomicInteger j, ArrayList<Block> toRemove, Material material) {
        for (Block block : blocks) {
            if (block.getLocation().getBlockY() == j.get()) {
                toRemove.add(block);
                placeBlockAt(block, material);
            }
        }
    }

    private void removeBlocks(ArrayList<Block> blocks, ArrayList<Block> toRemove) {
        for (Block remove : toRemove) {
            blocks.remove(remove);
        }
    }

    private void fillRedwoolAndLadderBlocks(Location center, World world, ArrayList<Block> redwool,
            ArrayList<Block> ladder) {
        // wall east
        Location loc1 = center.clone().add(2, 0, 1);
        Location loc2 = center.clone().add(2, 4, -1);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // wall south
        loc1 = center.clone().add(1, 0, 2);
        loc2 = center.clone().add(-1, 4, 2);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // wall west
        loc1 = center.clone().add(-2, 0, 1);
        loc2 = center.clone().add(-2, 4, -1);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // wall north
        loc1 = center.clone().add(-1, 0, -2);
        loc2 = center.clone().add(1, 4, -2);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // roof
        loc1 = center.clone().add(1, 4, 1);
        loc2 = center.clone().add(-1, 4, -1);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        Location adjustedCenter = center.clone().add(0, 5, 0);

        // NE tall
        loc1 = adjustedCenter.clone().add(2, -1, -2);
        loc2 = adjustedCenter.clone().add(2, 1, -2);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // SE tall
        loc1 = adjustedCenter.clone().add(2, -1, 2);
        loc2 = adjustedCenter.clone().add(2, 1, 2);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // SW tall
        loc1 = adjustedCenter.clone().add(-2, -1, 2);
        loc2 = adjustedCenter.clone().add(-2, 1, 2);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // SE tall
        loc1 = adjustedCenter.clone().add(-2, -1, -2);
        loc2 = adjustedCenter.clone().add(-2, 1, -2);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // east +
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(3, 0, -1)));
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(3, 0, 1)));
        loc1 = adjustedCenter.clone().add(3, 1, 0);
        loc2 = adjustedCenter.clone().add(3, -1, 0);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // south +
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(1, 0, 3)));
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(-1, 0, 3)));
        loc1 = adjustedCenter.clone().add(0, 1, 3);
        loc2 = adjustedCenter.clone().add(0, -1, 3);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // west +
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(-3, 0, 1)));
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(-3, 0, -1)));
        loc1 = adjustedCenter.clone().add(-3, 1, 0);
        loc2 = adjustedCenter.clone().add(-3, -1, 0);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // north +
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(1, 0, -3)));
        redwool.add(world.getBlockAt(adjustedCenter.clone().add(-1, 0, -3)));
        loc1 = adjustedCenter.clone().add(0, 1, -3);
        loc2 = adjustedCenter.clone().add(0, -1, -3);
        redwool.addAll(getBlocksBetween(loc1, loc2));

        // ladder
        loc1 = center.clone().add(0, 0, 1);
        loc2 = center.clone().add(0, 4, 1);
        ladder.addAll(getBlocksBetween(loc1, loc2));
    }

    private static void placeBlockAt(Block block, Material material){
        Material type = block.getType();
        for (Material functionalblocks : BlockCategories.functionalblock){
            if(type == functionalblocks){
                return;
            }
        }
        block.setType(material);
    }
}

