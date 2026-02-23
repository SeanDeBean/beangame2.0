package com.beangamecore.events;

import java.util.UUID;

import com.beangamecore.items.CrownOfTheCosmos;
import com.beangamecore.items.VampireFang;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.util.ItemNBT;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerInteract implements Listener{

    public static void handleItemRightClick(ItemStack item, org.bukkit.event.player.PlayerInteractEvent event, Player player, boolean mainHand){
        BeangameItemRegistry.getFromItemStack(item).ifPresent(bgitem -> bgitem.doIf(BGRClickableI.class, r -> {
            if(mainHand){
                if(!bgitem.canInteractWithMainHand()) return;
            } else {
                if(!bgitem.canInteractWithOffHand()) return;
            }
            boolean cd = bgitem.getIf(BGMobilityI.class, Cooldowns.onCooldown("attack", player.getUniqueId()), (m) -> {
                Cooldowns.sendPVPCooldownMessage("attack", player);
                event.setCancelled(true);
                return true;
            });
            if(cd) return;
            r.onRightClickWithAnimation(event, item, mainHand);
        }));
    }

    public static void handleItemLeftClick(ItemStack item, org.bukkit.event.player.PlayerInteractEvent event, Player player, boolean mainHand) {
        BeangameItemRegistry.getFromItemStack(item).ifPresent(bgitem -> bgitem.doIf(BGLClickableI.class, l -> {
            if(mainHand){
                if(!bgitem.canInteractWithMainHand()) return;
            } else {
                if(!bgitem.canInteractWithOffHand()) return;
            }
            boolean cd = bgitem.getIf(BGMobilityI.class, Cooldowns.onCooldown("attack", player.getUniqueId()), (m) -> {
                Cooldowns.sendPVPCooldownMessage("attack", player);
                event.setCancelled(true);
                return true;
            });
            if(cd) return;
            l.onLeftClick(event, item);
        }));
    }

    @EventHandler
    private void onClick(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        ItemStack[] heldItems = new ItemStack[] {
            player.getEquipment().getItemInMainHand(),
            player.getEquipment().getItemInOffHand()
        };

        // Handle left clicks
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            CrownOfTheCosmos.onLeftClick(event, null);
            for (int i = 0; i < 2; i++) {
                ItemStack item = heldItems[i];
                if (item == null || item.getType() == Material.AIR || !ItemNBT.hasBeanGameTag(item)) continue;
                handleItemLeftClick(item, event, player, i == 0);
            }
            return;
        }

        // Ensure the event is a right-click
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        // Prevent null block errors
        Block block = event.getClickedBlock();
        if(block != null && block.getType().name().endsWith("DOOR")){
            if(BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:vampirefang"), VampireFang.class).carryingVampireFang(player))
                event.setCancelled(true);
                return;
        }

        // Prevent actions in spectator mode
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        // Cooldown check (avoid null UUID errors)
        UUID uuid = player.getUniqueId();
        if (uuid == null) {
            Bukkit.getLogger().warning("Player UUID is null in PlayerInteractEvent!");
            return;
        }

        if (Cooldowns.onCooldown("use_item", uuid)) {
            long wandcooldownr = Cooldowns.getRemainingCooldown("use_item", uuid) / 1000L;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cYou are unable to right click for " + wandcooldownr + " second(s)!"));
            event.setCancelled(true);
            return;
        }

        // Handle right-click interactions safely
        for (int i = 0; i < 2; i++) {
            ItemStack item = heldItems[i];
            if (item == null || item.getType() == Material.AIR || !ItemNBT.hasBeanGameTag(item)) continue;
            handleItemRightClick(item, event, player, i == 0);
        }
    }

}

