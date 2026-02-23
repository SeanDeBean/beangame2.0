package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class PaleOakBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        
        // Create thick 2x2 trunk base (similar to dark oak)
        for (int y = 0; y < Math.min(3, config.getTreeHeight()); y++) {
            placedBlocks.add(new BlockPosition(centerX, centerY + y, centerZ, config.getWoodType().log));
            placedBlocks.add(new BlockPosition(centerX + 1, centerY + y, centerZ, config.getWoodType().log));
            placedBlocks.add(new BlockPosition(centerX, centerY + y, centerZ + 1, config.getWoodType().log));
            placedBlocks.add(new BlockPosition(centerX + 1, centerY + y, centerZ + 1, config.getWoodType().log));
        }
        
        // Large horizontal branches
        int branchY = centerY + config.getTreeHeight() - 4;
        generateHorizontalBranch(centerX, branchY, centerZ, 1, 0, 4, 3, config, placedBlocks, random);
        generateHorizontalBranch(centerX, branchY, centerZ, -1, 0, 4, 3, config, placedBlocks, random);
        generateHorizontalBranch(centerX, branchY, centerZ, 0, 1, 4, 3, config, placedBlocks, random);
        generateHorizontalBranch(centerX, branchY, centerZ, 0, -1, 4, 3, config, placedBlocks, random);
        
        // More organic canopy with branching patterns
        for (int layerOffset = 0; layerOffset < 4; layerOffset++) {
            int layerY = centerY + config.getTreeHeight() - layerOffset - 1;
            int radius = 4 - layerOffset;
            
            for (int i = 0; i < 8 + (layerOffset * 2); i++) {
                double angle = (2 * Math.PI * i) / (8 + (layerOffset * 2));
                int branchLength = radius - random.nextInt(2);
                
                for (int l = 1; l <= branchLength; l++) {
                    int x = centerX + (int) (Math.cos(angle) * l);
                    int z = centerZ + (int) (Math.sin(angle) * l);
                    
                    if (random.nextDouble() < 0.7) {
                        placedBlocks.add(new BlockPosition(x, layerY, z, config.getWoodType().planks));
                        
                        if (random.nextDouble() < 0.3) {
                            placedBlocks.add(new BlockPosition(x, layerY + 1, z, config.getWoodType().planks));
                        }
                        if (random.nextDouble() < 0.2) {
                            placedBlocks.add(new BlockPosition(x, layerY - 1, z, config.getWoodType().planks));
                        }
                    }
                }
            }
        }
    }
    
    private void generateHorizontalBranch(int startX, int startY, int startZ, int dirX, int dirZ, 
                                         int length, int thickness, TreeConfig config, 
                                         List<BlockPosition> placedBlocks, Random random) {
        for (int i = 1; i <= length; i++) {
            int branchX = startX + (dirX * i);
            int branchZ = startZ + (dirZ * i);
            
            placedBlocks.add(new BlockPosition(branchX, startY, branchZ, config.getWoodType().log));
            
            if (i <= thickness) {
                if (dirX != 0) {
                    placedBlocks.add(new BlockPosition(branchX, startY, branchZ + 1, config.getWoodType().log));
                    if (i == 1) placedBlocks.add(new BlockPosition(branchX, startY, branchZ - 1, config.getWoodType().log));
                } else {
                    placedBlocks.add(new BlockPosition(branchX + 1, startY, branchZ, config.getWoodType().log));
                    if (i == 1) placedBlocks.add(new BlockPosition(branchX - 1, startY, branchZ, config.getWoodType().log));
                }
            }
            
            if (i > 2 && random.nextDouble() < 0.3) {
                startY++;
            }
        }
    }
}
