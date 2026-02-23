package com.beangamecore.items.type;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public interface BGToolI {
    // Beangame Tool Item
    void onBlockBreak(BlockBreakEvent event, ItemStack item);
}

