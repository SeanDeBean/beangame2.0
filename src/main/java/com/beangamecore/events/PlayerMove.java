package com.beangamecore.events;

import java.util.UUID;

import com.beangamecore.blocks.type.BGWalkableB;
import com.beangamecore.items.type.move.BGMoveArmorI;
import com.beangamecore.registry.BeangameBlockData;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerMove implements Listener{
    @EventHandler
    private void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Block floor = player.getLocation().add(0, -1, 0).getBlock();
        BeangameBlockData.getBeangameBlock(floor).ifPresent(b -> {
            if(b instanceof BGWalkableB walkable) walkable.onMoveToBlock(event, floor);
        });
        if (Cooldowns.onCooldown("immobilized", uuid)){
            long immobilizedtimerremaining = Cooldowns.getRemainingCooldown("immobilized", uuid) / 1000L;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§bImmobilized for " + immobilizedtimerremaining + " second(s)!"));
            event.setCancelled(true);
            return;
        }
        for(ItemStack armor : player.getEquipment().getArmorContents()){
            BeangameItemRegistry.getFromItemStack(armor).ifPresent(item -> item.doIf(BGMoveArmorI.class, a -> a.onMoveArmor(event, armor)));
        }
        

    }

}

