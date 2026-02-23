package com.beangamecore.items.type.target;

import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface BGTargetArmor {
    void onTargetArmor(EntityTargetLivingEntityEvent event, ItemStack armor);
}

