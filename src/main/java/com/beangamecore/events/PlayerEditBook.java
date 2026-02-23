package com.beangamecore.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

public class PlayerEditBook implements Listener {
    @EventHandler
    private void onPlayerEditBook(PlayerEditBookEvent event){

        BookMeta bookMeta = event.getNewBookMeta();

        if(bookMeta == null || !bookMeta.hasPages()){
            return;
        }
    }
}

