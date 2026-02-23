package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Heatseeker extends BeangameItem implements BGRClickableI {
    
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
        handleNearbyEntities(player, loc, uuid);
        return true;
    }

    private void handleNearbyEntities(Player player, Location loc, UUID uuid) {
        for (Entity heatseekervictim : player.getNearbyEntities(40, 40, 40)) {
            if (!(heatseekervictim instanceof LivingEntity)) {
                continue;
            } else {
                handleHeatseekerVictim(heatseekervictim, loc, uuid);
            }
        }
    }

    private void handleHeatseekerVictim(Entity heatseekervictim, Location loc, UUID uuid) {
        if (isValidHeatseekerTarget(heatseekervictim, loc, uuid)) {
            if (heatseekervictim instanceof Player
                    && !((Player) heatseekervictim).getGameMode().equals(GameMode.SPECTATOR)) {
                ((Player) heatseekervictim).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1));
            } else if (!(heatseekervictim instanceof Player)) {
                ((LivingEntity) heatseekervictim).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1));
            }
        }
    }

    private boolean isValidHeatseekerTarget(Entity entity, Location playerLoc, UUID playerUuid) {
        Location vloc = entity.getLocation();
        return vloc.getWorld().equals(playerLoc.getWorld())
                && vloc.distance(playerLoc) < 48.0D
                && !entity.getUniqueId().equals(playerUuid);
    }
    @Override
    public long getBaseCooldown() {
        return 20000L;
    }

    @Override
    public String getId() {
        return "heatseeker";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " G ", "GBG", " G ", r.mCFromMaterial(Material.GLOWSTONE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§dHeatseeker";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Applies glowing to all nearby players",
            "§9and mobs within 48 blocks for 10",
            "§9seconds, revealing them through walls.",
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
        return Material.COMPASS;
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

