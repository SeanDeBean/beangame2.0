package com.beangamecore.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.NotePlayEvent;

import com.beangamecore.items.CatKeyboard;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;

public class NoteBlockPlay implements Listener {
    
    @EventHandler
    public void onNoteBlockPlay(NotePlayEvent event){
        BeangameItemRegistry.getRaw(Key.bg("catkeyboard"), CatKeyboard.class).onNoteBlockPlay(event);
    }

}

