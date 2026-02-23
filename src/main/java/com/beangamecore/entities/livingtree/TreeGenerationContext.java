package com.beangamecore.entities.livingtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeGenerationContext {
    private final TreeConfig treeConfig;
    private final List<BlockPosition> placedBlocks;
    private final CanopyCenter canopyCenter;
    private final Random random;
    
    public TreeGenerationContext(TreeConfig treeConfig) {
        this.treeConfig = treeConfig;
        this.placedBlocks = new ArrayList<>();
        this.random = new Random();
        
        // Initialize canopy center at trunk top
        int centerX = treeConfig.getBaseLocation().getBlockX();
        int centerY = treeConfig.getBaseLocation().getBlockY() + treeConfig.getTreeHeight() - 1;
        int centerZ = treeConfig.getBaseLocation().getBlockZ();
        this.canopyCenter = new CanopyCenter(centerX, centerY, centerZ);
    }
    
    // Getters
    public TreeConfig getTreeConfig() { 
        return treeConfig; 
    }
    
    public List<BlockPosition> getPlacedBlocks() { 
        return placedBlocks; 
    }
    
    public CanopyCenter getCanopyCenter() { 
        return canopyCenter; 
    }
    
    public Random getRandom() { 
        return random; 
    }
    
    /**
     * Updates the canopy center based on the actual branch positions
     * This should be called after generating branches but before generating leaves
     */
    public void updateCanopyCenter() {
        canopyCenter.updateFromBlocks(placedBlocks, treeConfig.getWoodType());
    }
    
    /**
     * Adds a block to the placed blocks list
     */
    public void addBlock(BlockPosition block) {
        placedBlocks.add(block);
    }
    
    /**
     * Adds multiple blocks to the placed blocks list
     */
    public void addBlocks(List<BlockPosition> blocks) {
        placedBlocks.addAll(blocks);
    }
    
    /**
     * Checks if a block position is already occupied in the placed blocks
     */
    public boolean isPositionOccupied(int x, int y, int z) {
        return placedBlocks.stream()
                .anyMatch(block -> block.x == x && block.y == y && block.z == z);
    }
    
    /**
     * Gets the world from the base location
     */
    public org.bukkit.World getWorld() {
        return treeConfig.getBaseLocation().getWorld();
    }
    
    /**
     * Gets the center X coordinate of the tree base
     */
    public int getCenterX() {
        return treeConfig.getBaseLocation().getBlockX();
    }
    
    /**
     * Gets the center Y coordinate of the tree base
     */
    public int getCenterY() {
        return treeConfig.getBaseLocation().getBlockY();
    }
    
    /**
     * Gets the center Z coordinate of the tree base
     */
    public int getCenterZ() {
        return treeConfig.getBaseLocation().getBlockZ();
    }
    
    /**
     * Gets the trunk top Y coordinate
     */
    public int getTrunkTopY() {
        return getCenterY() + treeConfig.getTreeHeight() - 1;
    }
    
    /**
     * Gets the total number of blocks that will be placed
     */
    public int getTotalBlockCount() {
        return placedBlocks.size();
    }
    
    /**
     * Clears all placed blocks (useful for regeneration)
     */
    public void clearPlacedBlocks() {
        placedBlocks.clear();
    }
    
    /**
     * Validates that the context is ready for tree generation
     */
    public boolean isValid() {
        return treeConfig != null && 
               treeConfig.getBaseLocation() != null && 
               treeConfig.getBaseLocation().getWorld() != null &&
               treeConfig.getTreeHeight() > 0;
    }
    
    /**
     * Gets a string representation of the context for debugging
     */
    @Override
    public String toString() {
        return String.format("TreeGenerationContext{woodType=%s, height=%d, blocks=%d, center=%s}",
                treeConfig.getWoodType().name(),
                treeConfig.getTreeHeight(),
                placedBlocks.size(),
                canopyCenter.toString());
    }
}
