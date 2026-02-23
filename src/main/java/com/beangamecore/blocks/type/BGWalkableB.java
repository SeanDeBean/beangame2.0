package com.beangamecore.blocks.type;

import com.beangamecore.blocks.generic.BeangameBlock;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerMoveEvent;

public interface BGWalkableB extends BeangameBlock {
    // Beangame Walkable Block
    void onMoveToBlock(PlayerMoveEvent event, Block block);
}

