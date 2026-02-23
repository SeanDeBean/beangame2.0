package com.beangamecore.items.type.death;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDeathInvI {
    // Beangame Death Inventory Item
    void onDeathInventory(EntityDeathEvent event, ItemStack item);
}

