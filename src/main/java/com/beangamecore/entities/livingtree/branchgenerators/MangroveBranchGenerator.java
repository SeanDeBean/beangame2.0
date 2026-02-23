package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class MangroveBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        
        // Create main twisted trunk
        int currentX = centerX;
        int currentZ = centerZ;
        
        for (int y = 0; y < config.getTreeHeight(); y++) {
            placedBlocks.add(new BlockPosition(currentX, centerY + y, currentZ, config.getWoodType().log));
            
            // Add twisting effect
            if (y > 2 && y < config.getTreeHeight() - 2 && random.nextDouble() < 0.3) {
                currentX += random.nextInt(3) - 1;
                currentZ += random.nextInt(3) - 1;
                currentX = Math.max(centerX - 2, Math.min(centerX + 2, currentX));
                currentZ = Math.max(centerZ - 2, Math.min(centerZ + 2, currentZ));
            }
        }
        
        // Generate aerial prop roots
        for (int i = 0; i < 6; i++) {
            double angle = (2 * Math.PI * i) / 6;
            int rootX = centerX + (int) (Math.cos(angle) * 3);
            int rootZ = centerZ + (int) (Math.sin(angle) * 3);
            int rootLength = 3 + random.nextInt(3);
            
            // Create downward hanging roots
            for (int y = centerY + config.getTreeHeight() - 3; y >= centerY + config.getTreeHeight() - rootLength; y--) {
                if (random.nextDouble() < 0.7) {
                    placedBlocks.add(new BlockPosition(rootX, y, rootZ, config.getWoodType().fence));
                }
            }
            
            // Create upward angled roots
            for (int j = 0; j < rootLength - 1; j++) {
                int rootY = centerY + j;
                int adjustedX = centerX + (int) (Math.cos(angle) * (3 - j * 0.4));
                int adjustedZ = centerZ + (int) (Math.sin(angle) * (3 - j * 0.4));
                placedBlocks.add(new BlockPosition(adjustedX, rootY, adjustedZ, config.getWoodType().fence));
            }
        }
        
        // Wide spreading canopy
        BranchUtils.generateRadialBranches(centerX, centerY + config.getTreeHeight() - 2, centerZ, 8, 5, 2, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, centerY + config.getTreeHeight() - 4, centerZ, 6, 4, 1, config, placedBlocks, random);
    }
}
