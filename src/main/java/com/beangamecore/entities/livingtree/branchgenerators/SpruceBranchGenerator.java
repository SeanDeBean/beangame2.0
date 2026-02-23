package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class SpruceBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        int trunkTopY = centerY + config.getTreeHeight() - 1;
        
        // Tall, minimal side branching
        for (int yOffset = 0; yOffset < 6; yOffset += 2) {
            BranchUtils.generateRadialBranches(centerX, trunkTopY - yOffset, centerZ, 3, 1 + yOffset/2, 2, config, placedBlocks, random);
        }
    }
}
