package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.general.BGResetableI;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Recall extends BeangameItem implements BGConsumableI, BGResetableI {

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        if(player.isSneaking()){ // set location (no cooldown)
            recallloc.put(uuid, player.getLocation());
            world.spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 5);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3Location saved!"));
            world.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 0);
        } else { // teleport to location (has cooldown)
            if(recallcd.containsKey(uuid) && recallcd.get(uuid) > System.currentTimeMillis()){
                long recallcdr = (recallcd.get(uuid) - System.currentTimeMillis()) / 1000L;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3Wait " + recallcdr + " second(s) before using again!"));
                return;
            }
            if(recallloc.containsKey(uuid)){
                // do event
                recallcd.put(uuid, System.currentTimeMillis() + 34000L);
                world.spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 5);
                world.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1, 0);
                Location loc = recallloc.get(uuid);
                loc.getWorld().spawnParticle(Particle.ENCHANT, loc.add(0, 1, 0), 5);
                loc.getWorld().playSound(loc, Sound.BLOCK_DISPENSER_DISPENSE, 1, 0);
                player.teleport(loc);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3No saved location!"));
            }
        }
    }

    private static Map<UUID, Long> recallcd = new HashMap<>();
    private static Map<UUID, Location> recallloc = new HashMap<>();

    @Override
    public void resetItem(){
        recallloc.clear();
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "recall";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "TLC", "EWS", " B ", r.mCFromMaterial(Material.IRON_TRAPDOOR), r.mCFromMaterial(Material.LEVER), r.eCFromBeangame(Key.bg("cosmicingot")), r.mCFromMaterial(Material.ENDER_PEARL), r.mCFromMaterial(Material.WATER_BUCKET), r.mCFromMaterial(Material.SOUL_SAND), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§3Recall";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Consume while sneaking to save",
            "§9current location. Consume normally",
            "§9to teleport to saved location.",
            "",
            "§2Food",
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
        return Material.ENCHANTED_GOLDEN_APPLE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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

