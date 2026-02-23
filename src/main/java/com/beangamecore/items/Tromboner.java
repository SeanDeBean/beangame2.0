package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import com.beangamecore.commands.BeangameDistribute;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Tromboner extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (BeangameDistribute.bgdistributeRecent) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§cCannot use during another item roll!"));
            return false;
        }
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        BeangameDistribute.bgdistributeRecent = true;

        Location loc = player.getLocation();
        handleItemEvent(player, loc);

        scheduleDelayedTask(player, loc);

        return true;
    }

    private void handleItemEvent(Player player, Location loc) {
        player.getWorld().playSound(loc, Sound.ENTITY_HORSE_DEATH, 3, 1);
        for (Player warnmessage : Bukkit.getOnlinePlayers()) {
            if (warnmessage.getLocation().distance(loc) <= 48
                    && !warnmessage.getUniqueId().equals(player.getUniqueId())) {
                warnmessage.getWorld().playSound(warnmessage.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
                warnmessage.sendTitle(null, "§3Items arriving soon!", 20, 100, 20);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + warnmessage.getName()
                        + " title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");
            }
        }
    }

    private void scheduleDelayedTask(Player player, Location loc) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            handleDelayedEvent(player, loc);
            BeangameDistribute.bgdistributeRecent = false;
        }, 60L);
    }

    private void handleDelayedEvent(Player player, Location loc) {
        for (Player displayfalse : Bukkit.getOnlinePlayers()) {
            if (displayfalse.getLocation().distance(loc) <= 48
                    && !displayfalse.getUniqueId().equals(player.getUniqueId())) {
                displayfalse.getWorld().playSound(displayfalse.getLocation(), Sound.ENTITY_BAT_DEATH, 1F, 1F);
                displayfalse.sendTitle("§coops!", "§3No items arriving soon!", 20, 100, 20);
                displayfalse.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 140, 1));
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 48000L;
    }

    @Override
    public String getId() {
        return "tromboner";
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
        return "§6Tromboner";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to play a fake item distribution",
            "§9sound and message for nearby players,",
            "§9followed by a disappointment effect",
            "§9that applies nausea.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_HOE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
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

