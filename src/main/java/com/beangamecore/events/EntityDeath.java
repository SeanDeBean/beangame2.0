package com.beangamecore.events;

import com.beangamecore.Main;
import com.beangamecore.items.Drownmet;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.death.BGDeathArmorI;
import com.beangamecore.items.type.death.BGDeathHeldI;
import com.beangamecore.items.type.death.BGDeathInvI;
import com.beangamecore.items.type.death.BGGlobalDeath;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.beangamecore.util.ItemNBT;

public class EntityDeath implements Listener{
    
    @EventHandler
    private void onRemove(EntityRemoveEvent event){
        Main.getPlugin().getSeaCreatureRegistry().handleEntityRemove(event.getEntity());
    }

    @EventHandler
    private void onDeath(EntityDeathEvent event) {
        
        LivingEntity entity = event.getEntity();

        // Armor checks
        for (ItemStack armor : entity.getEquipment().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                BeangameItemRegistry.getFromItemStack(armor).ifPresent(item -> 
                    item.doIf(BGDeathArmorI.class, a -> a.onDeathArmor(event, armor))
                );
            }
        }

        // Held item checks (Main hand and Off hand)
        ItemStack[] heldItems = new ItemStack[]{entity.getEquipment().getItemInMainHand(), entity.getEquipment().getItemInOffHand()};
        for (ItemStack stack : heldItems) {
            if (stack != null && stack.getType() != Material.AIR) {
                if (ItemNBT.hasBeanGameTag(stack)) {
                    BeangameItem item = BeangameItemRegistry.getFromItemStackRaw(stack);
                    if (item instanceof BGDeathHeldI deathItem) {
                        deathItem.onDeathHeldItem(event, stack);
                    }
                }
            }
        }

        // Player-specific checks
        if (entity instanceof Player player) {
            // Inventory checks for the player
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null) {
                    BeangameItemRegistry.getFromItemStack(invItem).ifPresent(item -> 
                        item.doIf(BGDeathInvI.class, i -> i.onDeathInventory(event, invItem))
                    );
                }
            }

            // Global inventory checks for all players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerInventory inventory = onlinePlayer.getInventory();
                for (ItemStack onlinePlayerItem : inventory.getContents()) {
                    if (onlinePlayerItem != null) {
                        BeangameItemRegistry.getFromItemStack(onlinePlayerItem).ifPresent(item -> 
                            item.doIf(BGGlobalDeath.class, g -> g.onGlobalDeath(onlinePlayer, onlinePlayerItem))
                        );
                    }
                }
            }
        }

        // Specific checks for special entities (e.g., Drowned)
        if (entity instanceof Drowned drowned) {
            Drownmet.onDrownedDeath(event, drowned);
        }


        // soul drops for smbean
        if(!(entity instanceof Player)){
            if(Math.random() < 0.005){
                entity.getWorld().dropItemNaturally(entity.getLocation(), BeangameItemRegistry.get(NamespacedKey.fromString("beangame:soul")).get().asItem());
            }
        }

        Main.getPlugin().getSeaCreatureRegistry().handleEntityDeath(entity);



        // Add more specific checks as needed for other entity types
    }

}

