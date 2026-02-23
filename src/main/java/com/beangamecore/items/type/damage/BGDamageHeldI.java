package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDamageHeldI {
    // Beangame Damage Held Item
    void onDamageHeldItem(EntityDamageEvent event, ItemStack item);
}

