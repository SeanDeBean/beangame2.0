package com.beangamecore.items.generic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public abstract class BeangameFish extends BeangameItem {

    private boolean isFishable; // is this fishable? if not, it is just a decorative item
    private int weight; // the weight/chance of the fish - lower weight = rarer = more XP
    private Set<Biome> allowedBiomes; // biomes where this fish can be caught
    private int minWaterDepth; // minimum water depth required
    private boolean requiresRain; // if this fish only appears during rain
    private TimeOfDay preferredTime; // time of day preference
    private int minPlayerLevel; // minimum player level required to catch
    
    public enum TimeOfDay {
        ANY, DAY, NIGHT, DAWN, DUSK
    }

    public BeangameFish() {
        super();
        initializeFishDefaults();
    }

    public BeangameFish(String namespace) {
        super(namespace);
        initializeFishDefaults();
    }

    private void initializeFishDefaults() {
        this.isFishable = true;
        this.weight = 100;
        this.allowedBiomes = new HashSet<>();
        this.minWaterDepth = 1;
        this.requiresRain = false;
        this.preferredTime = TimeOfDay.ANY;
        this.minPlayerLevel = 0;
    }

    public boolean getIsFishable() {
        return isFishable;
    }

    public void setIsFishable(boolean value) {
        isFishable = value;
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

    /**
     * Checks if this fish can be caught in the given location and conditions
     */
    public boolean canBeCaught(Location location, Player player, boolean isRaining) {
        if (!isFishable) return false;
        
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
    public FishRarity getRarity() {
        if (weight <= 5) return FishRarity.LEGENDARY;
        if (weight <= 25) return FishRarity.EPIC;
        if (weight <= 50) return FishRarity.RARE;
        if (weight <= 100) return FishRarity.UNCOMMON;
        return FishRarity.COMMON;
    }

    public enum FishRarity {
        COMMON("§fCommon", 1.0),
        UNCOMMON("§aUncommon", 1.5),
        RARE("§9Rare", 2.0),
        EPIC("§5Epic", 10.0),
        LEGENDARY("§6Legendary", 100.0);

        private final String displayName;
        private final double xpMultiplier;

        FishRarity(String displayName, double xpMultiplier) {
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
        while (current.getType() == Material.WATER && depth < 20) { // Max 20 blocks deep check
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
     * Called when this fish is caught by a player
     */
    public void onCaught(Player player, ItemStack fishingRod) {
        int exp = calculateExperience();
        player.giveExp(exp);
    }

    /**
     * Calculate experience awarded for catching this fish
     * Lower weight = rarer fish = more experience
     */
    protected int calculateExperience() {
        FishRarity rarity = getRarity();
        int baseXP = Math.max(1, (100 - weight) / 10); // Inverse relationship with weight
        return (int) (baseXP * rarity.getXpMultiplier());
    }

    @Override
    public int getMaxStackSize(){
        return 64;
    };

    @Override
    public Map<String, Integer> getEnchantments(){
        return Map.of();
    };

    @Override
    public CraftingRecipe getCraftingRecipe(){
        return null;
    };

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot() {
        return null;
    }
}
