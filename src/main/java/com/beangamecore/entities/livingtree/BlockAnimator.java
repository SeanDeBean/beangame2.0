package com.beangamecore.entities.livingtree;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import com.beangamecore.Main;

public class BlockAnimator {
    
    /**
     * Animate the growth of blocks with particle effects
     */
    public static void animateGrowth(List<BlockPosition> sortedBlocks, World world, int growthTicks, Runnable onComplete) {
        int totalBlocks = sortedBlocks.size();
        if (totalBlocks == 0) {
            onComplete.run();
            return;
        }
        
        int blocksPerTick = Math.max(1, (int) Math.ceil((double) totalBlocks / growthTicks));

        int[] currentIndex = {0, 0};
        int[] taskId = new int[2];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (currentIndex[0] >= totalBlocks) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                onComplete.run();
                return;
            }

            int blocksThisTick = Math.min(blocksPerTick, totalBlocks - currentIndex[0]);

            for (int i = 0; i < blocksThisTick; i++) {
                if (currentIndex[0] >= totalBlocks) break;
                    
                BlockPosition pos = sortedBlocks.get(currentIndex[0]);
                currentIndex[0]++;

                if(pos.y <= -64 || pos.y >= 320) continue;

                Block block = world.getBlockAt(pos.x, pos.y, pos.z);
                    
                if (block.getType() == Material.AIR || shouldReplace(block.getType())) {
                    block.setType(pos.material);
                    spawnGrowthParticles(block.getLocation(), pos.material);
                }
            }
        },0, 1).getTaskId();

        taskId[1] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (currentIndex[1] >= totalBlocks) {
                Bukkit.getScheduler().cancelTask(taskId[1]);
                onComplete.run();
                return;
            }

            int blocksThisTick = Math.min(blocksPerTick, totalBlocks - currentIndex[1]);

            for (int i = 0; i < blocksThisTick; i++) {
                if (currentIndex[1] >= totalBlocks) break;
                    
                BlockPosition pos = sortedBlocks.get(currentIndex[1]);
                currentIndex[1]++;

                if(pos.y <= -64 || pos.y >= 320) continue;

                Block block = world.getBlockAt(pos.x, pos.y, pos.z);
                    
                if (block.getType() == Material.AIR || shouldReplace(block.getType())) {
                    block.setType(pos.material);
                    spawnGrowthParticles(block.getLocation(), pos.material);
                }
            }
        },0, 1).getTaskId();
    }
    
    /**
     * Animate the decay/removal of blocks with particle effects
     */
    public static void animateDecay(List<BlockPosition> sortedBlocks, World world, int decayTicks) {
        int totalBlocks = sortedBlocks.size();
        if (totalBlocks == 0) {
            return;
        }
        
        int blocksPerTick = Math.max(1, (int) Math.ceil((double) totalBlocks / decayTicks));

        int[] currentIndex = {0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (currentIndex[0] >= totalBlocks) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            int blocksThisTick = Math.min(blocksPerTick, totalBlocks - currentIndex[0]);

            for (int i = 0; i < blocksThisTick; i++) {
                if (currentIndex[0] >= totalBlocks) break;
                    
                BlockPosition pos = sortedBlocks.get(currentIndex[0]);
                Block block = world.getBlockAt(pos.x, pos.y, pos.z);
                        
                if (block.getType() == pos.material) {
                    block.setType(Material.AIR);
                    spawnDecayParticles(block.getLocation(), pos.material);
                }
                currentIndex[0]++;
            }
        }, 0, 1).getTaskId();

    }
    
    /**
     * Animate growth with custom particle effects per material type
     */
    public static void animateGrowthWithCustomParticles(List<BlockPosition> sortedBlocks, World world, 
                                                       int growthTicks, Runnable onComplete) {
        int totalBlocks = sortedBlocks.size();
        if (totalBlocks == 0) {
            onComplete.run();
            return;
        }
        
        int blocksPerTick = Math.max(1, (int) Math.ceil((double) totalBlocks / growthTicks));


        int[] currentIndex = {0};
        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (currentIndex[0] >= totalBlocks) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                onComplete.run();
                return;
            }

            int blocksThisTick = Math.min(blocksPerTick, totalBlocks - currentIndex[0]);

            for (int i = 0; i < blocksThisTick; i++) {
                if (currentIndex[0] >= totalBlocks) break;
                    
                BlockPosition pos = sortedBlocks.get(currentIndex[0]);
                Block block = world.getBlockAt(pos.x, pos.y, pos.z);
                    
                if (block.getType() == Material.AIR || shouldReplace(block.getType())) {
                    block.setType(pos.material);
                    spawnCustomGrowthParticles(block.getLocation(), pos.material);
                }
                currentIndex[0]++;
            }
        }, 0, 1).getTaskId();

    }
    
    /**
     * Check if a block type should be replaced during growth
     */
    private static boolean shouldReplace(Material existingType) {
        // Replace only certain block types (like grass, flowers, etc.)
        return existingType == Material.SHORT_GRASS || 
               existingType == Material.TALL_GRASS ||
               existingType == Material.FERN ||
               existingType == Material.LARGE_FERN ||
               existingType == Material.DANDELION ||
               existingType == Material.POPPY ||
               existingType.name().contains("_FLOWER");
    }
    
    /**
     * Spawn growth particles based on block material
     */
    private static void spawnGrowthParticles(Location location, Material material) {
        World world = location.getWorld();
        Location particleLoc = location.add(0.5, 0.5, 0.5);
        
        if (isWoodMaterial(material)) {
            // Brown particles for wood
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, particleLoc, 3, 0.2, 0.2, 0.2, 0.05,
                new Particle.DustTransition(
                    org.bukkit.Color.fromRGB(139, 69, 19),  // Brown
                    org.bukkit.Color.fromRGB(101, 67, 33),  // Darker brown
                    1.0f
                ));
        } else {
            // Default particles
            world.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 2, 0.2, 0.2, 0.2, 0.02);
        }
    }
    
    /**
     * Spawn decay particles based on block material
     */
    private static void spawnDecayParticles(Location location, Material material) {
        World world = location.getWorld();
        Location particleLoc = location.add(0.5, 0.5, 0.5);
        
        if (isWoodMaterial(material)) {
            // Brownish decay particles for wood
            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particleLoc, 2, 0.2, 0.2, 0.2, 0.02);
        } else if (isLeafMaterial(material)) {
            // Greenish decay particles for leaves
            world.spawnParticle(Particle.CLOUD, particleLoc, 3, 0.2, 0.2, 0.2, 0.02);
        } else {
            // Default decay particles
            world.spawnParticle(Particle.CLOUD, particleLoc, 2, 0.2, 0.2, 0.2, 0.02);
        }
    }
    
    /**
     * Spawn custom growth particles with more variety
     */
    private static void spawnCustomGrowthParticles(Location location, Material material) {
        World world = location.getWorld();
        Location particleLoc = location.add(0.5, 0.5, 0.5);
        
        if (isWoodMaterial(material)) {
            // Wood growth - brown particles with some sparkles
            world.spawnParticle(Particle.DUST_COLOR_TRANSITION, particleLoc, 2, 0.2, 0.2, 0.2, 0.05,
                new Particle.DustTransition(
                    org.bukkit.Color.fromRGB(139, 69, 19),
                    org.bukkit.Color.fromRGB(101, 67, 33),
                    1.0f
                ));
            world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
        } else if (isLeafMaterial(material)) {
            // Leaf growth - green particles with happy villager effect
            world.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 4, 0.3, 0.3, 0.3, 0.1);
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0.005);
        } else {
            // Default - mix of effects
            world.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 2, 0.2, 0.2, 0.2, 0.02);
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.1, 0.1, 0.1, 0.005);
        }
    }
    
    private static boolean isWoodMaterial(Material material) {
        return material.name().contains("_LOG") || 
               material.name().contains("_STEM") ||
               material.name().contains("_PLANKS") ||
               material.name().contains("_FENCE") ||
               material.name().contains("_GATE");
    }
    
    private static boolean isLeafMaterial(Material material) {
        return material.name().contains("_LEAVES") || 
               material == Material.NETHER_WART_BLOCK || 
               material == Material.WARPED_WART_BLOCK;
    }
    
    /**
     * Quick growth animation without particles (for performance)
     */
    public static void animateQuickGrowth(List<BlockPosition> sortedBlocks, World world, Runnable onComplete) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (BlockPosition pos : sortedBlocks) {
                    Block block = world.getBlockAt(pos.x, pos.y, pos.z);
                    if (block.getType() == Material.AIR || shouldReplace(block.getType())) {
                        block.setType(pos.material);
                    }
                }
                onComplete.run();
            }
        }.runTask(Main.getPlugin());
    }
}
