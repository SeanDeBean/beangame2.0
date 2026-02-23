package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;

public class OakLeafGenerator implements LeafGenerator {
    @Override
    public void generateLeaves(CanopyCenter center, TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int radius = 4 + (config.getTreeHeight() / 3);
        int height = 5 + (config.getTreeHeight() / 4);
        LeafUtils.generateLeavesFromCenter(center, config, placedBlocks, radius, height, 0.4, "sphere", random);
        LeafUtils.generateBranchEndClusters(center, config, placedBlocks, radius / 2, height / 2, 0.3, "sphere", 0.6, random);
    }
}
