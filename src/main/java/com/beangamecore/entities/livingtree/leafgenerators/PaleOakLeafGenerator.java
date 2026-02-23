package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;

public class PaleOakLeafGenerator implements LeafGenerator {
    @Override
    public void generateLeaves(CanopyCenter center, TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int radius = 3 + (config.getTreeHeight() / 4);
        int height = 7 + (config.getTreeHeight() / 5);
        LeafUtils.generateLeavesFromCenter(center, config, placedBlocks, radius, height, 0.35, "cylinder", random);
    }
}
