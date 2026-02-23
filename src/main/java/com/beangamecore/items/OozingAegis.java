package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.events.ServerLoad;

public class OozingAegis extends BeangameItem implements BGRClickableI, BGLPTalismanI {
    
    private static final Map<UUID, List<UUID>> playerSlimes = new HashMap<>();
    private static final long SLIME_DURATION = 400L; // 20 ticks = 1 second
    private static final double MODEL_SWITCH_CHANCE = 0.5;

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        int currentModel = meta.getCustomModelData();
        int newModel = determineNextModel(currentModel);
        
        meta.setCustomModelData(newModel);
        item.setItemMeta(meta);
    }

    private int determineNextModel(int currentModel) {
        switch (currentModel) {
            case 101: return 102;
            case 102: return Math.random() >= MODEL_SWITCH_CHANCE ? 101 : 103;
            default: return 101;
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check cooldown
        if (onCooldown(playerId)) {
            sendCooldownMessage(player);
            return false;
        }
        
        applyCooldown(playerId);
        summonSlime(player);
        return true;
    }

    private void summonSlime(Player player) {
        Location spawnLoc = player.getLocation();
        World world = spawnLoc.getWorld();
        
        Slime slime = (Slime) world.spawnEntity(spawnLoc, EntityType.SLIME);
        configureSlime(slime, player);
        
        trackSlime(player.getUniqueId(), slime.getUniqueId());
        scheduleSlimeRemoval(player, slime);
    }

    private void configureSlime(Slime slime, Player player) {
        slime.setAI(false);
        slime.setCustomName(player.getName() + "'s oozing aegis");
        slime.setCustomNameVisible(false);
        slime.setGravity(false);
        slime.setSize(8);
        slime.setInvulnerable(true);
        ServerLoad.noCollisions.addEntry(slime.getUniqueId().toString());
    }

    private void trackSlime(UUID playerId, UUID slimeId) {
        playerSlimes.computeIfAbsent(playerId, k -> new ArrayList<>()).add(slimeId);
    }

    private void scheduleSlimeRemoval(Player player, Slime slime) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (slime.isValid()) {
                removeSlime(player, slime);
            }
        }, SLIME_DURATION);
    }

    private void removeSlime(Player player, Slime slime) {
        player.getWorld().playSound(slime.getLocation(), Sound.ENTITY_SLIME_DEATH, 1, 0);
        slime.remove();
        cleanupSlimeRecord(player.getUniqueId(), slime.getUniqueId());
    }

    private void cleanupSlimeRecord(UUID playerId, UUID slimeId) {
        List<UUID> slimes = playerSlimes.get(playerId);
        if (slimes != null) {
            slimes.remove(slimeId);
            if (slimes.isEmpty()) {
                playerSlimes.remove(playerId);
            }
        }
        ServerLoad.noCollisions.removeEntry(slimeId.toString());
    }

    public boolean handleSlimeHit(ProjectileHitEvent event) {
        Entity hitEntity = event.getHitEntity();
        if (!(hitEntity instanceof Slime)) return false;
        
        Slime slime = (Slime) hitEntity;
        if (isOozingAegisSlime(slime)) {
            event.getEntity().remove();
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    private boolean isOozingAegisSlime(Slime slime) {
        String customName = slime.getCustomName();
        if (slime.getCustomName() == null || ! customName.endsWith("'s oozing aegis")) return false;
        
        // Get the player name portion
        String playerName = customName.substring(0, customName.length() - "'s oozing aegis".length());
        
        // Check if any player with this name has spawned slimes
        for (Map.Entry<UUID, List<UUID>> entry : playerSlimes.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.getName().equals(playerName)) {
                return entry.getValue().contains(slime.getUniqueId());
            }
        }
        return false;
    }

    @Override
    public long getBaseCooldown() {
        return 30000L;
    }

    @Override
    public String getId() {
        return "oozingaegis";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "BSB", "SAS", "BSB", r.mCFromMaterial(Material.SLIME_BALL), r.mCFromMaterial(Material.SHIELD), r.eCFromBeangame(Key.bg("emotionalsupportanimal")));
        return null;
    }

    @Override
    public String getName() {
        return "§aOozing Aegis";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon a large slime",
            "§9that blocks projectiles for 20 seconds.",
            "§9Slime is invulnerable and cancels all",
            "§9projectiles that hit it.",
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
        return Material.SLIME_BALL;
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

