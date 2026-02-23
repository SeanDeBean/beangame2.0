package com.beangamecore.events;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.Main;
import com.beangamecore.items.type.BGCastableI;
import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerFish implements Listener {

    @EventHandler
    private void onFish(PlayerFishEvent event){

        Player player = event.getPlayer();
        ItemStack[] heldItems = new ItemStack[]{player.getEquipment().getItemInMainHand(), player.getEquipment().getItemInOffHand()};

        for(ItemStack item : heldItems){
            BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> {
                boolean cd = i.getIf(BGMobilityI.class, Cooldowns.onCooldown("attack", player.getUniqueId()), c -> {
                   Cooldowns.sendPVPCooldownMessage("attack", player);
                   return true;
                });
                i.doIf(BGCastableI.class, !cd, f -> f.onFish(event));
            });
        }

        if(event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Main.getPlugin().getFishingManager().handlePlayerFishEvent(event);

    }
}

