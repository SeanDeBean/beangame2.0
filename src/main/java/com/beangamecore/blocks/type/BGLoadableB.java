package com.beangamecore.blocks.type;

import org.bukkit.block.Block;

public interface BGLoadableB {
    // Beangame Loadable Block
    void onLoad(Block block);
    void onUnload(Block block);
}

