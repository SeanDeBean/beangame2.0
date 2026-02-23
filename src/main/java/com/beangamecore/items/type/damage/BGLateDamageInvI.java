package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public interface BGLateDamageInvI {
    // Beangame Late Damage Inventory Item
    boolean onLateDamageInventory(EntityDamageEvent event, ItemStack item);
}

