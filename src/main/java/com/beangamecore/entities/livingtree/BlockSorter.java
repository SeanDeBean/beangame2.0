package com.beangamecore.entities.livingtree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Location;

public class BlockSorter {
    
    /**
     * Sort blocks for growth animation (bottom to top, inside out)
     */
    public static List<BlockPosition> sortForGrowth(List<BlockPosition> blocks, Location baseLocation) {
        List<BlockPosition> sorted = new ArrayList<>(blocks);
        int centerX = baseLocation.getBlockX();
        int centerZ = baseLocation.getBlockZ();
        
        sorted.sort(Comparator
            .comparingInt((BlockPosition pos) -> pos.y) // Bottom to top
            .thenComparingDouble(pos -> {
                // Then by distance from center (inside out)
                double distA = Math.sqrt(Math.pow(pos.x - centerX, 2) + Math.pow(pos.z - centerZ, 2));
                return distA;
            })
        );
        
        return sorted;
    }
    
    /**
     * Sort blocks for decay animation (top to bottom, outside in)
     */
    public static List<BlockPosition> sortForDecay(List<BlockPosition> blocks, Location baseLocation) {
        List<BlockPosition> sorted = new ArrayList<>(blocks);
        int centerX = baseLocation.getBlockX();
        int centerZ = baseLocation.getBlockZ();
        
        sorted.sort(Comparator
            .comparingInt((BlockPosition pos) -> -pos.y) // Top to bottom (negative for descending)
            .thenComparingDouble(pos -> {
                // Then by distance from center (outside in)
                double distA = Math.sqrt(Math.pow(pos.x - centerX, 2) + Math.pow(pos.z - centerZ, 2));
                return -distA; // Negative for outside in
            })
        );
        
        return sorted;
    }
    
    /**
     * Sort blocks by material type (logs first, then planks, then fences, then leaves)
     */
    public static List<BlockPosition> sortByMaterialType(List<BlockPosition> blocks) {
        List<BlockPosition> sorted = new ArrayList<>(blocks);
        
        sorted.sort((a, b) -> {
            int priorityA = getMaterialPriority(a.material);
            int priorityB = getMaterialPriority(b.material);
            return Integer.compare(priorityA, priorityB);
        });
        
        return sorted;
    }
    
    private static int getMaterialPriority(org.bukkit.Material material) {
        // Logs have highest priority (appear first)
        if (material.name().contains("_LOG") || material.name().contains("_STEM")) {
            return 0;
        }
        // Planks next
        if (material.name().contains("_PLANKS")) {
            return 1;
        }
        // Fences and gates
        if (material.name().contains("_FENCE") || material.name().contains("_GATE")) {
            return 2;
        }
        // Leaves and special blocks last
        if (material.name().contains("_LEAVES") || 
            material == org.bukkit.Material.NETHER_WART_BLOCK || 
            material == org.bukkit.Material.WARPED_WART_BLOCK) {
            return 3;
        }
        return 4; // Unknown materials
    }
    
    /**
     * Sort blocks by Y coordinate only (simple vertical sorting)
     */
    public static List<BlockPosition> sortByHeight(List<BlockPosition> blocks, boolean ascending) {
        List<BlockPosition> sorted = new ArrayList<>(blocks);
        
        if (ascending) {
            sorted.sort(Comparator.comparingInt(pos -> pos.y));
        } else {
            sorted.sort((a, b) -> Integer.compare(b.y, a.y));
        }
        
        return sorted;
    }
}
