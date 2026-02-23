package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class OakBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        int trunkTopY = centerY + config.getTreeHeight() - 1;
        
        int branchLength = config.getWoodType().minBranchLength + random.nextInt(2);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 2, centerZ, 4, branchLength, 3, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 4, centerZ, 4, branchLength - 1, 2, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 6, centerZ, 4, branchLength - 2, 1, config, placedBlocks, random);
        
        // Top branches
        BranchUtils.generateRadialBranches(centerX, trunkTopY, centerZ, 3, 1, 1, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 1, centerZ, 3, 2, 1, config, placedBlocks, random);
    }
}
