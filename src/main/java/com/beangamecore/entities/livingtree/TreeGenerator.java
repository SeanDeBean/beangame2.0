package com.beangamecore.entities.livingtree;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;
import com.beangamecore.entities.livingtree.branchgenerators.BranchGenerator;
import com.beangamecore.entities.livingtree.leafgenerators.LeafGenerator;
import com.beangamecore.entities.livingtree.trunkgenerators.TrunkGenerator;

public class TreeGenerator {
    private final TreeGenerationConfig config;
    private final TreeComponentFactory componentFactory;
    
    private static final double GROWTH_TIME_SECONDS = 4.0;
    private static final double DECAY_TIME_SECONDS = 6.0;
    private static final int GROWTH_TICKS = (int)(GROWTH_TIME_SECONDS * 20);
    private static final int DECAY_TICKS = (int)(DECAY_TIME_SECONDS * 20);
    private static final int WAIT_TICKS = 20;
    
    public TreeGenerator(TreeGenerationConfig config, TreeComponentFactory componentFactory) {
        this.config = config;
        this.componentFactory = componentFactory;
    }
    
    public void spawnTree(Player owner, Location location) {
        WoodType woodType = getValidWoodType();
        TreeConfig treeConfig = new TreeConfig(woodType, location, new Random());
        TreeGenerationContext context = new TreeGenerationContext(treeConfig);
        
        generateTree(context);
        
        if (shouldApplyBuff(owner)) {
            applyTreeBuffEffects(owner);
        }
    }
    
    private void generateTree(TreeGenerationContext context) {
        generateTrunk(context);
        generateBranches(context);
        context.updateCanopyCenter(); // Update center after branches are placed
        generateLeaves(context);
        animateGrowth(context);
    }
    
    private void generateTrunk(TreeGenerationContext context) {
        TrunkGenerator trunkGenerator = componentFactory.createTrunkGenerator(context.getTreeConfig().getWoodType());
        trunkGenerator.generateTrunk(context);
    }
    
    private void generateBranches(TreeGenerationContext context) {
        BranchGenerator branchGenerator = componentFactory.createBranchGenerator(context.getTreeConfig().getWoodType());
        branchGenerator.generateBranches(context.getTreeConfig(), context.getPlacedBlocks(), context.getRandom());
    }
    
    private void generateLeaves(TreeGenerationContext context) {
        LeafGenerator leafGenerator = componentFactory.createLeafGenerator(context.getTreeConfig().getWoodType());
        leafGenerator.generateLeaves(context.getCanopyCenter(), context.getTreeConfig(), context.getPlacedBlocks(), context.getRandom());
    }
    
    private WoodType getValidWoodType() {
        WoodType woodType;
        do {
            woodType = WoodType.getRandom();
        } while (woodType.equals(WoodType.CRIMSON) || woodType.equals(WoodType.WARPED));
        return woodType;
    }
    
    private boolean shouldApplyBuff(Player player) {
        // Add your buff logic here
        return false; // or implement your buff condition
    }
    
    private void applyTreeBuffEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 500, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 500, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 500, 1));
    }
    
    private void animateGrowth(TreeGenerationContext context) {
        List<BlockPosition> sortedBlocks = BlockSorter.sortForGrowth(
            context.getPlacedBlocks(), 
            context.getTreeConfig().getBaseLocation()
        );
        
        BlockAnimator.animateGrowth(sortedBlocks, context.getTreeConfig().getBaseLocation().getWorld(), GROWTH_TICKS, () -> {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                animateDecay(context);
            }, WAIT_TICKS);
        });
    }
    
    private void animateDecay(TreeGenerationContext context) {
        List<BlockPosition> sortedBlocks = BlockSorter.sortForDecay(
            context.getPlacedBlocks(), 
            context.getTreeConfig().getBaseLocation()
        );
        
        BlockAnimator.animateDecay(sortedBlocks, context.getTreeConfig().getBaseLocation().getWorld(), DECAY_TICKS);
    }
}
