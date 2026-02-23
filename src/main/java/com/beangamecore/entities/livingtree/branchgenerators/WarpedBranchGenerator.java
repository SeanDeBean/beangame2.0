package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class WarpedBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        
        // Similar to crimson but more upward-focused
        int currentX = centerX;
        int currentZ = centerZ;
        
        for (int y = 0; y < config.getTreeHeight(); y++) {
            placedBlocks.add(new BlockPosition(currentX, centerY + y, currentZ, config.getWoodType().log));
            
            // Less twisting than crimson, more upward growth
            if (y > 2 && random.nextDouble() < 0.25) {
                currentX += random.nextInt(3) - 1;
                currentZ += random.nextInt(3) - 1;
                currentX = Math.max(centerX - 2, Math.min(centerX + 2, currentX));
                currentZ = Math.max(centerZ - 2, Math.min(centerZ + 2, currentZ));
            }
        }
        
        // Generate upward-reaching tendrils
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            int tendrilX = centerX + (int) (Math.cos(angle) * 2);
            int tendrilZ = centerZ + (int) (Math.sin(angle) * 2);
            int tendrilHeight = 3 + random.nextInt(3);
            
            for (int y = 0; y < tendrilHeight; y++) {
                placedBlocks.add(new BlockPosition(tendrilX, centerY + config.getTreeHeight() + y, tendrilZ, config.getWoodType().fence));
                
                // Slight outward curve
                if (y > 1) {
                    tendrilX += Math.cos(angle) * 0.3 > 0 ? 1 : (Math.cos(angle) * 0.3 < 0 ? -1 : 0);
                    tendrilZ += Math.sin(angle) * 0.3 > 0 ? 1 : (Math.sin(angle) * 0.3 < 0 ? -1 : 0);
                }
            }
        }
        
        // Warped fungal cap - more organized than crimson
        int capY = centerY + config.getTreeHeight() - 1;
        for (int layer = 0; layer < 3; layer++) {
            int layerY = capY + layer;
            int radius = 3 - layer;
            
            for (int x = currentX - radius; x <= currentX + radius; x++) {
                for (int z = currentZ - radius; z <= currentZ + radius; z++) {
                    double distance = Math.sqrt(Math.pow(x - currentX, 2) + Math.pow(z - currentZ, 2));
                    if (distance <= radius && random.nextDouble() < (0.95 - layer * 0.2)) {
                        placedBlocks.add(new BlockPosition(x, layerY, z, config.getWoodType().planks));
                    }
                }
            }
        }
    }
}

