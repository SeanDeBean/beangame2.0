package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.commands.PvpToggleCommand;
import com.beangamecore.particles.BeangameParticleManager;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Ignite extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        // effect on user
        applyUserEffects(player, loc);
        if (!PvpToggleCommand.pvp) {
            return false;
        }
        // effect on other players
        igniteNearbyPlayers(player, loc);
        return false;
    }

    private void applyUserEffects(Player player, Location loc) {
        player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 2.0F, 1.0F);
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        double health = attribute.getValue();
        if (player.getHealth() + 8 >= health) {
            player.setHealth(health);
        } else {
            player.setHealth(player.getHealth() + 8);
        }
        player.setFoodLevel(player.getFoodLevel() + 8);
    }

    private void igniteNearbyPlayers(Player player, Location loc) {
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        for (LivingEntity ignitevictim : player.getWorld().getLivingEntities()) {
            Location vloc = ignitevictim.getLocation();
            if (isValidIgniteVictim(ignitevictim, player, loc, vloc)) {
                particleManager.particleTrail(loc, vloc, 255, 69, 0);
                applyIgniteEffects(ignitevictim);
            }
        }
    }

    private boolean isValidIgniteVictim(LivingEntity ignitevictim, Player player, Location loc, Location vloc) {
        return vloc.getWorld().equals(loc.getWorld()) &&
                vloc.distance(loc) < 8.0D &&
                !ignitevictim.getUniqueId().equals(player.getUniqueId());
    }

    private void applyIgniteEffects(LivingEntity ignitevictim) {
        if (ignitevictim instanceof Player) {
            Player victimPlayer = (Player) ignitevictim;
            if (!victimPlayer.getGameMode().equals(GameMode.SPECTATOR)
                    && !victimPlayer.getGameMode().equals(GameMode.CREATIVE)) {
                victimPlayer.sendHurtAnimation(0);
            }
        }
        double health = ignitevictim.getHealth() - 2;
        if (health < 0) {
            health = 0;
        }
        ignitevictim.setHealth(health);
        World world = ignitevictim.getWorld();
        Location vloc = ignitevictim.getLocation();
        world.spawnParticle(Particle.FLAME, vloc, 3);
        world.playSound(vloc, Sound.ENTITY_PLAYER_HURT, 0.6F, 1.0F);
        ignitevictim.setFireTicks(100);
    }

    @Override
    public long getBaseCooldown() {
        return 15500L;
    }

    @Override
    public String getId() {
        return "ignite";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " P ", "LBL", "CCC", r.mCFromMaterial(Material.BLAZE_POWDER), r.mCFromMaterial(Material.LAVA_BUCKET), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.CAMPFIRE));
        return null;
    }

    @Override
    public String getName() {
        return "§4Ignite";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Heals you for 4 hearts and restores",
            "§9hunger. Damages and sets all nearby",
            "§9entities on fire for 5 seconds.",
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
        return Material.BLAZE_POWDER;
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

