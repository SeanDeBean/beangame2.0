package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;

public interface BGCancelFallDmgHeldI extends BGDImmuneHeldI {
    // Beangame Cancel Fall Damage Held Item
    @Override
    default boolean isImmuneHeldItem(EntityDamageEvent.DamageCause cause) {
        return cause.equals(EntityDamageEvent.DamageCause.FALL);
    }
}

