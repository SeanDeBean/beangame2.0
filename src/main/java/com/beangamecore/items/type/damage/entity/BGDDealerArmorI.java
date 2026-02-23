package com.beangamecore.items.type.damage.entity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDDealerArmorI {
    // Beangame Damage Dealer Armor Item
    void attackerOnHitArmor(EntityDamageByEntityEvent event, ItemStack armor);
}

