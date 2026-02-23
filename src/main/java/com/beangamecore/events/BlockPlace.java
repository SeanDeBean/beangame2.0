package com.beangamecore.events;

import com.beangamecore.blocks.type.BGPlaceableB;
import com.beangamecore.registry.BeangameBlockData;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.items.type.BGPlaceableI;
import com.beangamecore.items.type.talisman.BGBlockPlaceTalismanI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BlockPlace implements Listener{
    private static void handleItemPlace(ItemStack item, org.bukkit.event.block.BlockPlaceEvent event) {
        BeangameItemRegistry.get(ItemNBT.getBeanGame(item)).ifPresent(bgitem -> {
            boolean cancel = bgitem.getIf(BGMobilityI.class, Cooldowns.onCooldown("attack", event.getPlayer().getUniqueId()), i -> {
                Cooldowns.sendPVPCooldownMessage("attack", event.getPlayer());
                return true;
            });
            if(cancel){
                event.setCancelled(true);
                return;
            }
            bgitem.doIf(BGPlaceableI.class, b -> b.onBlockPlace(event, item));
            bgitem.doIf(BGPlaceableB.class, b -> {
                b.onBlockPlace(event, item);
                if(!event.isCancelled()) BeangameBlockData.addBeangameBlock(b, event.getBlockPlaced());
            });
            if(!bgitem.is(BGPlaceableI.class) && !bgitem.is(BGPlaceableB.class)) event.setCancelled(true);
        });
    }

    @EventHandler
    private void onPlace(org.bukkit.event.block.BlockPlaceEvent event){
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        for(ItemStack playerInventoryItem : inventory.getContents()){
            if (playerInventoryItem != null && playerInventoryItem.getType() != Material.AIR){
                if(!ItemNBT.hasBeanGameTag(playerInventoryItem)){
                    continue;
                }
                BeangameItem invitem = BeangameItemRegistry.getFromItemStackRaw(playerInventoryItem);
                if(invitem instanceof BGBlockPlaceTalismanI t){
                    boolean stop = t.onPlace(event, playerInventoryItem);
                    if(stop) break;
                }
            }
        }

        // defines the held item as "item"
        ItemStack item = event.getPlayer().getEquipment().getItem(event.getHand());
        // checks if item has a bean game nbt tag
        if(!(ItemNBT.hasBeanGameTag(item))){
            return;
        }
        
        // item checks
        handleItemPlace(item, event);
        

        //next

    }
}

