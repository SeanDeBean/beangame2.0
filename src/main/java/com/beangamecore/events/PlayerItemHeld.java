package com.beangamecore.events;

import java.util.UUID;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.beangamecore.items.SlotEnforcer;

public class PlayerItemHeld implements Listener{
    
    @EventHandler
    private void onSlotChange(org.bukkit.event.player.PlayerItemHeldEvent event){
        UUID uuid = event.getPlayer().getUniqueId();

        // slot enforcer
        if(Cooldowns.onCooldown("slot_enforced", uuid)){
            BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:slotenforcer"), SlotEnforcer.class).slotenforcerSlotChange(event);
        }
    }
}

