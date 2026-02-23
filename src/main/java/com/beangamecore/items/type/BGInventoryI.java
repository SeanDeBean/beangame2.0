package com.beangamecore.items.type;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface BGInventoryI {
    // Beangame Inventory Item
    void onInventoryClick(InventoryClickEvent event);
    String getInventoryName();
}

