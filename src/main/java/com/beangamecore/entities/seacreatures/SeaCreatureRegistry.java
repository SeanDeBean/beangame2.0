package com.beangamecore.entities.seacreatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.Main;

public class SeaCreatureRegistry {
    private static final Map<String, BeangameSeaCreature> registry = new ConcurrentHashMap<>();
    private static final Map<UUID, BeangameSeaCreature> activeCreatures = new ConcurrentHashMap<>();

    /**
     * Register a sea creature in the registry
     */
    public static void registerSeaCreature(BeangameSeaCreature creature) {
        String key = creature.getDisplayName().toLowerCase().replace(" ", "_");
        registry.put(key, creature);
        Main.logger().info("Registered Sea Creature: [" + creature.getDisplayName() + "] with weight: " + creature.getWeight());
    }

    /**
     * Get a sea creature by its display name
     */
    public static Optional<BeangameSeaCreature> getSeaCreature(String displayName) {
        String key = displayName.toLowerCase().replace(" ", "_");
        return Optional.ofNullable(registry.get(key));
    }

    /**
     * Get all registered sea creatures
     */
    public static Collection<BeangameSeaCreature> getAllSeaCreatures() {
        return new ArrayList<>(registry.values());
    }

    /**
     * Get all spawnable sea creatures
     */
    public static Collection<BeangameSeaCreature> getSpawnableSeaCreatures() {
        return registry.values().stream()
                .filter(BeangameSeaCreature::getIsSpawnable)
                .collect(Collectors.toList());
    }

    /**
     * Get eligible sea creatures for the given location and conditions
     */
    public static Collection<BeangameSeaCreature> getEligibleSeaCreatures(Location location, Player player, boolean isRaining) {
        return getSpawnableSeaCreatures().stream()
                .filter(creature -> creature.canSpawn(location, player, isRaining))
                .collect(Collectors.toList());
    }

    /**
     * Get total weight of all eligible sea creatures
     */
    public static int getTotalWeight(Location location, Player player, boolean isRaining) {
        return getEligibleSeaCreatures(location, player, isRaining).stream()
                .mapToInt(BeangameSeaCreature::getWeight)
                .sum();
    }

    /**
     * Select a random sea creature based on weights
     */
    public static Optional<BeangameSeaCreature> selectRandomSeaCreature(Location location, Player player, boolean isRaining, Random random) {
        Collection<BeangameSeaCreature> eligibleCreatures = getEligibleSeaCreatures(location, player, isRaining);


        if (eligibleCreatures.isEmpty()) {
            return Optional.empty();
        }

        int totalWeight = getTotalWeight(location, player, isRaining);
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (BeangameSeaCreature creature : eligibleCreatures) {
            currentWeight += creature.getWeight();
            if (randomValue < currentWeight) {
                return Optional.of(creature);
            }
        }

        return Optional.empty();
    }

    /**
     * Register an active sea creature for ticking
     */
    public static void registerActiveCreature(BeangameSeaCreature creature) {
        if (creature.getMainEntity() != null) {
            // Register all entities, not just the main one
            for (Entity entity : creature.getAllEntities()) {
                activeCreatures.put(entity.getUniqueId(), creature);
            }
            creature.setActive(true);
        }
    }

    /**
     * Unregister an active sea creature
     */
    public static void unregisterActiveCreature(UUID entityId) {
        BeangameSeaCreature creature = activeCreatures.remove(entityId);
        if (creature != null) {
            // Remove all entities of this creature from active tracking
            for (Entity entity : creature.getAllEntities()) {
                activeCreatures.remove(entity.getUniqueId());
            }
            creature.setActive(false);
            creature.onDespawn();
        }
    }

    /**
     * Unregister all entities of a sea creature
     */
    public static void unregisterAllCreatureEntities(BeangameSeaCreature creature) {
        for (Entity entity : creature.getAllEntities()) {
            activeCreatures.remove(entity.getUniqueId());
        }
        creature.setActive(false);
        creature.onDespawn();
    }

