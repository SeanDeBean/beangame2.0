package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;

public interface BGDImmuneArmorI {
    // Beangame Damage Immune Armor Item
    boolean isImmuneArmorItem(EntityDamageEvent.DamageCause cause);
}

