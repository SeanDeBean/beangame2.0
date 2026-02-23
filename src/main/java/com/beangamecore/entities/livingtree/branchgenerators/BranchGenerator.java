package com.beangamecore.entities.livingtree.branchgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.TreeConfig;

public interface BranchGenerator {
    void generateBranches(TreeConfig config, List<BlockPosition> placedBlocks, Random random);
}

