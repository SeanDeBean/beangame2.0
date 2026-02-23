package com.beangamecore.items.type.damage.entity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDReceiverInvI {
    // Beangame Damage Receiver Inventory Item
    void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item);
}

