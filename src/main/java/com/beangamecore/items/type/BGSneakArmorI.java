package com.beangamecore.items.type;

import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public interface BGSneakArmorI {
    // Beangame Sneak Armor Item
    void onSneakArmor(PlayerToggleSneakEvent event, ItemStack stack);
}

