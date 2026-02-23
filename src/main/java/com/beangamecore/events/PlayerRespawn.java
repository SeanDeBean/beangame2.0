package com.beangamecore.events;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Booleans;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.beangamecore.Main;
import com.beangamecore.commands.DeathSpectateCommand;
import com.beangamecore.items.CrownOfTheCosmos;
import com.beangamecore.items.PortalMaker;

public class PlayerRespawn implements Listener{

    @EventHandler
    private void onRespawn(org.bukkit.event.player.PlayerRespawnEvent event){
        Player player = event.getPlayer();
        player.setInvisible(false);
        player.setAllowFlight(false);

        resetBeangameItemsOnRespawn(player);

        if(player.getGameMode() == GameMode.SPECTATOR){
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        if(player.getGameMode() == GameMode.CREATIVE){
            player.setAllowFlight(true);
        }
        if(DeathSpectateCommand.deathspectate){
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                player.setGameMode(GameMode.SPECTATOR);
                player.setFlying(true);
            }, 1L);
        }

        if(player.getWorld().getName() == "lobby"){
            player.setGameMode(GameMode.SPECTATOR);
            player.getEquipment().setBoots(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:carrotslides")).asItem());
        }
    }

    private void resetBeangameItemsOnRespawn(Player player){
        
        Booleans.setBoolean("gracefulwaders_active", player.getUniqueId(), false);
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:crownofthecosmos"), CrownOfTheCosmos.class).releaseStars(player);
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:portalmaker"), PortalMaker.class).portalmakerReset(player);

    }
}

