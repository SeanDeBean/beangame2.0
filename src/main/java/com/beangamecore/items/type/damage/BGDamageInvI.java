package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDamageInvI {
    // Beangame Damage Inventory Item
    void onDamageInventory(EntityDamageEvent event, ItemStack item);
}

