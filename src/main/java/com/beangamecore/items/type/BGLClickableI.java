package com.beangamecore.items.type;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface BGLClickableI {
    // Beangame Left Clickable Item
    void onLeftClick(PlayerInteractEvent event, ItemStack stack);
}

