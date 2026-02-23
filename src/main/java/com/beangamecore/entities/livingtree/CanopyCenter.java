package com.beangamecore.entities.livingtree;

import java.util.List;

import org.bukkit.Material;

public class CanopyCenter {
    private int x;
    private int y;
    private int z;
    
    public CanopyCenter(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }
    
    public void updateFromBlocks(List<BlockPosition> placedBlocks, WoodType woodType) {
        int totalX = 0, totalY = 0, totalZ = 0;
        int branchBlockCount = 0;

        for (BlockPosition pos : placedBlocks) {
            if (isBranchMaterial(pos.material, woodType)) {
                totalX += pos.x;
                totalY += pos.y;
                totalZ += pos.z;
                branchBlockCount++;
            }
        }

        if (branchBlockCount > 0) {
            this.x = totalX / branchBlockCount;
            this.y = totalY / branchBlockCount;
            this.z = totalZ / branchBlockCount;
        }
    }
    
    private boolean isBranchMaterial(Material material, WoodType woodType) {
        return material == woodType.log || material == woodType.planks || 
               material == woodType.fence || material == woodType.fenceGate;
    }
}
