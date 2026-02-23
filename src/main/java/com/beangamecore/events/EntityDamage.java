package com.beangamecore.events;

import java.util.concurrent.atomic.AtomicBoolean;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.damage.*;
import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.Longs;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.beangamecore.commands.BeangameStart;
import com.beangamecore.commands.PvpToggleCommand;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class EntityDamage implements Listener {

    @EventHandler
    private void onDamage(EntityDamageEvent event) {

        if (event.getEntity() instanceof LivingEntity) {
            // Generalized for LivingEntities, including Players and Mobs
            LivingEntity victimEntity = (LivingEntity) event.getEntity();
            PlayerInventory victimInventory = null;

            if (victimEntity instanceof Player player) {
                victimInventory = player.getInventory();

                if(player.getWorld().getName().equals("lobby")) {
                    event.setCancelled(true);
                    return;
                }

                // PVP Toggle: Prevent damage if PVP is off (specific to Players)
                if (!PvpToggleCommand.pvp && player.getWorld().getName().equals("beangame-world")) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§fDamage immune!"));
                    event.setCancelled(true);
                    return;
                }
            }

            if(!event.isCancelled() && event.getFinalDamage() > 0 && BeangameStart.gamerunning && victimEntity instanceof Player) {
                double damageMultiplier = 0.5 + (BeangameStart.percent * 0.5);

                event.setDamage(event.getDamage() * damageMultiplier);
            }

            // Loop over inventory items and handle item-specific logic
            if(victimEntity instanceof Player){
                for (ItemStack victimInventoryItem : victimInventory.getContents()) {
                    if (victimInventoryItem == null) continue;  // Null check for inventory items
                    BeangameItemRegistry.getFromItemStack(victimInventoryItem).ifPresent(item -> {
                        item.doIf(BGInvUnstackable.class, u -> {
                            if (u.alreadyActivated(victimEntity.getUniqueId(), item)) {
                                u.reset(victimEntity.getUniqueId(), item);
                            }
                        });
                    });
                }
            }

            // Handle held items (for both Player and non-Player LivingEntities)
            ItemStack[] heldItems = new ItemStack[]{victimEntity.getEquipment().getItemInMainHand(), victimEntity.getEquipment().getItemInOffHand()};

            for (ItemStack vItem : heldItems) {
                if (vItem == null) continue;  // Null check for held items

                BeangameItemRegistry.getFromItemStack(vItem).ifPresent(item -> {
                    item.doIf(BGDImmuneHeldI.class, i -> {
                        if (i.isImmuneHeldItem(event.getCause())) {
                            event.setCancelled(true);
                        }
                    });
                    item.doIf(BGDamageHeldI.class, i -> i.onDamageHeldItem(event, vItem));
                });
            }


            // Handle inventory items and armor for damage immunity and item-specific effects
            if(victimEntity instanceof Player vPlayer){
                for (ItemStack vItem : vPlayer.getInventory().getContents()) {
                    if (vItem == null) continue;
                    
                    BeangameItemRegistry.getFromItemStack(vItem).ifPresent(item -> {
                        // Check if this is an unstackable item that has already been processed
                        if (item instanceof BGInvUnstackable unstackableItem) {
                            
                            // Check if this item type has already been activated for this player in this event
                            if (unstackableItem.alreadyActivated(vPlayer.getUniqueId(), item)) {
                                return; // Skip this item, already processed
                            }
                            
                            // Mark this item type as activated
                            unstackableItem.activate(vPlayer.getUniqueId(), item);
                        }
                        
                        // Process damage reduction
                        item.doIf(BGDamageInvI.class, i -> i.onDamageInventory(event, vItem));
                    });
                }
            }

            // Handle armor items for immunity and effects
            for (ItemStack armor : victimEntity.getEquipment().getArmorContents()) {
                if (armor == null) continue;  // Null check for armor items

                BeangameItemRegistry.getFromItemStack(armor).ifPresent(item -> {
                    item.doIf(BGDImmuneArmorI.class, i -> {
                        if (i.isImmuneArmorItem(event.getCause())) {
                            event.setCancelled(true);
                        }
                    });
                    item.doIf(BGDamageArmorI.class, i -> i.onDamageArmor(event, armor));
                });
            }

            // Fall damage immunity cooldown check
            if (Cooldowns.onCooldown("fall_damage_immunity", victimEntity.getUniqueId())
                    && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);
            }

            if (Cooldowns.onCooldown("explosion_immunity", victimEntity.getUniqueId())
                    && (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) || event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION))) {
                event.setCancelled(true);
            }

            if (Cooldowns.onCooldown("suffocation_immunity", victimEntity.getUniqueId()) && event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
                event.setCancelled(true);
            } else if (event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION) && !event.isCancelled()) {
                Long currentStacks = Longs.getLong("suffocation_stacks", victimEntity.getUniqueId());
                Long newStacks = currentStacks + 1;
                Longs.setLong("suffocation_stacks", victimEntity.getUniqueId(), newStacks);
                Cooldowns.setCooldown("suffocation_immunity", victimEntity.getUniqueId(), Math.max(0, 2000L - 50 * newStacks));  // 2 seconds base cooldown, reduced by 50ms per stack
                event.setDamage(event.getDamage() * (1 + 0.2 * Math.max(0, currentStacks - 4)));  // Increase damage by 20% per stack after 4 stacks
            }

            // Run late damage logic for items that have specific behavior
            if (victimInventory != null) {  // Only run for players (non-null inventory)
                AtomicBoolean revive = new AtomicBoolean(false);
                ItemStack[] contents = victimInventory.getContents();

                for (int i = 0; i < contents.length; i++) {
                    if (revive.get()) break;

                    ItemStack victimInventoryItem = contents[i];
                    if (victimInventoryItem == null) continue;

                    BeangameItemRegistry.getFromItemStack(victimInventoryItem).ifPresent(item -> {
                        item.doIf(BGLateDamageInvI.class, lateDamageItem -> {
                            // Avoid re-activation of unstackable items when already activated
                            item.doIf(BGInvUnstackable.class, i2 -> {
                                if (!i2.alreadyActivated(victimEntity.getUniqueId(), item)) {
                                    i2.activate(victimEntity.getUniqueId(), item);
                                }
                            });

                            boolean didRevive = lateDamageItem.onLateDamageInventory(event, victimInventoryItem);
                            if (didRevive) {
                                revive.set(true);
                            }
                        });
                    });
                }
            }
        }
    }

}
