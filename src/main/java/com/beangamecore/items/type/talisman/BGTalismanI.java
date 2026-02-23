package com.beangamecore.items.type.talisman;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BGTalismanI {
    // Beangame Talisman Item (Don't use directly, use BGLPTalismanI, BGMPTalismanI, or BGHPTalismanI)
    void applyTalismanEffects(Player player, ItemStack item);
}

