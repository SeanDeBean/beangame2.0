package com.beangamecore.items.type.talisman;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public interface BGBlockPlaceTalismanI {
    boolean onPlace(BlockPlaceEvent event, ItemStack item);
}

