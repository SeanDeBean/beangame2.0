package com.beangamecore.entities.livingtree;

import java.util.Arrays;
import java.util.function.Predicate;

public enum BiomeType {
    FLOWER(biome -> biome.contains("FLOWER") || biome.contains("TULIP") || biome.contains("SUNFLOWER")),
    FOREST(biome -> biome.contains("FOREST") || biome.contains("WOOD") || biome.contains("JUNGLE") || 
                   biome.contains("TAIGA") || biome.contains("BIRCH") || biome.contains("GROVE")),
    OTHER(biome -> false); // Default case

    private final Predicate<String> matcher;

    BiomeType(Predicate<String> matcher) {
        this.matcher = matcher;
    }

    public static BiomeType fromString(String biomeName) {
        String upperBiome = biomeName.toUpperCase();
        return Arrays.stream(values())
                .filter(type -> type != OTHER && type.matcher.test(upperBiome))
                .findFirst()
                .orElse(OTHER);
    }
}
