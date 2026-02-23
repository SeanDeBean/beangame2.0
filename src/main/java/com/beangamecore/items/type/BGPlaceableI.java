package com.beangamecore.items.type;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public interface BGPlaceableI {
    // Beangame Placeable Item
    void onBlockPlace(BlockPlaceEvent event, ItemStack stack);
}

