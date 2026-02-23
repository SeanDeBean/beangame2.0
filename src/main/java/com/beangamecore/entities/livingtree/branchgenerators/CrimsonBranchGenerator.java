package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class CrimsonBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        
        // Create irregular, twisted stem
        int currentX = centerX;
        int currentZ = centerZ;
        
        for (int y = 0; y < config.getTreeHeight(); y++) {
            placedBlocks.add(new BlockPosition(currentX, centerY + y, currentZ, config.getWoodType().log));
            
            // More aggressive twisting than mangrove
            if (y > 1 && random.nextDouble() < 0.4) {
                currentX += random.nextInt(3) - 1;
                currentZ += random.nextInt(3) - 1;
                currentX = Math.max(centerX - 3, Math.min(centerX + 3, currentX));
                currentZ = Math.max(centerZ - 3, Math.min(centerZ + 3, currentZ));
            }
        }
        
        // Generate weeping vines equivalent
        for (int i = 0; i < 12; i++) {
            int vineX = centerX + random.nextInt(7) - 3;
            int vineZ = centerZ + random.nextInt(7) - 3;
            int vineLength = 2 + random.nextInt(4);
            
            for (int y = centerY + config.getTreeHeight() - 1; y >= centerY + config.getTreeHeight() - vineLength; y--) {
                if (random.nextDouble() < 0.8) {
                    placedBlocks.add(new BlockPosition(vineX, y, vineZ, config.getWoodType().fenceGate));
                }
            }
        }
        
        // Irregular fungal cap
        int capY = centerY + config.getTreeHeight() - 1;
        for (int x = currentX - 4; x <= currentX + 4; x++) {
            for (int z = currentZ - 4; z <= currentZ + 4; z++) {
                double distance = Math.sqrt(Math.pow(x - currentX, 2) + Math.pow(z - currentZ, 2));
                // Irregular, patchy distribution
                if (distance <= 4 && random.nextDouble() < (0.9 - distance * 0.15)) {
                    placedBlocks.add(new BlockPosition(x, capY, z, config.getWoodType().planks));
                    
                    // Some blocks extend up
                    if (random.nextDouble() < 0.3) {
                        placedBlocks.add(new BlockPosition(x, capY + 1, z, config.getWoodType().planks));
                    }
                }
            }
        }
    }
}
