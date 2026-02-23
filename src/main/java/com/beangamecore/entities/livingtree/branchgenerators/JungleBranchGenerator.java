package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public class JungleBranchGenerator implements BranchGenerator {
    @Override
    public void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int centerX = config.getBaseLocation().getBlockX();
        int centerY = config.getBaseLocation().getBlockY();
        int centerZ = config.getBaseLocation().getBlockZ();
        int trunkTopY = centerY + config.getTreeHeight() - 1;
        
        // Dense, complex branching
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 1, centerZ, 6, 5, 3, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 3, centerZ, 8, 4, 2, config, placedBlocks, random);
        BranchUtils.generateRadialBranches(centerX, trunkTopY - 5, centerZ, 6, 3, 1, config, placedBlocks, random);
    }
}
