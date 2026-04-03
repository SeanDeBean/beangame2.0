package com.beangamecore.events;

import com.beangamecore.commands.PvpToggleCommand;
import com.beangamecore.items.SentientBeehive;
import com.beangamecore.items.WitherScepter;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.damage.entity.*;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

import java.util.UUID;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class EntityDamageByEntity implements Listener {

    public static void handleAttackerOnHit(ItemStack item, org.bukkit.event.entity.EntityDamageByEntityEvent event, boolean mainHand) {
        if (item == null) return;  // Ensure the item is not null
        BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> {
            if (mainHand) {
                if (!i.canInteractWithMainHand()) return;
            } else {
                if (!i.canInteractWithOffHand()) return;
            }
            if(event.getEntity() instanceof LivingEntity && event.getDamager() instanceof LivingEntity){
                i.doIf(BGDDealerHeldI.class, h -> h.attackerOnHit(event, item));
            }
        });
    }

    public static void handleAttackerInventoryOnHit(ItemStack item, org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (item == null) return;  // Ensure the item is not null
        BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> i.doIf(BGDDealerInvI.class, d -> d.attackerInventoryOnHit(event, item)));
    }

    public static void handleVictimOnHit(ItemStack item, org.bukkit.event.entity.EntityDamageByEntityEvent event, boolean mainHand) {
        if (item == null) return;  // Ensure the item is not null
        BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> {
            if (mainHand) {
                if (!i.canInteractWithMainHand()) return;
            } else {
                if (!i.canInteractWithOffHand()) return;
            }
            i.doIf(BGDReceiverHeldI.class, h -> h.victimOnHit(event, item));
        });
    }

    public static void handleVictimInventoryOnHit(ItemStack item, org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (item == null) return;  // Ensure the item is not null
        BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> i.doIf(BGDReceiverInvI.class, d -> d.victimInventoryOnHit(event, item)));
    }

    public static void handleVictimFinalInventoryOnHit(ItemStack item, org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (item == null) return;  // Ensure the item is not null
        BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> i.doIf(BGDReceiverFinalInvI.class, d -> d.victimFinalInventoryOnHit(event, item)));
    }

    @EventHandler
    private void onHit(EntityDamageByEntityEvent event) {

        SentientBeehive.resetStinger(event); // keeps the bees aggressive
        WitherScepter.resetEffect(event); // resets the wither duration

        if(event.getEntity() instanceof Item || event.getEntity() instanceof EnderCrystal) return;

        // Attacker logic
        if (event.getDamager() instanceof LivingEntity attacker) {
            EntityEquipment equipment = attacker.getEquipment();
            if (equipment != null) {
                ItemStack[] heldItems = new ItemStack[]{
                    equipment.getItemInMainHand(),
                    equipment.getItemInOffHand()
                };

                for (int i = 0; i < 2; i++) {
                    ItemStack stack = heldItems[i];
                    if (stack != null && ItemNBT.hasBeanGameTag(stack)) {
                        handleAttackerOnHit(stack, event, i == 0);
                    }
                }

                for (ItemStack armor : equipment.getArmorContents()) {
                    if (armor != null) {
                        BeangameItemRegistry.getFromItemStack(armor).ifPresent(item ->
                            item.doIf(BGDDealerArmorI.class, a -> a.attackerOnHitArmor(event, armor))
                        );
                    }
                }
            }

            if (attacker instanceof Player attackerPlayer) {
                // Apply movement item damage cooldown
                UUID auuid = attackerPlayer.getUniqueId();
                if (!event.isCancelled() && (PvpToggleCommand.pvp || !attackerPlayer.getWorld().getName().equals("lobby"))) Cooldowns.setCooldown("attack", auuid, 1500);

                PlayerInventory attackerInventory = attackerPlayer.getInventory();
                for (ItemStack item : attackerInventory.getContents()) {
                    if (item != null) {
                        handleAttackerInventoryOnHit(item, event);
                    }
                }
            }
        }

        // Victim logic
        if (event.getEntity() instanceof LivingEntity victim) {
            EntityEquipment equipment = victim.getEquipment();
            if (equipment != null) {
                ItemStack[] heldItems = new ItemStack[]{
                    equipment.getItemInMainHand(),
                    equipment.getItemInOffHand()
                };

                for (int i = 0; i < 2; i++) {
                    ItemStack stack = heldItems[i];
                    if (stack != null) {
                        handleVictimOnHit(stack, event, i == 0);
                    }
                }

                for (ItemStack armor : equipment.getArmorContents()) {
                    if (armor != null) {
                        BeangameItemRegistry.getFromItemStack(armor).ifPresent(item ->
                            item.doIf(BGDReceiverArmorI.class, a -> a.victimOnHitArmor(event, armor))
                        );
                    }
                }
            }

            if (victim instanceof Player victimPlayer) {
                PlayerInventory victimInventory = victimPlayer.getInventory();
                for (ItemStack item : victimInventory.getContents()) {
                    if (item != null) {
                        handleVictimInventoryOnHit(item, event);
                        handleVictimFinalInventoryOnHit(item, event);
                    }
                }
            }
        }
    }

    // @EventHandler
    // private void onHit(EntityDamageByEntityEvent event) {

    //     NewHelicopter.feedback(event);
    //     SentientBeehive.resetStinger(event); // keeps the bees aggressive
    //     WitherScepter.resetEffect(event); // resets the wither duration

    //     // Handle attacker logic for all LivingEntities
    //     if (event.getDamager() instanceof LivingEntity attacker) {
    //         ItemStack[] heldItems = new ItemStack[]{
    //             attacker.getEquipment().getItemInMainHand(), 
    //             attacker.getEquipment().getItemInOffHand()
    //         };
            
    //         for (int i = 0; i < 2; i++) {
    //             ItemStack stack = heldItems[i];
    //             if (stack != null && ItemNBT.hasBeanGameTag(stack)) {
    //                 handleAttackerOnHit(stack, event, i == 0);
    //             }
    //         }

    //         // Check full inventory if the attacker is a Player
    //         if (attacker instanceof Player attackerPlayer) {
    //             PlayerInventory attackerInventory = attackerPlayer.getInventory();
    //             for (ItemStack item : attackerInventory.getContents()) {
    //                 if (item != null) {
    //                     handleAttackerInventoryOnHit(item, event);
    //                 }
    //             }
    //         }

    //         // Armor checks
    //         for (ItemStack armor : attacker.getEquipment().getArmorContents()) {
    //             if (armor != null) {
    //                 BeangameItemRegistry.getFromItemStack(armor).ifPresent(item -> 
    //                     item.doIf(BGDDealerArmorI.class, a -> a.attackerOnHitArmor(event, armor))
    //                 );
    //             }
    //         }
    //     }

    //     // Handle victim logic for all LivingEntities
    //     if (event.getEntity() instanceof LivingEntity victim) {
    //         ItemStack[] heldItems = new ItemStack[]{
    //             victim.getEquipment().getItemInMainHand(), 
    //             victim.getEquipment().getItemInOffHand()
    //         };

    //         for (int i = 0; i < 2; i++) {
    //             ItemStack stack = heldItems[i];
    //             if (stack != null) {
    //                 handleVictimOnHit(stack, event, i == 0);
    //             }
    //         }

    //         // Check full inventory if the victim is a Player
    //         if (victim instanceof Player victimPlayer) {
    //             PlayerInventory victimInventory = victimPlayer.getInventory();

    //             for (ItemStack item : victimInventory.getContents()) {
    //                 if (item != null) {
    //                     handleVictimInventoryOnHit(item, event);
    //                 }
    //             }

    //             // Final inventory checks
    //             for (ItemStack item : victimInventory.getContents()) {
    //                 if (item != null) {
    //                     handleVictimFinalInventoryOnHit(item, event);
    //                 }
    //             }
    //         }

    //         // Armor checks
    //         for (ItemStack armor : victim.getEquipment().getArmorContents()) {
    //             if (armor != null) {
    //                 BeangameItemRegistry.getFromItemStack(armor).ifPresent(item -> 
    //                     item.doIf(BGDReceiverArmorI.class, a -> a.victimOnHitArmor(event, armor))
    //                 );
    //             }
    //         }
    //     }
    // }

}
