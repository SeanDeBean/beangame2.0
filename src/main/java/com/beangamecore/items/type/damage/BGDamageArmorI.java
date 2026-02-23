package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDamageArmorI {
    // Beangame Damage Armor Item
    void onDamageArmor(EntityDamageEvent event, ItemStack armor);
}

