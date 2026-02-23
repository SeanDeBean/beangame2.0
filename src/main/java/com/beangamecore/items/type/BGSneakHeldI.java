package com.beangamecore.items.type;

import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public interface BGSneakHeldI {
    // Beangame Sneak Held Item
    void onToggleHeldItemSneak(PlayerToggleSneakEvent event, ItemStack item);
}

