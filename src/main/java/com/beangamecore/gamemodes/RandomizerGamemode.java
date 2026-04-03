package com.beangamecore.gamemodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;

public class RandomizerGamemode {

    private static List<ItemStack> allItems = new ArrayList<>();
    private static Random random = new Random();

    public static void randomDrops(BlockBreakEvent event){
        if (allItems.isEmpty()) {
            initializeItemList();
        }
        event.setDropItems(false);
        Block block = event.getBlock();

        ItemStack randomItem = allItems.get(random.nextInt(allItems.size()));
        Item item = block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), randomItem);
        item.setTicksLived(5800);
    }

    private static void initializeItemList() {
        allItems.clear(); // Clear existing items

        for (Material material : Material.values()) {
            // Filter out non-items (like AIR, VOID_AIR, etc.) and legacy materials
            if (isSuitableMaterial(material)) {
                allItems.add(new ItemStack(material, 1));
            }
        }

        for (BeangameItem bgitem : BeangameItemRegistry.getItemsInRotation()) {
            allItems.add(bgitem.asItem());
        }

        // Optional: Log how many items were loaded
        Bukkit.getLogger().info("Loaded " + allItems.size() + " items for randomizer!");
    }

    @SuppressWarnings("deprecation")
    private static boolean isSuitableMaterial(Material material) {
        return material.isItem() && !material.isAir() && !material.isLegacy();
    }
}

