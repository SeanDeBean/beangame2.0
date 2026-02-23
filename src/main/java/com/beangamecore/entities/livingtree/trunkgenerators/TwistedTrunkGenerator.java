package com.beangamecore.entities.livingtree.trunkgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;
import com.beangamecore.entities.livingtree.TreeGenerationContext;
import com.beangamecore.entities.livingtree.WoodType;

public class TwistedTrunkGenerator implements TrunkGenerator {
    @Override
    public void generateTrunk(TreeGenerationContext context) {
        TreeConfig config = context.getTreeConfig();
        List<BlockPosition> placedBlocks = context.getPlacedBlocks();
        Random random = context.getRandom();
        
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();

        // Create main twisted trunk
        int currentX = centerX;
        int currentZ = centerZ;
        
        for (int y = 0; y < config.getTreeHeight(); y++) {
            placedBlocks.add(new BlockPosition(currentX, centerY + y, currentZ, config.getWoodType().log));
            
            // Add twisting effect
            if (shouldTwistAtHeight(y, config.getTreeHeight(), config.getWoodType(), random)) {
                currentX += random.nextInt(3) - 1;
                currentZ += random.nextInt(3) - 1;
                currentX = Math.max(centerX - getMaxTwistDistance(config.getWoodType()), 
                                  Math.min(centerX + getMaxTwistDistance(config.getWoodType()), currentX));
                currentZ = Math.max(centerZ - getMaxTwistDistance(config.getWoodType()), 
                                  Math.min(centerZ + getMaxTwistDistance(config.getWoodType()), currentZ));
            }
        }
    }
    
    private boolean shouldTwistAtHeight(int currentHeight, int treeHeight, WoodType woodType, Random random) {
        if (woodType == WoodType.MANGROVE) {
            return currentHeight > 2 && currentHeight < treeHeight - 2 && random.nextDouble() < 0.3;
        } else if (woodType == WoodType.CRIMSON) {
            return currentHeight > 1 && random.nextDouble() < 0.4;
        } else if (woodType == WoodType.WARPED) {
            return currentHeight > 2 && random.nextDouble() < 0.25;
        }
        return false;
    }
    
    private int getMaxTwistDistance(WoodType woodType) {
        if (woodType == WoodType.MANGROVE) return 2;
        if (woodType == WoodType.CRIMSON) return 3;
        if (woodType == WoodType.WARPED) return 2;
        return 2;
    }
}
