package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;

public class SpruceLeafGenerator implements LeafGenerator {
    @Override
    public void generateLeaves(CanopyCenter center, TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int baseRadius = 2 + (config.getTreeHeight() / 6);
        int height = 10 + (config.getTreeHeight() / 2);
        LeafUtils.generatePointyConeLeaves(center, config, placedBlocks, baseRadius, height, random);
    }
}
