package com.beangamecore.items.type;

import com.beangamecore.items.generic.BeangameItem;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public interface BGSneakInvI {
    // Beangame Sneak Inventory Item
    void onToggleInventoryItemSneak(PlayerToggleSneakEvent event, BeangameItem item);
}

