package com.beangamecore.items.type.damage.entity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDReceiverFinalInvI {
    // Beangame Damage Receiver Final Inventory Item
    void victimFinalInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item);
}

