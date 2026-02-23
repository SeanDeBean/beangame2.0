package com.beangamecore.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.beangamecore.items.WalkieTalkie;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;

public class PlayerChangeSign implements Listener {
    @EventHandler
    private void onPlayerChangeSign(SignChangeEvent event){
        BeangameItemRegistry.get(Key.bg("walkietalkie"), WalkieTalkie.class).ifPresent(walkieTalkie -> walkieTalkie.onSignChange(event));
    }
}

