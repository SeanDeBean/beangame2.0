package com.beangamecore.items.type.damage.entity;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDReceiverArmorI {
    // Beangame Damage Receiver Armor Item
    void victimOnHitArmor(EntityDamageByEntityEvent event, ItemStack armor);
}

