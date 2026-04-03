package com.beangamecore.items.type;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface BGRClickableI {
    // Beangame Right Clickable Item
    boolean onRightClick(PlayerInteractEvent event, ItemStack stack);
    default void onRightClickWithAnimation(PlayerInteractEvent event, ItemStack stack, boolean mainHand, boolean animation){
        if(onRightClick(event, stack) && animation){
            if(mainHand) event.getPlayer().swingMainHand();
            else event.getPlayer().swingOffHand();
        }
    }
}

