package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IdentitySeparationCore extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getEyeLocation();
        World world = player.getWorld();
        for (Player identityseperationcorevictim : Bukkit.getOnlinePlayers()) {
            Location locv = identityseperationcorevictim.getLocation();
            if (shouldAffectPlayer(identityseperationcorevictim, locv, world, loc)) {
                identityseperationcorevictim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20*8, 0));
                identityseperationcorevictim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*8, 0));
                identityseperationcorevictim.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*8, 0));
                Cooldowns.setCooldown("redacted", identityseperationcorevictim.getUniqueId(), 8000L);
                world.spawnParticle(org.bukkit.Particle.SMOKE, locv, 30, 0.5, 0.5, 0.5, 0.2);
                identityseperationcorevictim.playSound(locv, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
                Main.getPlugin().getParticleManager().particleTrail(loc, locv, 75, 0, 130);
            }
        }
        return true;
    }

    private boolean shouldAffectPlayer(Player player, Location playerLoc, World world, Location sourceLoc) {
        return playerLoc.getWorld().equals(world)
                && playerLoc.distance(sourceLoc) < 48.0D
                && player.getGameMode().equals(GameMode.SURVIVAL);
    }

    @Override
    public long getBaseCooldown() {
        return 32000L;
    }

    @Override
    public String getId() {
        return "identityseparationcore";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " F ", "DBG", " G ", r.mCFromMaterial(Material.FERMENTED_SPIDER_EYE), r.mCFromMaterial(Material.DISC_FRAGMENT_5), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.TINTED_GLASS));
        return null;
    }

    @Override
    public String getName() {
        return "§5Identity Separation Core";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Applies blindness, invisibility, and",
            "§9glowing to all nearby players within",
            "§948 blocks for 10 seconds, including",
            "§9yourself.",
            "",
            "§aSupport",
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
        return Material.HEAVY_CORE;
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

