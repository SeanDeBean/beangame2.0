package com.beangamecore.events;

import com.beangamecore.Main;
import com.beangamecore.items.WalkieTalkie;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChat implements Listener {
    @EventHandler
    void onChat(AsyncPlayerChatEvent event){
        if(WalkieTalkie.frequencyChat.contains(event.getPlayer())){
            event.setCancelled(true);
            float frequency = 0;
            try{
                frequency = Float.parseFloat(event.getMessage());
            } catch (Exception ignored){}
            final float finalFreq = frequency;
            Bukkit.getScheduler().runTask(Main.getPlugin(), () -> BeangameItemRegistry.getRaw(Key.bg("walkietalkie"), WalkieTalkie.class).setTempFreq(event.getPlayer(), finalFreq));
            WalkieTalkie.frequencyChat.remove(event.getPlayer());
        }
    }
}

