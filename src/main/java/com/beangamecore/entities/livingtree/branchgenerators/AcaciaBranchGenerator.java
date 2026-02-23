package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class AcaciaBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        
        int currentX = centerX;
        int currentZ = centerZ;
        int currentY = centerY;
        
        // Create the characteristic bent trunk with potential for multiple flat sections
        int flatSections = 1 + random.nextInt(2);
        
        for (int y = 0; y < config.getTreeHeight(); y++) {
            placedBlocks.add(new BlockPosition(currentX, currentY + y, currentZ, config.getWoodType().log));
            
            // Add bends at different heights to create multiple flat sections
            if (y > config.getTreeHeight()/3 && y < config.getTreeHeight() - 3 && random.nextDouble() < 0.15) {
                currentX += random.nextBoolean() ? 1 : -1;
                currentZ += random.nextBoolean() ? 1 : -1;
                currentX = Math.max(centerX - 3, Math.min(centerX + 3, currentX));
                currentZ = Math.max(centerZ - 3, Math.min(centerZ + 3, currentZ));
            }
        }
        
        // Generate flat canopy sections at different heights
        for (int section = 0; section < flatSections; section++) {
            int sectionY = centerY + config.getTreeHeight() - 2 - (section * 2);
            int sectionSize = 3 - section;
            
            // Thinner, more sparse flat canopy
            for (int x = currentX - sectionSize; x <= currentX + sectionSize; x++) {
                for (int z = currentZ - sectionSize; z <= currentZ + sectionSize; z++) {
                    double distance = Math.sqrt(Math.pow(x - currentX, 2) + Math.pow(z - currentZ, 2));
                    if (distance <= sectionSize + 0.5 && random.nextDouble() < 0.6) {
                        placedBlocks.add(new BlockPosition(x, sectionY, z, config.getWoodType().fence));
                    }
                }
            }
        }
    }
}
