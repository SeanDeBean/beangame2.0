package com.beangamecore.entities.seacreatures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class BeangameSeaCreature {
    private boolean isSpawnable; // is this spawnable? if not, it's just a template
    private int weight; // the weight/chance of the sea creature - lower weight = rarer
    private Set<Biome> allowedBiomes; // biomes where this sea creature can spawn
    private int minWaterDepth; // minimum water depth required
    private boolean requiresRain; // if this sea creature only appears during rain
    private TimeOfDay preferredTime; // time of day preference
    private int minPlayerLevel; // minimum player level required to encounter

    private boolean isActive = false;
    private Entity mainEntity;
    private List<Entity> allEntities = new ArrayList<>();
    
    public enum TimeOfDay {
        ANY, DAY, NIGHT, DAWN, DUSK
    }

    public BeangameSeaCreature() {
        initializeSeaCreatureDefaults();
    }

    private void initializeSeaCreatureDefaults() {
        this.isSpawnable = true;
        this.weight = 100;
        this.allowedBiomes = new HashSet<>();
        this.minWaterDepth = 3; // Sea creatures typically need deeper water
        this.requiresRain = false;
        this.preferredTime = TimeOfDay.ANY;
        this.minPlayerLevel = 0;
    }

    public boolean getIsSpawnable() {
        return isSpawnable;
    }

    public void setIsSpawnable(boolean value) {
        isSpawnable = value;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int value) {
        weight = value;
    }

    public Set<Biome> getAllowedBiomes() {
        return new HashSet<>(allowedBiomes);
    }

    public void setAllowedBiomes(Set<Biome> biomes) {
        this.allowedBiomes = new HashSet<>(biomes);
    }

    public void addAllowedBiome(Biome biome) {
        this.allowedBiomes.add(biome);
    }

    public void removeAllowedBiome(Biome biome) {
        this.allowedBiomes.remove(biome);
    }

    public int getMinWaterDepth() {
        return minWaterDepth;
    }

    public void setMinWaterDepth(int depth) {
        this.minWaterDepth = depth;
    }

    public boolean requiresRain() {
        return requiresRain;
    }

    public void setRequiresRain(boolean requiresRain) {
        this.requiresRain = requiresRain;
    }

    public TimeOfDay getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(TimeOfDay time) {
        this.preferredTime = time;
    }

    public int getMinPlayerLevel() {
        return minPlayerLevel;
    }

    public void setMinPlayerLevel(int level) {
        this.minPlayerLevel = level;
    }

    public List<Entity> getAllEntities() {
        return new ArrayList<>(allEntities);
    }

    public void addEntity(Entity entity) {
        if (!allEntities.contains(entity)) {
            allEntities.add(entity);
        }
    }

    public void removeEntity(Entity entity) {
        allEntities.remove(entity);
    }

    public boolean isAnyEntityDead() {
        return allEntities.stream().anyMatch(Entity::isDead);
    }

    public boolean isMainEntityDead() {
        return mainEntity != null && mainEntity.isDead();
    }

    public void removeAllEntities() {
        for (Entity entity : allEntities) {
            if (!entity.isDead()) {
                entity.remove();
            }
        }
        allEntities.clear();
    }

    /**
     * Checks if this sea creature can spawn in the given location and conditions
     */
    public boolean canSpawn(Location location, Player player, boolean isRaining) {
        if (!isSpawnable) return false;
        
        // Check player level
        if (player.getLevel() < minPlayerLevel) {
            return false;
        }
        
        // Check biome
        if (!allowedBiomes.isEmpty() && !allowedBiomes.contains(location.getBlock().getBiome())) {
            return false;
        }
        
        // Check rain requirement
        if (requiresRain && !isRaining) {
            return false;
        }
        
        // Check water depth
        if (!hasSufficientWaterDepth(location)) {
            return false;
        }
        
        // Check time of day
        if (!isPreferredTime(location)) {
            return false;
        }
        
        return true;
    }

    /**
     * Get rarity based on weight - lower weight = rarer
     */
    public SeaCreatureRarity getRarity() {
        if (weight <= 5) return SeaCreatureRarity.LEGENDARY;
        if (weight <= 25) return SeaCreatureRarity.EPIC;
        if (weight <= 50) return SeaCreatureRarity.RARE;
        if (weight <= 100) return SeaCreatureRarity.UNCOMMON;
        return SeaCreatureRarity.COMMON;
    }

    public enum SeaCreatureRarity {
        COMMON("§fCommon", 1.0),
        UNCOMMON("§aUncommon", 1.5),
        RARE("§9Rare", 2.0),
        EPIC("§5Epic", 10.0),
        LEGENDARY("§6Legendary", 100.0);

        private final String displayName;
        private final double xpMultiplier;

        SeaCreatureRarity(String displayName, double xpMultiplier) {
            this.displayName = displayName;
            this.xpMultiplier = xpMultiplier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getXpMultiplier() {
            return xpMultiplier;
        }
    }

    private boolean hasSufficientWaterDepth(Location location) {
        int depth = 0;
        Block surfaceBlock = location.getBlock();
        
        // Check downward from the surface to find how deep the water is
        Block current = surfaceBlock.getRelative(BlockFace.DOWN);
        while (current.getType() == Material.WATER && depth < 20) {
            depth++;
            current = current.getRelative(BlockFace.DOWN);
        }
        
        return depth >= minWaterDepth;
    }

    private boolean isPreferredTime(Location location) {
        if (preferredTime == TimeOfDay.ANY) return true;
        
        long time = location.getWorld().getTime();
        
        switch (preferredTime) {
            case DAY:
                return time < 12300 || time > 23850;
            case NIGHT:
                return time >= 12300 && time <= 23850;
            case DAWN:
                return time >= 23000 && time <= 24000 || time >= 0 && time <= 1000;
            case DUSK:
                return time >= 12000 && time <= 13000;
            default:
                return true;
        }
    }

    /**
     * Calculate experience awarded for defeating this sea creature
     * Lower weight = rarer creature = more experience
     */
    protected int calculateExperience() {
        SeaCreatureRarity rarity = getRarity();
        int baseXP = Math.max(1, (100 - weight) / 10);
        return (int) (baseXP * rarity.getXpMultiplier());
    }

    /**
     * Spawn the sea creature entity/entities
     * This is where you can create complex multi-entity setups
     */
    public abstract void spawn(Location location, Player player);

    /**
     * Called when the sea creature is defeated
     */
    public abstract void onDefeated(Player player);

    /**
     * Get the display name for this sea creature
     */
    public abstract String getDisplayName();

    /**
     * Tick method called every server tick while the creature is active
     * Override this to add custom behavior, movement, attacks, etc.
     */
    public void tick() {
        // Default empty
    }

    public List<ItemStack> getLootDrops() {
        return new ArrayList<>();
    }

    public void onSpawn(Player player) {
        // Default empty
    }

    /**
     * Called when the sea creature is removed/despawns
     */
    public void onDespawn() {
        isActive = false;
        removeAllEntities();
        mainEntity = null;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public Entity getMainEntity() {
        return mainEntity;
    }

    public void setMainEntity(Entity mainEntity) {
        this.mainEntity = mainEntity;
        if (mainEntity != null) {
            this.isActive = true;
            addEntity(mainEntity);
        }
    }
}
