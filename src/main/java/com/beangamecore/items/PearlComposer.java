package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class PearlComposer extends BeangameItem implements BGRClickableI, BGProjectileI {

    // Track endermites by player UUID
    private static final Map<UUID, List<UUID>> playerEndermites = new HashMap<>();
    private static final long ENDERMITE_DURATION = 230L; // 11.5 seconds

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Location loc = player.getLocation();
        UUID playerId = player.getUniqueId();
        
        // Spawn 3 endermites
        for (int i = 0; i < 3; i++) {
            Endermite endermite = (Endermite) player.getWorld().spawnEntity(loc, EntityType.ENDERMITE);
            configureEndermite(endermite, player);
            trackEndermite(playerId, endermite.getUniqueId());
            scheduleEndermiteRemoval(endermite, player);
        }
    }

    private void configureEndermite(Endermite endermite, Player player) {
        endermite.setCustomName(player.getName() + "'s endermite");
        endermite.setCustomNameVisible(false);
        // Add any other configuration here
    }

    private void trackEndermite(UUID playerId, UUID endermiteId) {
        playerEndermites.computeIfAbsent(playerId, k -> new ArrayList<>()).add(endermiteId);
    }

    private void scheduleEndermiteRemoval(Endermite endermite, Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (endermite.isValid()) {
                removeEndermite(endermite, player);
            }
        }, ENDERMITE_DURATION);
    }

    private void removeEndermite(Endermite endermite, Player player) {
        UUID endermiteId = endermite.getUniqueId();
        UUID playerId = player.getUniqueId();
        
        // Only remove if still tracked
        if (playerEndermites.getOrDefault(playerId, Collections.emptyList()).contains(endermiteId)) {
            endermite.setHealth(0);
            cleanupEndermiteRecord(playerId, endermiteId);
        }
    }

    private void cleanupEndermiteRecord(UUID playerId, UUID endermiteId) {
        List<UUID> endermites = playerEndermites.get(playerId);
        if (endermites != null) {
            endermites.remove(endermiteId);
            if (endermites.isEmpty()) {
                playerEndermites.remove(playerId);
            }
        }
    }


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
        Location loc = player.getLocation();
        World world = player.getWorld();
        world.playSound(loc, Sound.ENTITY_ENDER_PEARL_THROW, 1.0F, 1.0F);
        launchProjectile(this, player, EnderPearl.class);
        return true;
    }
    
    @Override
    public long getBaseCooldown() {
        return 11000L;
    }

    @Override
    public String getId() {
        return "pearlcomposer";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "BSS", "BNP", "BSS", r.mCFromMaterial(Material.END_STONE_BRICKS), r.mCFromMaterial(Material.END_STONE), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.ENDER_PEARL));
        return null;
    }

    @Override
    public String getName() {
        return "§dPearl Composer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to throw an ender pearl",
            "§9that spawns 3 endermites at your",
            "§9location when it lands. Endermites",
            "§9last for 11.5 seconds before",
            "§9automatically despawning.",
            "",
            "§9Summon",
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
        return Material.END_PORTAL_FRAME;
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

