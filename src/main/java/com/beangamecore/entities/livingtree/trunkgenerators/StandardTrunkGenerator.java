package com.beangamecore.entities.livingtree.trunkgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;
import com.beangamecore.entities.livingtree.TreeGenerationContext;
import com.beangamecore.entities.livingtree.WoodType;

public class StandardTrunkGenerator implements TrunkGenerator {
    @Override
    public void generateTrunk(TreeGenerationContext context) {
        TreeConfig config = context.getTreeConfig();
        List<BlockPosition> placedBlocks = context.getPlacedBlocks();
        Random random = context.getRandom();
        
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();

        // Main trunk
        for (int y = 0; y < config.getTreeHeight(); y++) {
            placedBlocks.add(new BlockPosition(centerX, centerY + y, centerZ, config.getWoodType().log));
        }
        
        // Add extra logs at ground level for larger trees
        if (config.getTreeHeight() > 10 && shouldGenerateRoots(config.getWoodType())) {
            generateRoots(centerX, centerY, centerZ, config, placedBlocks, random);
        }
    }
    
    private boolean shouldGenerateRoots(WoodType woodType) {
        return woodType == WoodType.SPRUCE || woodType == WoodType.OAK || 
               woodType == WoodType.DARK_OAK || woodType == WoodType.JUNGLE;
    }
    
    private void generateRoots(int centerX, int centerY, int centerZ, TreeConfig config, 
                              List<BlockPosition> placedBlocks, Random random) {
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                if ((x != centerX || z != centerZ) && random.nextDouble() < 0.7) {
                    placedBlocks.add(new BlockPosition(x, centerY, z, config.getWoodType().log));
                    // Sometimes add a second layer
                    if (random.nextDouble() < 0.3) {
                        placedBlocks.add(new BlockPosition(x, centerY - 1, z, config.getWoodType().log));
                    }
                }
            }
        }
    }
}
