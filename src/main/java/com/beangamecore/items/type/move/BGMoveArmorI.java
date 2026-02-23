package com.beangamecore.items.type.move;

import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public interface BGMoveArmorI {
    // Beangame On Move Armor item
    void onMoveArmor(PlayerMoveEvent event, ItemStack armor);
}

