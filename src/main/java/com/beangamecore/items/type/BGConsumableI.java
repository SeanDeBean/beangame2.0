package com.beangamecore.items.type;

import org.bukkit.event.player.PlayerItemConsumeEvent;

public interface BGConsumableI {
    // Beangame Consumable Item
    void onConsume(PlayerItemConsumeEvent event);
}

