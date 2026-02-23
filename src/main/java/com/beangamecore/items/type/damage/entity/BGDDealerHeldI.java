package com.beangamecore.items.type.damage.entity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDDealerHeldI {
    // Beangame Damage Dealer Held Item
    void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item);
}

