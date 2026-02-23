package com.beangamecore.items.type.death;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDeathHeldI {
    // Beangame Death Held Item
    void onDeathHeldItem(EntityDeathEvent event, ItemStack item);
}

