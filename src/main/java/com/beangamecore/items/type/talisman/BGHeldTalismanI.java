package com.beangamecore.items.type.talisman;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BGHeldTalismanI {
    void applyHeldTalismanEffects(Player player, ItemStack item);
    default void resetNonHoldingPlayers(Set<UUID> currentlyHoldingPlayers){
        // Default empty implementation - override in classes that need tracking
    }
}

