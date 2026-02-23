package com.beangamecore.items.type;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BGArmorI {
    // Beangame Armor Item (every 3 seconds)
    void applyArmorEffects(Player player, ItemStack item);
}

