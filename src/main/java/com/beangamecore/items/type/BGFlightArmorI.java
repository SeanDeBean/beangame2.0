package com.beangamecore.items.type;

import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

public interface BGFlightArmorI {
    // Beangame Flight Armor Item
    void onToggleFlightArmor(PlayerToggleFlightEvent event, ItemStack item);
}

