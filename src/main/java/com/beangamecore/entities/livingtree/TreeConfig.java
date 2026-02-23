package com.beangamecore.entities.livingtree;

import java.util.Random;

import org.bukkit.Location;

public class TreeConfig {
    private final WoodType woodType;
    private final int treeHeight;
    private final Location baseLocation;
    private final int minLeafY;
    
    public TreeConfig(WoodType woodType, Location baseLocation, Random random) {
        this.woodType = woodType;
        this.baseLocation = baseLocation.subtract(0, 1, 0);
        this.treeHeight = woodType.minHeight + random.nextInt(woodType.maxHeight - woodType.minHeight + 1);
        this.minLeafY = this.baseLocation.getBlockY() + (treeHeight / 3);
    }
    
    // Getters
    public WoodType getWoodType() { return woodType; }
    public int getTreeHeight() { return treeHeight; }
    public Location getBaseLocation() { return baseLocation; }
    public int getMinLeafY() { return minLeafY; }
}
