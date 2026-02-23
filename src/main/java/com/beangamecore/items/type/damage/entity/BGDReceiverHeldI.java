package com.beangamecore.items.type.damage.entity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDReceiverHeldI {
    // Beangame Damage Receiver Held Item
    void victimOnHit(EntityDamageByEntityEvent event, ItemStack item);
}

