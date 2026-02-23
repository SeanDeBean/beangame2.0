package com.beangamecore.items.type;

import org.bukkit.event.player.PlayerTeleportEvent;

import com.beangamecore.items.generic.BeangameItem;

public interface BGTeleportInvI {
    // Beangame Teleport Inventory Item
    void onTeleport(PlayerTeleportEvent event, BeangameItem item);
}

