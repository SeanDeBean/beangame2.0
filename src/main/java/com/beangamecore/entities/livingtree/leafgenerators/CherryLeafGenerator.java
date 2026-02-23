package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;

public class CherryLeafGenerator implements LeafGenerator {
    @Override
    public void generateLeaves(CanopyCenter center, TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int radius = 3 + (config.getTreeHeight() / 5);
        int height = 5 + (config.getTreeHeight() / 6);
        LeafUtils.generateLeavesFromCenter(center, config, placedBlocks, radius, height, 0.35, "dome", random);
    }
}
