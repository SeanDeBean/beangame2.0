package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class CherryBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        int trunkTopY = centerY + config.getTreeHeight() - 1;
        
        // Thinner, more spaced out branching pattern
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 2, centerZ, 4, 3, 1, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 4, centerZ, 3, 2, 1, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 6, centerZ, 2, 1, 1, config, placedBlocks, random);
        
        // Create fewer drooping branches with thinner appearance
        for (int i = 0; i < 5; i++) {
            double angle = (2 * Math.PI * i) / 5;
            int branchX = centerX + (int) (Math.cos(angle) * 2);
            int branchZ = centerZ + (int) (Math.sin(angle) * 2);
            int branchY = centerY + config.getTreeHeight() - 3;
            
            // Create drooping branches with less density
            for (int drop = 0; drop < 2; drop++) {
                if (random.nextDouble() < 0.5) {
                    placedBlocks.add(new BlockPosition(branchX, branchY - drop, branchZ, config.getWoodType().fence));
                    
                    // Smaller, sparser leaf clusters at branch ends
                    if (drop == 1 && random.nextDouble() < 0.6) {
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                if (Math.abs(dx) + Math.abs(dz) <= 1 && random.nextDouble() < 0.6) {
                                    placedBlocks.add(new BlockPosition(branchX + dx, branchY - drop, branchZ + dz, config.getWoodType().planks));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
