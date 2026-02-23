package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.Main;
import com.beangamecore.commands.BeangameStart;
import com.beangamecore.events.ServerLoad;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.death.BGDeathArmorI;
import com.beangamecore.registry.BeangameItemRegistry;

public class AngelicCrown extends BeangameItem implements BGDeathArmorI {
    
    @Override
    public void onDeathArmor(EntityDeathEvent event, ItemStack armor) {
        LivingEntity entity = event.getEntity();
        UUID uuid = entity.getUniqueId();
        armor.setAmount(0);

        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        World world = player.getWorld();
        
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) return;

            // Ensure player is tracked as alive
            BeangameStart.alivePlayers.add(uuid);

            // Teleport and inventory
            Location spawnLoc = ServerLoad.randomSpawn(p, world);
            p.teleport(spawnLoc, TeleportCause.SPECTATE);
            p.getInventory().clear();

            // Give a random food item
            List<BeangameItem> foods = BeangameItemRegistry.getFoodItemsInRotation();
            if (!foods.isEmpty()) {
                ItemStack food = foods.get(ThreadLocalRandom.current().nextInt(foods.size())).asItem();
                p.getInventory().addItem(food);
            }

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (!p.isOnline()) return;
                p.setGameMode(GameMode.SURVIVAL);
            }, 1L);
        }, 8L);

        // Run sound + particle effects
        int[] ticks = {0};
        int[] taskId = new int[1];
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Location loc = p.getLocation();
            world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
            world.spawnParticle(Particle.FALLING_HONEY, loc, ticks[0]);

            if (++ticks[0] > 5) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
            }
        }, 0, 4).getTaskId();
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "angeliccrown";
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
        return "§eAngelic Crown";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Revives the wearer after death,",
            "§6teleporting them to safety with",
            "§6a random food item.",
            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Head:", "§9+3 Armor"
        );
    }


    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_HELMET;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.RESIN, TrimPattern.RIB);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 3;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.HEAD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
