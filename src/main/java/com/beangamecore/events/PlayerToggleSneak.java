package com.beangamecore.events;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGSneakArmorI;
import com.beangamecore.items.type.BGSneakHeldI;
import com.beangamecore.items.type.BGSneakInvI;
import com.beangamecore.util.Booleans;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.beangamecore.items.PortalMaker;

public class PlayerToggleSneak implements Listener{
    @EventHandler
    private void onCrouch(org.bukkit.event.player.PlayerToggleSneakEvent event){
        // player held item based
        Player player = event.getPlayer();
        ItemStack[] heldItems = new ItemStack[]{player.getEquipment().getItemInMainHand(), player.getEquipment().getItemInOffHand()};
        PlayerInventory inventory = player.getInventory();
        for(ItemStack item : heldItems){
            BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> i.doIf(BGSneakHeldI.class, s -> s.onToggleHeldItemSneak(event, item)));
        }

        //inventory checks
        for(ItemStack inventoryItem : inventory.getContents()){
            BeangameItemRegistry.getFromItemStack(inventoryItem).ifPresent(i -> i.doIf(BGSneakInvI.class, s -> s.onToggleInventoryItemSneak(event, i)));
        }
            
        // armor checks
        for(ItemStack stack : inventory.getArmorContents()){
            BeangameItemRegistry.getFromItemStack(stack).ifPresent(i -> i.doIf(BGSneakArmorI.class, s -> s.onSneakArmor(event, stack)));
        }

        // cloak of the spy uncrouch to prevent perfect
        if(Booleans.getBoolean("cloakofthespy_active", player.getUniqueId()) && player.isSneaking()){
            player.setInvisible(false);
            Booleans.setBoolean("cloakofthespy_active", player.getUniqueId(), false);
        }

        // checks if player is on a portal block
        for(ArmorStand armorStand : player.getWorld().getEntitiesByClass(ArmorStand.class)){
            if(player.getLocation().distance(armorStand.getLocation()) <= 0.5){
                BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:portalmaker"), PortalMaker.class).portalmakerTeleport(event, armorStand);
            }
        }
    }
}

