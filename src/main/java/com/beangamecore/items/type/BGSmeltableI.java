package com.beangamecore.items.type;

import org.bukkit.event.inventory.FurnaceSmeltEvent;

public interface BGSmeltableI {
    // Beangame Smeltable Item
    void onSmelt(FurnaceSmeltEvent event);
}

