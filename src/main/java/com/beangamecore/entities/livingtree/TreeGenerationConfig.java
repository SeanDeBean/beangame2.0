package com.beangamecore.entities.livingtree;

public class TreeGenerationConfig {
    private final double growthTimeSeconds;
    private final double decayTimeSeconds;
    private final int waitTicks;
    private final boolean enableParticles;
    private final boolean enableSounds;
    private final int maxBlocksPerTick;
    
    public TreeGenerationConfig(double growthTimeSeconds, double decayTimeSeconds, int waitTicks,
                              boolean enableParticles, boolean enableSounds, int maxBlocksPerTick) {
        this.growthTimeSeconds = growthTimeSeconds;
        this.decayTimeSeconds = decayTimeSeconds;
        this.waitTicks = waitTicks;
        this.enableParticles = enableParticles;
        this.enableSounds = enableSounds;
        this.maxBlocksPerTick = maxBlocksPerTick;
    }
    
    // Getters
    public int getGrowthTicks() { return (int)(growthTimeSeconds * 20); }
    public int getDecayTicks() { return (int)(decayTimeSeconds * 20); }
    public int getWaitTicks() { return waitTicks; }
    public boolean isEnableParticles() { return enableParticles; }
    public boolean isEnableSounds() { return enableSounds; }
    public int getMaxBlocksPerTick() { return maxBlocksPerTick; }
    
    // Builder pattern for easy configuration
    public static Builder builder() {
        return new Builder();
    }
    
    // Default configuration
    public static TreeGenerationConfig getDefault() {
        return new TreeGenerationConfig(4.0, 6.0, 20, true, false, 10);
    }
    
    // Fast configuration (for testing)
    public static TreeGenerationConfig getFast() {
        return new TreeGenerationConfig(2.0, 3.0, 10, true, false, 20);
    }
    
    // Performance configuration (minimal effects)
    public static TreeGenerationConfig getPerformance() {
        return new TreeGenerationConfig(3.0, 4.0, 10, false, false, 15);
    }
    
    // Builder class
    public static class Builder {
        private double growthTimeSeconds = 4.0;
        private double decayTimeSeconds = 6.0;
        private int waitTicks = 20;
        private boolean enableParticles = true;
        private boolean enableSounds = false;
        private int maxBlocksPerTick = 10;
        
        public Builder growthTimeSeconds(double growthTimeSeconds) {
            this.growthTimeSeconds = growthTimeSeconds;
            return this;
        }
        
        public Builder decayTimeSeconds(double decayTimeSeconds) {
            this.decayTimeSeconds = decayTimeSeconds;
            return this;
        }
        
        public Builder waitTicks(int waitTicks) {
            this.waitTicks = waitTicks;
            return this;
        }
        
        public Builder enableParticles(boolean enableParticles) {
            this.enableParticles = enableParticles;
            return this;
        }
        
        public Builder enableSounds(boolean enableSounds) {
            this.enableSounds = enableSounds;
            return this;
        }
        
        public Builder maxBlocksPerTick(int maxBlocksPerTick) {
            this.maxBlocksPerTick = maxBlocksPerTick;
            return this;
        }
        
        public TreeGenerationConfig build() {
            return new TreeGenerationConfig(
                growthTimeSeconds, 
                decayTimeSeconds, 
                waitTicks, 
                enableParticles, 
                enableSounds, 
                maxBlocksPerTick
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "TreeGenerationConfig{growth=%.1fs, decay=%.1fs, wait=%dt, particles=%s, sounds=%s, maxBlocks=%d}",
            growthTimeSeconds, decayTimeSeconds, waitTicks, enableParticles, enableSounds, maxBlocksPerTick
        );
    }
}
