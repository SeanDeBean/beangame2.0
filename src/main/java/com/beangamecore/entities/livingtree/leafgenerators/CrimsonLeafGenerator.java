package com.beangamecore.entities.livingtree.leafgenerators;

import java.util.List;
import java.util.Random;

import com.beangamecore.entities.livingtree.BlockPosition;
import com.beangamecore.entities.livingtree.CanopyCenter;
import com.beangamecore.entities.livingtree.TreeConfig;

public class CrimsonLeafGenerator implements LeafGenerator {
    @Override
    public void generateLeaves(CanopyCenter center, TreeConfig config, List<BlockPosition> placedBlocks, Random random) {
        int radius = getCanopyRadius(config);
        LeafUtils.generateVerticalRangeLeaves(center, config, placedBlocks, radius, 4, 0.3, random);
    }
    
    private int getCanopyRadius(TreeConfig config) {
        int baseRadius = 2 + (config.getTreeHeight() / 4);
        return baseRadius; // Crimson has base radius
    }
}

