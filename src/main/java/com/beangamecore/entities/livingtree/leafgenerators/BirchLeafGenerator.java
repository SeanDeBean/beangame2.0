package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;

public class BirchLeafGenerator implements LeafGenerator {
    @Override
    public void generateLeaves(CanopyCenter center, TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int radius = 4 + (config.getTreeHeight() / 5);
        int height = 7 + (config.getTreeHeight() / 6);
        LeafUtils.generateLeavesFromCenter(center, config, placedBlocks, radius, height, 0.4, "sphere", random);
    }
}

