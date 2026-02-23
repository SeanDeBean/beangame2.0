package com.beangamecore.items.type.death;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BGGlobalDeath {
    // Beangame Global Death
    void onGlobalDeath(Player player, ItemStack item);
}

