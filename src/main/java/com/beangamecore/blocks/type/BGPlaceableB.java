package com.beangamecore.blocks.type;

import com.beangamecore.blocks.generic.BeangameBlock;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public interface BGPlaceableB extends BeangameBlock {
    // Beangame Placeable Block
    void onBlockPlace(BlockPlaceEvent event, ItemStack item);
}

