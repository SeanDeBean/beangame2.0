package com.beangamecore.items.type.damage.entity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDDealerInvI {
    // Beangame Damage Dealer Inventory Item
    void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item);
}

