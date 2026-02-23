package com.beangamecore.entities.livingtree;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;

public enum WoodType {
        OAK(Material.OAK_LOG, Material.OAK_PLANKS, Material.OAK_FENCE, Material.OAK_FENCE_GATE, 8, 12, "§6Oak", 5, 7),
        SPRUCE(Material.SPRUCE_LOG, Material.SPRUCE_PLANKS, Material.SPRUCE_FENCE, Material.SPRUCE_FENCE_GATE, 10, 16, "§2Spruce", 3, 5),
        BIRCH(Material.BIRCH_LOG, Material.BIRCH_PLANKS, Material.BIRCH_FENCE, Material.BIRCH_FENCE_GATE, 6, 10, "§eBirch", 4, 6),
        JUNGLE(Material.JUNGLE_LOG, Material.JUNGLE_PLANKS, Material.JUNGLE_FENCE, Material.JUNGLE_FENCE_GATE, 8, 14, "§aJungle", 6, 8),
        ACACIA(Material.ACACIA_LOG, Material.ACACIA_PLANKS, Material.ACACIA_FENCE, Material.ACACIA_FENCE_GATE, 7, 12, "§6Acacia", 7, 9),
        DARK_OAK(Material.DARK_OAK_LOG, Material.DARK_OAK_PLANKS, Material.DARK_OAK_FENCE, Material.DARK_OAK_FENCE_GATE, 8, 14, "§8Dark Oak", 5, 7),
        MANGROVE(Material.MANGROVE_LOG, Material.MANGROVE_PLANKS, Material.MANGROVE_FENCE, Material.MANGROVE_FENCE_GATE, 9, 13, "§3Mangrove", 4, 6),
        CHERRY(Material.CHERRY_LOG, Material.CHERRY_PLANKS, Material.CHERRY_FENCE, Material.CHERRY_FENCE_GATE, 7, 11, "§dCherry", 4, 6),
        PALE_OAK(Material.PALE_OAK_LOG, Material.PALE_OAK_PLANKS, Material.PALE_OAK_FENCE, Material.PALE_OAK_FENCE_GATE, 8, 14, "§fPale Oak", 5, 7),
        CRIMSON(Material.CRIMSON_STEM, Material.CRIMSON_PLANKS, Material.CRIMSON_FENCE, Material.CRIMSON_FENCE_GATE, 8, 14, "§cCrimson", 5, 7),
        WARPED(Material.WARPED_STEM, Material.WARPED_PLANKS, Material.WARPED_FENCE, Material.WARPED_FENCE_GATE, 8, 14, "§3Warped", 5, 7);

        public final Material log;
        public final Material planks;
        public final Material fence;
        public final Material fenceGate;
        public final int minHeight;
        public final int maxHeight;
        public final int minBranchLength;

        WoodType(Material log, Material planks, Material fence, Material fenceGate, int minHeight, int maxHeight, String displayName, int minBranchLength, int maxBranchLength) {
            this.log = log;
            this.planks = planks;
            this.fence = fence;
            this.fenceGate = fenceGate;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.minBranchLength = minBranchLength;
        }

        public static WoodType getRandom() {
            WoodType[] values = values();
            return values[ThreadLocalRandom.current().nextInt(values.length)];
        }

        public Material getLeafMaterial() {
            switch (this) {
                case OAK: return Material.OAK_LEAVES;
                case SPRUCE: return Material.SPRUCE_LEAVES;
                case BIRCH: return Material.BIRCH_LEAVES;
                case JUNGLE: return Material.JUNGLE_LEAVES;
                case ACACIA: return Material.ACACIA_LEAVES;
                case DARK_OAK: return Material.DARK_OAK_LEAVES;
                case PALE_OAK: return Material.PALE_OAK_LEAVES; 
                case MANGROVE: return Material.MANGROVE_LEAVES;
                case CHERRY: return Material.CHERRY_LEAVES;
                case CRIMSON: return Material.NETHER_WART_BLOCK;
                case WARPED: return Material.WARPED_WART_BLOCK;
                default: return Material.OAK_LEAVES;
            }
        }
    }
