package com.beangamecore.events;

import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGFlightArmorI;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.beangamecore.util.Cooldowns;
import org.bukkit.inventory.ItemStack;

public class PlayerToggleFlight implements Listener{
    @EventHandler
    private void onPlayerFlightToggle(org.bukkit.event.player.PlayerToggleFlightEvent event){
        Player player = event.getPlayer();
        GameMode gamemode = player.getGameMode();
        if(gamemode != GameMode.SPECTATOR && gamemode != GameMode.CREATIVE){
            event.setCancelled(true);
            player.setFlying(false);
            player.setAllowFlight(false);
            for(ItemStack armor : player.getEquipment().getArmorContents()){
                BeangameItemRegistry.getFromItemStack(armor).ifPresent(item -> {
                    boolean cd = item.getIf(BGMobilityI.class, Cooldowns.onCooldown("attack", player.getUniqueId()), (i) -> {
                        Cooldowns.sendPVPCooldownMessage("attack", player);
                        event.setCancelled(true);
                        return true;
                    });
                    item.doIf(BGFlightArmorI.class, !cd, f -> f.onToggleFlightArmor(event, armor));
                });
            }
        }
    }
}

