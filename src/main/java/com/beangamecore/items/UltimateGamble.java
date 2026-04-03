package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BG3sTickingI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class UltimateGamble extends BeangameItem implements BGRClickableI, BG3sTickingI {

    public void setActive(boolean active){
        UltimateGambleActive = active;
    }

    @Override
    public void tick(){
        for(Player player : players){
            if(!player.getGameMode().equals(GameMode.SURVIVAL)){
                players.remove(player);
                continue;
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 2));
            Cooldowns.setCooldown("attack", player.getUniqueId(), 5000);
        }  
    };

    public static boolean UltimateGambleActive = true;

    private static CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        if(!UltimateGambleActive){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3This item will work 4 minutes into the game!"));
            return false;
        }
        if(players.contains(player)){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3You may only have a single gamble going at a time!"));
            return false;
        }
        // item event
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3A gamble has begun!"));
        stack.setAmount(stack.getAmount() - 1);
        players.add(player);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                if(player.isOnline() && players.contains(player)){
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3Successful Gamble!"));
                    ItemStack item = BeangameItemRegistry.get(NamespacedKey.fromString("beangame:beanchronicles")).get().asItem();
                    for(int i = 0; i < 3; i++){
                        if (player.getInventory().firstEmpty() != -1) {
                            player.getInventory().addItem(item);
                        } else {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                    }
                }
                players.remove(player);
             }
        }, 2400L);

        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 0L;
    }

    @Override
    public String getId() {
        return "ultimategamble";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§dUltimate Gamble";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to enter a 2-minute gamble.",
            "§9Become glowing with Weakness and",
            "§9Mining Fatigue. Survive to receive",
            "§93 Bean Chronicles. Consumed on use.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.COPPER_INGOT;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

