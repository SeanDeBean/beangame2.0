package com.beangamecore.events;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.beangamecore.Main;

public class DropItem implements Listener{
    
    @EventHandler
    private void onDropItem(PlayerDropItemEvent event){
        if(Main.getPlugin().getBeangameModes().getGameMode("randomizer").isEnabled()){
            Item item = event.getItemDrop();
            item.setTicksLived(5800);
        }
    }

}

