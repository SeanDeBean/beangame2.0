package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;
import com.beangamecore.entities.livingtree.WoodType;

public class LeafUtils {
    
    public static void generateLeavesFromCenter(CanopyCenter center, TreeConfig config, 
                                              List<BlockPosition> placedBlocks, int radius, 
                                              int height, double density, String shape, Random random) {
        Material leafMaterial = config.getWoodType().getLeafMaterial();
        World world = config.getBaseLocation().getWorld();
        int centerX = center.getX(), centerY = center.getY(), centerZ = center.getZ();
        
        for (int yOffset = 0; yOffset <= height; yOffset++) {
            double normalizedY = (double) yOffset / height;
            double verticalRadius = getVerticalRadius(radius, normalizedY, shape);
            int currentY = centerY + yOffset - (yOffset / 2);
            
            if (currentY < config.getMinLeafY()) continue;
            
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                    
                    if (shouldPlaceLeaf(distance, verticalRadius, normalizedY, shape) && random.nextDouble() < density) {
                        Block block = world.getBlockAt(x, currentY, z);
                        if (block.getType() == Material.AIR) {
                            placedBlocks.add(new BlockPosition(x, currentY, z, leafMaterial));
                        }
                    }
                }
            }
        }
    }
    
    public static void generatePointyConeLeaves(CanopyCenter center, TreeConfig config, 
                                               List<BlockPosition> placedBlocks, int baseRadius, 
                                               int height, Random random) {
        Material leafMaterial = config.getWoodType().getLeafMaterial();
        World world = config.getBaseLocation().getWorld();
        int centerX = center.getX(), centerY = center.getY(), centerZ = center.getZ();
        
        for (int yOffset = 0; yOffset < height; yOffset++) {
            double progress = (double) yOffset / height;
            int layerRadius = (int) Math.max(1, baseRadius * (1 - progress));
            int currentY = centerY + yOffset;
            
            if (currentY < config.getMinLeafY()) continue;
            
            generateLeafLayer(world, leafMaterial, centerX, currentY, centerZ, layerRadius, 0.7, true, placedBlocks, random);
            
            if (yOffset % 2 == 0 && random.nextDouble() < 0.4) {
                generateLeafCluster(world, leafMaterial, 
                    centerX + random.nextInt(3) - 1, 
                    currentY, 
                    centerZ + random.nextInt(3) - 1, 
                    1, 0.6, placedBlocks, random);
            }
        }
    }
    
    public static void generateLeafLayer(World world, Material material, int centerX, int centerY, int centerZ, 
                                       int radius, double edgeDensity, boolean includeCenter, 
                                       List<BlockPosition> placedBlocks, Random random) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                boolean isEdge = distance <= radius && distance >= radius - 1;
                boolean isCenter = distance <= 1;
                
                if ((isEdge && random.nextDouble() < edgeDensity) || 
                    (includeCenter && isCenter && random.nextDouble() < 0.3)) {
                    Block block = world.getBlockAt(x, centerY, z);
                    if (block.getType() == Material.AIR) {
                        placedBlocks.add(new BlockPosition(x, centerY, z, material));
                    }
                }
            }
        }
    }
    
    public static void generateLeafCluster(World world, Material material, int centerX, int centerY, int centerZ, 
                                         int size, double density, List<BlockPosition> placedBlocks, Random random) {
        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= size && random.nextDouble() < density) {
                    Block block = world.getBlockAt(centerX + dx, centerY, centerZ + dz);
                    if (block.getType() == Material.AIR) {
                        placedBlocks.add(new BlockPosition(centerX + dx, centerY, centerZ + dz, material));
                    }
                }
            }
        }
    }
    
    public static void generateBranchEndClusters(CanopyCenter center, TreeConfig config, 
                                                List<BlockPosition> placedBlocks, int radius, 
                                                int height, double density, String shape, 
                                                double chance, Random random) {
        List<BlockPosition> branchEnds = getBranchEnds(placedBlocks, config.getWoodType());
        for (BlockPosition branch : branchEnds) {
            if (random.nextDouble() < chance) {
                generateLeavesFromCenter(new CanopyCenter(branch.x, branch.y, branch.z), config, 
                                       placedBlocks, radius, height, density, shape, random);
            }
        }
    }
    
    public static void generateLayeredLeaves(CanopyCenter center, TreeConfig config, 
                                            List<BlockPosition> placedBlocks, int radius, 
                                            int layers, double density, Random random) {
        Material leafMaterial = config.getWoodType().getLeafMaterial();
        World world = config.getBaseLocation().getWorld();
        int centerX = center.getX(), centerY = center.getY(), centerZ = center.getZ();
        
        for (int layer = 0; layer < layers; layer++) {
            int layerY = centerY - layer;
            if (layerY < config.getMinLeafY()) continue;
            
            int layerRadius = radius - layer;
            generateLeafLayer(world, leafMaterial, centerX, layerY, centerZ, layerRadius, density, false, placedBlocks, random);
        }
    }
    
    public static void generateVerticalRangeLeaves(CanopyCenter center, TreeConfig config, 
                                                  List<BlockPosition> placedBlocks, int radius, 
                                                  int verticalRange, double density, Random random) {
        Material leafMaterial = config.getWoodType().getLeafMaterial();
        World world = config.getBaseLocation().getWorld();
        int centerX = center.getX(), centerY = center.getY(), centerZ = center.getZ();
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int yOffset = -verticalRange; yOffset <= verticalRange; yOffset++) {
                    int y = centerY + yOffset;
                    if (y < config.getMinLeafY()) continue;
                    
                    double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                    double heightFactor = 1 - Math.abs(yOffset) / (double) (verticalRange + 1);
                    
                    if (distance <= radius * heightFactor && random.nextDouble() < density) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.AIR) {
                            placedBlocks.add(new BlockPosition(x, y, z, leafMaterial));
                        }
                    }
                }
            }
        }
    }
    
    private static List<BlockPosition> getBranchEnds(List<BlockPosition> placedBlocks, WoodType woodType) {
        List<BlockPosition> branchEnds = new ArrayList<>();
        Set<String> positions = new HashSet<>();
        
        for (BlockPosition pos : placedBlocks) {
            if (isBranchMaterial(pos.material, woodType)) {
                positions.add(pos.x + "," + pos.y + "," + pos.z);
            }
        }
        
        for (BlockPosition pos : placedBlocks) {
            if (isBranchMaterial(pos.material, woodType) && countAdjacentBranchBlocks(pos, positions) <= 2) {
                branchEnds.add(pos);
            }
        }
        
        return branchEnds;
    }
    
    private static boolean isBranchMaterial(Material material, WoodType woodType) {
        return material == woodType.log || material == woodType.planks || 
               material == woodType.fence || material == woodType.fenceGate;
    }
    
    private static int countAdjacentBranchBlocks(BlockPosition pos, Set<String> positions) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    if (positions.contains((pos.x + dx) + "," + (pos.y + dy) + "," + (pos.z + dz))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    private static double getVerticalRadius(int radius, double normalizedY, String shape) {
        switch (shape) {
            case "sphere": return radius * (1 - normalizedY * 0.7);
            case "cone": return radius * (1 - normalizedY);
            case "cylinder": return radius;
            case "umbrella": return radius * (0.3 + normalizedY * 0.7);
            case "dome": return radius * Math.cos(normalizedY * Math.PI / 2);
            default: return radius;
        }
    }
    
    private static boolean shouldPlaceLeaf(double distance, double verticalRadius, double normalizedY, String shape) {
        switch (shape) {
            case "cylinder": return distance <= verticalRadius && normalizedY <= 0.8;
            case "umbrella": return distance <= verticalRadius && normalizedY > 0.3;
            default: return distance <= verticalRadius;
        }
    }
}
