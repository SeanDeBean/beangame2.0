package com.beangamecore.items;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class DropTheCarts extends BeangameItem implements BGRClickableI {
    
    private static CopyOnWriteArrayList<Entity> dropthecartsTracker = new CopyOnWriteArrayList<>();
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // cooldown system
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        loc.add(0, 150, 0);
        Entity minecart = loc.getWorld().spawnEntity(loc, EntityType.TNT_MINECART);
        dropthecartsTracker.add(minecart);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                if(!minecart.isDead()){
                    minecart.remove();
                }
            }
        }, 30 * 20L);
        return true;
    }

    public void dropthecartsParticles(){
        Iterator<Entity> iterator = dropthecartsTracker.iterator();
        while(iterator.hasNext()){
            Entity minecart = iterator.next();
            Location end = minecart.getLocation();
            int i = 0;
            while(end.getBlock().getType().equals(Material.AIR)){
                i++;
                end.subtract(0, 1, 0);
            }
            if(i <= 16 || minecart.isDead()){
                dropthecartsTracker.remove(minecart);
            }
            Location start = minecart.getLocation();
            Main.getPlugin().getParticleManager().particleTrail(start, end, 255, 0, 0, 3);
            minecart.getWorld().playSound(end, Sound.ENTITY_TNT_PRIMED, 1, 1);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 40000L;
    }

    @Override
    public String getId() {
        return "dropthecarts";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " E ", " T ", " C ", r.eCFromBeangame(Key.bg("cosmiceye")), r.eCFromBeangame(Key.bg("tntimer")), r.mCFromMaterial(Material.MINECART));
        return null;
    }

    @Override
    public String getName() {
        return "§cDrop The Carts ®";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon a TNT minecart",
            "§9150 blocks above you. The minecart falls",
            "§9and creates a red particle trail to the",
            "§9ground.",
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
        return Material.IRON_INGOT;
    }

    @Override
    public int getCustomModelData() {
        return 104;
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

