package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.blocks.type.BGMPTickableB;
import com.beangamecore.registry.BeangameBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;

public class IHadACake extends BeangameItem implements BGRClickableI, BGProjectileI, BGMPTickableB {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
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
        Location loc = player.getEyeLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_ARMADILLO_ROLL, 1.0F, 1.0F);
        Snowball snowball = launchProjectile(this, player, Snowball.class);
        snowball.setVelocity(loc.getDirection().multiply(1.55));
        return true;
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        event.getEntity().remove();

        Block block = event.getEntity().getLocation().getBlock();
        block.setType(Material.CAKE);

        for(int i = 0; i < 8; i++){
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                if(block.getType().equals(Material.CAKE)){
                    block.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, block.getLocation().add(0.5, 0.3, 0.5), 1);
                }
            }, 10*i);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if(block.getType().equals(Material.CAKE)){
                block.getWorld().playSound(block.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 0.5F, 1);
                BeangameBlockData.addBeangameBlock(this, block);
            }
        }, 8*10);
    }
    
    @Override
    public long getBaseCooldown() {
        return 4000L;
    }

    @Override
    public String getId() {
        return "ihadacake";
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
        return "§dI Had A Cake";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Shoots a projectile that places",
            "§9explosive cakes. When players approach,",
            "§9cakes flash and explode after 0.75",
            "§9seconds dealing area damage.",
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
        return Material.CAKE;
    }

    @Override
    public void onDestroy(Block block) {

    }

    @Override
    public boolean shouldDropOnDestroy(Block block) {
        return false;
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

    @Override
    public void tick(Block block) {
        Location loc = block.getLocation().add(0.5, 0.3, 0.5);
        World world = loc.getWorld();
        for (Entity entity : block.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
            if (entity instanceof LivingEntity) {
                if (shouldSkipPlayer(entity)) {
                    continue;
                }
                if (block.getType() == Material.CAKE) {
                    handleCakeExplosion(block, loc, world, entity);
                }
                continue;
            }
        }
    }

    private boolean shouldSkipPlayer(Entity entity) {
        if (entity instanceof Player) {
            if (!((Player) entity).getGameMode().equals(GameMode.SURVIVAL)) {
                return true;
            }
        }
        return false;
    }

    private void handleCakeExplosion(Block block, Location loc, World world, Entity entity) {
        // Schedule explosion and block state changes
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                entity.getWorld().createExplosion(loc, 3F, false, true);
            };
        }, 15L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                block.setType(Material.CAKE);
            };
        }, 3L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                block.setType(Material.AIR);
            };
        }, 6L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                block.setType(Material.CAKE);
            };
        }, 9L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable() {
            public void run() {
                block.setType(Material.AIR);
            };
        }, 12L);
        block.setType(Material.AIR);
        world.playSound(loc, Sound.ENTITY_CREEPER_PRIMED, 1, 1);
        world.spawnParticle(Particle.FLAME, loc, 6);
    }
}

