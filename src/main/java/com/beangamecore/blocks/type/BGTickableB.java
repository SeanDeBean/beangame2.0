package com.beangamecore.blocks.type;

import com.beangamecore.blocks.generic.BeangameBlock;
import org.bukkit.block.Block;

public interface BGTickableB extends BeangameBlock {
    // Beangame Tickable Block (dont use this directly, use BGLPTickableB, BGMPTickableB, or BGHPTickableB)
    void tick(Block block);
}

