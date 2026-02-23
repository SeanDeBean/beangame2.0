package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShadowBomb extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        player.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.5F, 1.0F);
        player.spawnParticle(Particle.CLOUD, player.getLocation(), 30);
        Cooldowns.setCooldown("explosion_immunity", uuid, 4500L);
        for (Player shadowbombvictim : Bukkit.getOnlinePlayers()) {
            Location vloc = shadowbombvictim.getLocation();
            if (isShadowBombVictim(shadowbombvictim, player, vloc, loc)) {
                shadowbombvictim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                if (Math.random() < 0.4) {
                    shadowbombvictim.getWorld().spawn(vloc, TNTPrimed.class);
                }
            }
        }
        return true;
    }

    private boolean isShadowBombVictim(Player shadowbombvictim, Player player, Location vloc, Location loc) {
        return vloc.getWorld().equals(loc.getWorld()) &&
                vloc.distance(loc) < 12.0D &&
                !shadowbombvictim.getUniqueId().equals(player.getUniqueId()) &&
                shadowbombvictim.getGameMode().equals(GameMode.SURVIVAL);
    }
    
    @Override
    public long getBaseCooldown() {
        return 22000L;
    }

    @Override
    public String getId() {
        return "shadowbomb";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "III", "ITI", "III", r.mCFromMaterial(Material.INK_SAC), r.eCFromBeangame(Key.bg("tntimer")));
        return null;
    }

    @Override
    public String getName() {
        return "§8Shadow Bomb";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to create a 12 block",
            "§9radius shadow explosion. Applies",
            "§9blindness for 5 seconds to all",
            "§9nearby players and has 40% chance",
            "§9to spawn TNTon each. Grants you",
            "§9explosion immunity for 4.5 seconds.",
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
        return Material.BLACK_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

