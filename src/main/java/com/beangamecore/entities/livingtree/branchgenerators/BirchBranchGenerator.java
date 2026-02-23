package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class BirchBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        int trunkTopY = centerY + config.getTreeHeight() - 1;
        
        // Slender, graceful branches
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 2, centerZ, 3, 4, 2, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 4, centerZ, 2, 3, 1, config, placedBlocks, random);
    }
}
