package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BG2tTickingI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class WarpingTrader extends BeangameItem implements BGLPTalismanI, BGRClickableI, BG2tTickingI {

    private Type[] types = new Type[]{Type.DESERT, Type.JUNGLE, Type.PLAINS, Type.SAVANNA, Type.SNOW, Type.SWAMP, Type.TAIGA};

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        // villager summon
        Villager villager = (Villager) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.VILLAGER);
        villager.setCustomName(player.getName());
        villager.setBaby();
        int i = ThreadLocalRandom.current().nextInt(types.length);
        villager.setVillagerType(types[i]);
        World world = player.getWorld();
        world.playSound(loc, Sound.ENTITY_VILLAGER_TRADE, 1, 1);
        villager.setVelocity(loc.getDirection().multiply(3));
        warpingtraderVillagers.add(villager);
        // timed task for villager
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            villager.setHealth(0);
            warpingtraderVillagers.remove(villager);
            world.createExplosion(villager.getLocation(), 1.5F, false, true, player);
            world.spawnParticle(Particle.DRAGON_BREATH, villager.getLocation(), 4);
        }, 20L);
        return true;
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        if (Math.random() <= 0.15){
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.EMERALD, 1));
            }
        }
    }

    public static CopyOnWriteArrayList<Villager> warpingtraderVillagers = new CopyOnWriteArrayList<>();

    @Override
    public void tick() {
        // Safely remove dead villagers
        warpingtraderVillagers.removeIf(Villager::isDead);
        
        // Spawn particles for remaining villagers
        for (Villager villager : warpingtraderVillagers) {
            DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 1);
            villager.getWorld().spawnParticle(Particle.DUST, villager.getLocation(), 1, dustOptions);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 3500L;
    }

    @Override
    public String getId() {
        return "warpingtrader";
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
        return "§aWarping Trader";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Passively generates emeralds while",
            "§9carried in your inventory.",
            "§9Right-click to launch a random baby",
            "§9villager that explodes on impact after",
            "§91 second.",
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
        return Material.EMERALD_BLOCK;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

