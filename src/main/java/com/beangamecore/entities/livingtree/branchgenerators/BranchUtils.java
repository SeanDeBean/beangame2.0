package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;
import com.beangamecore.entities.livingtree.WoodType;

public class BranchUtils {
    public static void generateRadialBranches(int centerX, int centerY, int centerZ, int numBranches, 
                                             int length, int thickness, TreeConfig config, 
                                             List<BlockPosition> placedBlocks, Random random) {
        double angleStep = 2 * Math.PI / numBranches;
        
        for (int i = 0; i < numBranches; i++) {
            double angle = i * angleStep + (random.nextDouble() * 0.4 - 0.2);
            generateBranch(centerX, centerY, centerZ, angle, length, thickness, 0.4, config, placedBlocks, random);
        }
    }
    
    private static void generateBranch(int startX, int startY, int startZ, double angle, double length, 
                                      int thickness, double upwardFactor, TreeConfig config, 
                                      List<BlockPosition> placedBlocks, Random random) {
        if (thickness <= 0) return;

        double xDir = Math.cos(angle);
        double zDir = Math.sin(angle);
        
        for (double t = 0; t < length; t += 0.5) {
            int x = startX + (int) Math.round(t * xDir);
            int z = startZ + (int) Math.round(t * zDir);
            int y = startY + (int) (t * upwardFactor);

            Material material = getMaterialForThickness(thickness, config.getWoodType(), random);
            placedBlocks.add(new BlockPosition(x, y, z, material));

            // Sub-branches with decreasing thickness
            if (random.nextDouble() < 0.4 && thickness > 1 && t > length * 0.3) {
                double subAngle = angle + (random.nextDouble() - 0.5) * Math.PI / 1.5;
                generateBranch(x, y, z, subAngle, length * 0.8, thickness - 1, upwardFactor * 0.8, config, placedBlocks, random);
            }
        }
    }
    
    private static Material getMaterialForThickness(int thickness, WoodType woodType, Random random) {
        switch (thickness) {
            case 3: return woodType.log;
            case 2: return woodType.planks;
            case 1: return random.nextBoolean() ? woodType.fence : woodType.fenceGate;
            default: return woodType.fence;
        }
    }
}
