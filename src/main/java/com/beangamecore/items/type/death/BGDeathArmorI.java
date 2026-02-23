package com.beangamecore.items.type.death;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public interface BGDeathArmorI {
    // Beangame Death Armor Item
    void onDeathArmor(EntityDeathEvent event, ItemStack armor);
}

