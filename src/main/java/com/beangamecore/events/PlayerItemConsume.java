package com.beangamecore.events;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.generic.BeangameFish;
import com.beangamecore.items.type.BGConsumableI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PlayerItemConsume implements Listener{
    @EventHandler
    private void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent event){
        ItemStack item = event.getItem();
        BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> {
            if(!i.getIf(BGConsumableI.class, c -> {
                c.onConsume(event);
                return true;
            })) {
                if(!(i instanceof BeangameFish)){
                    event.setCancelled(true);
                }
            }
        });
    }
}

