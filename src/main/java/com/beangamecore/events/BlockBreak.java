package com.beangamecore.events;

import com.beangamecore.blocks.type.BGBreakableB;
import com.beangamecore.gamemodes.RandomizerGamemode;
import com.beangamecore.blocks.generic.BeangameBlock;
import com.beangamecore.registry.BeangameBlockData;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.items.type.BGToolI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import com.beangamecore.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Optional;

public class BlockBreak implements Listener{
    private static void handleItemMining(ItemStack item, org.bukkit.event.block.BlockBreakEvent event) {
        BeangameItemRegistry.get(ItemNBT.getBeanGame(item)).ifPresent(bgitem -> {
            boolean end = bgitem.getIf(BGMobilityI.class, Cooldowns.onCooldown("attack", event.getPlayer().getUniqueId()), m -> {
                Cooldowns.sendPVPCooldownMessage("attack", event.getPlayer());
                return true;
            });
            if(!end) bgitem.doIf(BGToolI.class, t -> t.onBlockBreak(event, item));
        });
    }

    @EventHandler
    private void onMine(org.bukkit.event.block.BlockBreakEvent event){

        // cancels the breaking of bean buckets
        Block block = event.getBlock();
        Material material = block.getType();
        Player player = event.getPlayer();

        if(material == Material.CRYING_OBSIDIAN){
            for(ArmorStand armorStand : player.getWorld().getEntitiesByClass(ArmorStand.class)){
                NamespacedKey portalmakernsk = new NamespacedKey(Main.getPlugin(), "portalmaker_beangame");
                if(block.getLocation().add(0.5,1,0.5).distance(armorStand.getLocation()) <= 0.3 && armorStand.getPersistentDataContainer().has(portalmakernsk, PersistentDataType.BOOLEAN)){
                    if(player.isOp() && player.getGameMode().equals(GameMode.CREATIVE)){
                        block.setType(Material.AIR);
                        return;
                    } else {
                        event.setCancelled(true);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cYou don't have permission to break this block!"));
                        return;
                    }
                }
            }
        }


        // randomizer
        if(Main.getPlugin().getBeangameModes().getGameMode("randomizer").isEnabled()){
            RandomizerGamemode.randomDrops(event);
        }


        // checks to see if there is a held item
        if(!player.getEquipment().getItemInMainHand().getType().equals(Material.AIR)){
            ItemStack item = player.getEquipment().getItemInMainHand();
            if(ItemNBT.hasBeanGameTag(item)){
                handleItemMining(item, event);
            }
        }
        Optional<BeangameBlock> bgblock = BeangameBlockData.getBeangameBlock(block);
        bgblock.ifPresent(b -> {
            if(!event.isCancelled() && b instanceof BGBreakableB breakable) breakable.onBlockBreak(event, player.getEquipment().getItemInMainHand());
        });
    }
}