    /**
     * Tick all active sea creatures
     */
    public void tickAllActiveCreatures() {
        Iterator<Map.Entry<UUID, BeangameSeaCreature>> iterator = activeCreatures.entrySet().iterator();
        Set<BeangameSeaCreature> tickedCreatures = new HashSet<>();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, BeangameSeaCreature> entry = iterator.next();
            BeangameSeaCreature creature = entry.getValue();
            
            // Skip if we already ticked this creature this cycle
            if (tickedCreatures.contains(creature)) {
                continue;
            }
            
            // Check if the main entity is dead - if so, remove entire creature
            if (creature.isMainEntityDead()) {
                unregisterAllCreatureEntities(creature);
                iterator.remove();
                continue;
            }
            
            // Check if any entity is invalid
            if (creature.getMainEntity() == null || !creature.isActive()) {
                unregisterAllCreatureEntities(creature);
                iterator.remove();
                continue;
            }
            
            // Tick the creature (only once per tick cycle)
            creature.tick();
            tickedCreatures.add(creature);
        }
    }

    /**
     * Get all active sea creatures
     */
    public static Collection<BeangameSeaCreature> getActiveCreatures() {
        return new ArrayList<>(activeCreatures.values());
    }

    /**
     * Get sea creatures by rarity
     */
    public static Collection<BeangameSeaCreature> getSeaCreaturesByRarity(BeangameSeaCreature.SeaCreatureRarity rarity) {
        return registry.values().stream()
                .filter(creature -> creature.getRarity() == rarity)
                .collect(Collectors.toList());
    }

    /**
     * Get sea creatures that can spawn in a specific biome
     */
    public static Collection<BeangameSeaCreature> getSeaCreaturesForBiome(Biome biome) {
        return registry.values().stream()
                .filter(creature -> creature.getAllowedBiomes().isEmpty() || creature.getAllowedBiomes().contains(biome))
                .collect(Collectors.toList());
    }

    /**
     * Unregister a sea creature from the main registry
     */
    public static boolean unregisterSeaCreature(String displayName) {
        String key = displayName.toLowerCase().replace(" ", "_");
        return registry.remove(key) != null;
    }

    /**
     * Clear all registered sea creatures
     */
    public void clearRegistry() {
        registry.clear();
        // Also clear active creatures
        for (BeangameSeaCreature creature : activeCreatures.values()) {
            creature.onDespawn();
        }
        activeCreatures.clear();
    }

    /**
     * Get registry size
     */
    public static int getRegistrySize() {
        return registry.size();
    }

    /**
     * Check if a sea creature is registered
     */
    public static boolean isRegistered(String displayName) {
        String key = displayName.toLowerCase().replace(" ", "_");
        return registry.containsKey(key);
    }

    /**
     * Get sea creature names for debugging or UI purposes
     */
    public static List<String> getSeaCreatureNames() {
        return registry.values().stream()
                .map(BeangameSeaCreature::getDisplayName)
                .collect(Collectors.toList());
    }

    /**
     * Handle entity death - check if it's an active sea creature
     */
    public void handleEntityDeath(Entity entity) {
        BeangameSeaCreature creature = activeCreatures.get(entity.getUniqueId());
        if (creature != null) {
            // If the main entity died, defeat the entire creature
            if (entity.equals(creature.getMainEntity())) {
                Player killer = findNearestPlayer(entity.getLocation());
                if (killer != null) {
                    creature.onDefeated(killer);
                    
                    // Drop loot
                    for (ItemStack loot : creature.getLootDrops()) {
                        entity.getWorld().dropItemNaturally(entity.getLocation(), loot);
                    }
                }
                unregisterAllCreatureEntities(creature);
            } else {
                // Just remove this specific entity from tracking
                creature.removeEntity(entity);
                activeCreatures.remove(entity.getUniqueId());
            }
        }
    }

    public void handleEntityRemove(Entity entity) {
        BeangameSeaCreature creature = activeCreatures.get(entity.getUniqueId());
        if (creature != null) {
            // If the main entity is being removed, remove entire creature
            if (entity.equals(creature.getMainEntity())) {
                unregisterAllCreatureEntities(creature);
            } else {
                // Just remove this specific entity
                creature.removeEntity(entity);
                activeCreatures.remove(entity.getUniqueId());
            }
        }
    }

    private static Player findNearestPlayer(Location location) {
        return location.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(location) < 30) // 30 block range
                .min(Comparator.comparing(p -> p.getLocation().distance(location)))
                .orElse(null);
    }

    /**
     * Print registry info for debugging
     */
    public static void printRegistryInfo() {
        Main.logger().info("=== Sea Creature Registry ===");
        Main.logger().info("Total registered: " + getRegistrySize());
        Main.logger().info("Active creatures: " + activeCreatures.size());
        
        for (BeangameSeaCreature creature : registry.values()) {
            Main.logger().info(" - " + creature.getDisplayName() + 
                             " (Weight: " + creature.getWeight() + 
                             ", Rarity: " + creature.getRarity().getDisplayName() + 
                             ", Spawnable: " + creature.getIsSpawnable() + ")");
        }
    }
}
