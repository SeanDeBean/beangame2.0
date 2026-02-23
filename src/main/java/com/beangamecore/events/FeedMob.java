package com.beangamecore.events;

import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.util.ItemNBT;

public class FeedMob implements Listener {

    @EventHandler
    public static void onFeedMob(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Animals){
            Player player = event.getPlayer();
            if (player == null) return;

            ItemStack[] heldItems = new ItemStack[] {
                player.getEquipment().getItemInMainHand(),
                player.getEquipment().getItemInOffHand()
            };

            for (int i = 0; i < 2; i++) {
                ItemStack item = heldItems[i];
                if (item == null || item.getType() == Material.AIR || !ItemNBT.hasBeanGameTag(item)) continue;
                event.setCancelled(true);
                return;
            }
        }
    }

    
}

