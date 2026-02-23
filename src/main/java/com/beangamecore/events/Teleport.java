package com.beangamecore.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.type.BGTeleportInvI;
import com.beangamecore.registry.BeangameItemRegistry;

public class Teleport implements Listener {
    @EventHandler
    private void onTeleport(org.bukkit.event.player.PlayerTeleportEvent event){
        // player held item based
        Player player = event.getPlayer();
        Inventory inventory = player.getInventory();

        //inventory checks
        for(ItemStack inventoryItem : inventory.getContents()){
            BeangameItemRegistry.getFromItemStack(inventoryItem).ifPresent(i -> i.doIf(BGTeleportInvI.class, s -> s.onTeleport(event, i)));
        }

    }
}

