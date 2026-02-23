package com.beangamecore.entities.livingtree;

import org.bukkit.Material;

public class BlockPosition {
    public final int x, y, z;
    public final Material material;

    public BlockPosition(int x, int y, int z, Material material) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
    }
}
