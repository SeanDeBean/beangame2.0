package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;

public interface BGDImmuneHeldI {
    // Beangame Damage Immune Held Item
    boolean isImmuneHeldItem(EntityDamageEvent.DamageCause cause);
}

