package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.beangamecore.entities.renderers.BlockDisplayRenderer;
import com.beangamecore.entities.renderers.RenderDebugOptions;
import com.beangamecore.entities.tntspider.Gait;
import com.beangamecore.entities.tntspider.Spider;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BG1tTickingI;

import org.bukkit.*;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class SuicidalSpider extends BeangameItem implements BGRClickableI, BG1tTickingI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack itemStack){
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
        Gait gait = new Gait();
        // make adjustments to gait here
        Spider spider = new Spider(loc, gait, player);
        new BlockDisplayRenderer().renderSpider(spider, RenderDebugOptions.none());
        // spider.setBehaviour(new DirectionBehaviour(new Vector(1, 0, 0)));
        return true;
    }

    Random random = new Random();

    @Override
    public void tick() {
        List<Spider> toRemove = new ArrayList<>();

        for (Spider instance : Spider.spiders) {
            Location spiderLocation = instance.getLocation();
            World world = spiderLocation.getWorld();

            if (world == null) continue;

            // Teleport nearby TNT, creepers, etc.
            double radius = 3.5;
            for (Entity entity : world.getNearbyEntities(spiderLocation, radius, radius, radius)) {
                if (entity instanceof TNTPrimed || entity instanceof Minecart || entity instanceof Creeper) {
                    double offsetX = random.nextDouble(-0.1, 0.1);
                    double offsetY = random.nextDouble(-0.1, 0.1);
                    double offsetZ = random.nextDouble(-0.1, 0.1);

                    Location teleportLocation = spiderLocation.clone().add(offsetX, offsetY, offsetZ);
                    entity.teleport(teleportLocation);
                }

                if (entity instanceof EnderCrystal) {
                    EnderCrystal original = (EnderCrystal) entity;

                    // Store original properties
                    Entity customVehicle = original.getVehicle();

                    // Calculate new position
                    double offsetX = random.nextDouble(-0.01, 0.01);
                    double offsetY = random.nextDouble(-0.01, 0.01);
                    double offsetZ = random.nextDouble(-0.01, 0.01);
                    Location newLoc = spiderLocation.clone().add(offsetX, offsetY, offsetZ);

                    // Remove and respawn
                    original.remove();
                    EnderCrystal crystal = (EnderCrystal) world.spawnEntity(newLoc, EntityType.END_CRYSTAL);
                    crystal.setShowingBottom(false);
                    if (customVehicle != null) {
                        customVehicle.addPassenger(crystal);
                    }

                    continue;
                }
            }

            instance.update();
            instance.getRenderer().renderSpider(instance, RenderDebugOptions.none());
            instance.ticksAlive++;

            if (instance.ticksAlive == 220 || instance.ticksAlive == 230) {
                world.playSound(spiderLocation, Sound.ENTITY_PARROT_IMITATE_CREEPER, 1f, 1f);
                world.spawnParticle(Particle.FLAME, spiderLocation, 5);
            }

            if (instance.ticksAlive > 240) {
                try {
                    LivingEntity owner = instance.owner;
                    if (owner != null && owner.isValid() && !owner.isDead()) {
                        world.createExplosion(spiderLocation, 3.4F, true, true, owner);
                    } else {
                        world.createExplosion(spiderLocation, 3.4F, true, true);
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Suicidal Spider explosion fallback used: " + e.getMessage());
                    world.createExplosion(spiderLocation, 3.4F, true, true);
                }

                instance.getRenderer().clearSpider(instance);
                toRemove.add(instance);
            }
        }

        Spider.spiders.removeAll(toRemove);
    }

    @Override
    public long getBaseCooldown() {
        return 10000L;
    }

    @Override
    public String getId() {
        return "suicidalspider";
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
        return "§4Suicidal Spider";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to spawn a mechanical spider",
            "§9that races forward and collects nearby",
            "§9TNT, Creepers, and End Crystals.",
            "§9Explodes after 12 seconds.",
            "",
            "§9Castable",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_INGOT;
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

