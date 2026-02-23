package com.beangamecore.items.type.damage;

import org.bukkit.event.entity.EntityDamageEvent;

public interface BGCancelFallDmgArmorI extends BGDImmuneArmorI {
    // Beangame Cancel Fall Damage Armor Item
    default boolean isImmuneArmorItem(EntityDamageEvent.DamageCause cause){
        return cause.equals(EntityDamageEvent.DamageCause.FALL);
    }
}

