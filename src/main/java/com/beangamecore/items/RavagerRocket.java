package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.target.BGTargetTalisman;
import com.beangamecore.util.Cooldowns;

public class RavagerRocket extends BeangameItem implements BGRClickableI, BGTargetTalisman {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // Item event - play sound and action bar message
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ITEM_GOAT_HORN_SOUND_2, 3.0F, 1.0F);

        // Ravager summon
        Entity ravager = player.getWorld().spawnEntity(loc, EntityType.RAVAGER);
        ravager.setCustomName(player.getName() + "'s ravager");
        ravager.setVelocity(loc.getDirection().multiply(3.2));
        ((Ravager) ravager).getEquipment().clear();

        ravager.addPassenger(player);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                if (ravager.isValid() && !ravager .isDead()) {
                    ravager.remove();
                }
            }
        }, 260L);

        Cooldowns.setCooldown("fall_damage_immunity", uuid, 7500L);
        return true;
    }

    @Override
    public void onTargetTalisman(EntityTargetLivingEntityEvent event, ItemStack armor) {
        if (event.getEntity().getType() != EntityType.RAVAGER)
            return;

        Entity target = event.getTarget();
        if (target == null)
            return;

        if (event.getEntity().getPassengers().contains(target)) {
            handlePassengerTargeting(event, target);
        }
    }

    private void handlePassengerTargeting(EntityTargetLivingEntityEvent event, Entity target) {
        Location origin = event.getEntity().getLocation();
        Player closestPlayer = findClosestValidPlayer(origin, target);

        // Redirect the target to the nearest other player
        if (closestPlayer != null) {
            event.setTarget(closestPlayer);
        } else {
            event.setCancelled(true); // No valid player, so cancel the targeting
        }
    }

    private Player findClosestValidPlayer(Location origin, Entity excludeTarget) {
        double closestDistance = Double.MAX_VALUE;
        Player closestPlayer = null;

        for (Player p : origin.getWorld().getPlayers()) {
            if (p.equals(excludeTarget))
                continue;
            if (!(p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE)))
                continue;
            double dist = p.getLocation().distanceSquared(origin);
            if (dist < closestDistance) {
                closestDistance = dist;
                closestPlayer = p;
            }
        }
        return closestPlayer;
    }

    @Override
    public long getBaseCooldown() {
        return 39000L;
    }

    @Override
    public String getId() {
        return "ravagerrocket";
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
        return "§3Ravager Rocket";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to launch forward on a",
            "§9ravager that lasts 13 seconds. Ravager",
            "§9targets nearest player instead of you",
            "§9as long as you are riding the ravager",
            "",
            "§9Summon",
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
        return Material.BRICK;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

