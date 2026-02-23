package com.beangamecore.items;

import com.beangamecore.commands.BeangameStart;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGRClickableI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Jailbreak extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer(); // player -> player who right clicked
        
        // respawns
        for(Player respawn : Bukkit.getOnlinePlayers()){

            // respawn -> a online player (anybody)
            UUID rUuid = respawn.getUniqueId();
            if(BeangameStart.alivePlayers.contains(rUuid) || Revive.noRevive.contains(rUuid)){
                continue;
            }

            // respawn -> a online player who is dead
            respawn.teleport(player);
            respawn.getInventory().clear();
            respawn.setGameMode(GameMode.SURVIVAL);
            if(!BeangameStart.alivePlayers.contains(rUuid)){
                BeangameStart.alivePlayers.add(rUuid);
            }
            ArrayList<BeangameItem> foods = BeangameItemRegistry.getFoodItemsInRotation();
            int rindex = (int)Math.round(Math.random() * (foods.size()-1));
            ItemStack food = foods.get(rindex).asItem();
            respawn.getInventory().addItem(food);
        }
        if(stack != null){
            stack.setAmount(0);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0);
        }
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "jailbreak";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§dJailbreak";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to consume and instantly",
            "§arevive all spectating players at your",
            "§alocation. Gives each revived player a",
            "§arandom food item.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.PUFFERFISH_BUCKET;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

