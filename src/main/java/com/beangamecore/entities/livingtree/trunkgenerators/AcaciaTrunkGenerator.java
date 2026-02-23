package com.beangamecore.entities.livingtree.trunkgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;
import com.beangamecore.entities.livingtree.TreeGenerationContext;

public class AcaciaTrunkGenerator implements TrunkGenerator {
    @Override
    public void generateTrunk(TreeGenerationContext context) {
        TreeConfig config = context.getTreeConfig();
        List<BlockPosition> placedBlocks = context.getPlacedBlocks();
        Random random = context.getRandom();
        
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();

        // Generate angled trunk sections with potential for multiple bends
        int currentX = centerX;
        int currentZ = centerZ;
        int currentY = centerY;
        
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
    }
}

