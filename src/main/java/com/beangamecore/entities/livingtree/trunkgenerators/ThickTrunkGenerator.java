package com.beangamecore.entities.livingtree.trunkgenerators;

import java.util.List;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;
import com.beangamecore.entities.livingtree.TreeGenerationContext;

public class ThickTrunkGenerator implements TrunkGenerator {
    @Override
    public void generateTrunk(TreeGenerationContext context) {
        TreeConfig config = context.getTreeConfig();
        List<BlockPosition> placedBlocks = context.getPlacedBlocks();
        
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();

        // Create thick 2x2 trunk base
        for (int y = 0; y < Math.min(3, config.getTreeHeight()); y++) {
            placedBlocks.add(new BlockPosition(centerX, centerY + y, centerZ, config.getWoodType().log));
            placedBlocks.add(new BlockPosition(centerX + 1, centerY + y, centerZ, config.getWoodType().log));
            placedBlocks.add(new BlockPosition(centerX, centerY + y, centerZ + 1, config.getWoodType().log));
            placedBlocks.add(new BlockPosition(centerX + 1, centerY + y, centerZ + 1, config.getWoodType().log));
        }
        
        // Continue with single trunk above the thick base
        for (int y = 3; y < config.getTreeHeight(); y++) {
            placedBlocks.add(new BlockPosition(centerX, centerY + y, centerZ, config.getWoodType().log));
        }
    }
}
