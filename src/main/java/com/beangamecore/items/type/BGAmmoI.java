package com.beangamecore.items.type;

import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

public interface BGAmmoI {
    // Beangame Ammo Item
    void onShootBow(EntityShootBowEvent event, ItemStack bow);
}

