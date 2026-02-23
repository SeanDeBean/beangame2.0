package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;

public class WarpedLeafGenerator implements LeafGenerator {
    @Override
    public void generateLeaves(CanopyCenter center, TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int radius = getCanopyRadius(config);
        LeafUtils.generateLayeredLeaves(center, config, placedBlocks, radius, 7, 0.4, random);
    }
    
    private int getCanopyRadius(TreeConfig config) {
        int baseRadius = 2 + (config.getTreeHeight() / 4);
        return baseRadius; // Warped has base radius
    }
}
