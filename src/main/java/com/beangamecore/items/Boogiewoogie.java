package com.beangamecore.items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.BGRClickableI;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Boogiewoogie extends BeangameItem implements BGLClickableI, BGRClickableI {
    
    // ============ CONFIGURABLE SETTINGS ============
    private static final int MAX_MARK_RANGE = 64;           // Max range to mark a player
    private static final int MIN_MARK_RANGE = 3;            // Must be at least this close
    private static final int MARK_DURATION_TICKS = 400;     // 20 seconds to complete swap
    private static final int MARK_GLOW_DURATION = 600;      // 30 seconds glow
    private static final double BASE_CONE_ANGLE = Math.toRadians(12);      // Tight at close range (12°)
    private static final double MAX_CONE_ANGLE = Math.toRadians(35);       // Lenient at long range (35°)
    private static final double LENIENCY_START_DISTANCE = 20.0;            // When leniency begins
    private static final double MAX_LENIENCY_DISTANCE = 60.0;              // Full leniency
    
    // Visual colors
    private static final Color MARKER_COLOR_A = Color.fromRGB(255, 100, 100);
    private static final Color MARKER_COLOR_B = Color.fromRGB(100, 100, 255);
    private static final Color SELF_MARKER_COLOR = Color.fromRGB(255, 255, 100);
    private static final Color CONE_COLOR = Color.fromRGB(200, 200, 200);
    private static final Color CONE_CENTER_COLOR = Color.fromRGB(255, 255, 255);

    private final Map<UUID, MarkData> playerMarks = new ConcurrentHashMap<>();

    private static class MarkData {
        final UUID targetUuid;
        final String targetName;
        final long expiryTime;
        final boolean isSelfMark;
        
        MarkData(UUID targetUuid, String targetName, long expiryTime, boolean isSelfMark) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.expiryTime = expiryTime;
            this.isSelfMark = isSelfMark;
        }
    }
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Shift + Right Click = Mark A (or self if looking down)
        if (player.isSneaking()) {
            event.setCancelled(true);
            
            // Check if looking down for self-mark
            float pitch = player.getLocation().getPitch();
            if (pitch > 60) {
                markSelf(player);
                return true;
            }
            
            attemptMark(player, true);
            return true;
        }
        
        // Regular Right Click = Mark B and execute swap
        event.setCancelled(true);
        
        MarkData markA = playerMarks.get(uuid);
        if (markA == null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cShift+RClick to mark first target first!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return false;
        }
        
        if (System.currentTimeMillis() > markA.expiryTime) {
            playerMarks.remove(uuid);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cYour mark expired! Shift+RClick to mark again."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return false;
        }
        
        attemptMarkAndSwap(player, markA);
        return true;
    }
    
    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        if (playerMarks.containsKey(uuid)) {
            event.setCancelled(true);
            clearMark(player);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§eMark cleared."));
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
        }
    }
    
    private void markSelf(Player player) {
        UUID uuid = player.getUniqueId();
        clearMark(player);
        
        long expiryTime = System.currentTimeMillis() + (MARK_DURATION_TICKS * 50L);
        MarkData markData = new MarkData(uuid, player.getName(), expiryTime, true);
        playerMarks.put(uuid, markData);
        
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 20, 
            0.5, 0.5, 0.5, new Particle.DustOptions(SELF_MARKER_COLOR, 1.5f));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, MARK_GLOW_DURATION, 0, false, false, false));
        
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy("§e§lMarked: §f§lYOU §7| §fRight-click to mark second target"));
    }
    
    private void attemptMark(Player player, boolean isMarkA) {
        Player target = findTargetPlayer(player);
        
        if (target == null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cNo player found! Aim closer to your target."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            showTargetingCone(player, null);
            return;
        }
        
        if (target.equals(player)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§eLook down and Shift+RClick to mark yourself!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return;
        }
        
        clearMark(player);
        
        long expiryTime = System.currentTimeMillis() + (MARK_DURATION_TICKS * 50L);
        MarkData markData = new MarkData(target.getUniqueId(), target.getName(), expiryTime, false);
        playerMarks.put(player.getUniqueId(), markData);
        
        Color markColor = isMarkA ? MARKER_COLOR_A : MARKER_COLOR_B;
        target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 20, 
            0.5, 0.5, 0.5, new Particle.DustOptions(markColor, 1.5f));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.2f);
        
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, MARK_GLOW_DURATION, 0, false, false, false));
        
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacy("§e§lMarked: §f§l" + target.getName() + " §7| §fRight-click to mark second target"));
    }
    
    private void attemptMarkAndSwap(Player player, MarkData markA) {
        Player targetB = findTargetPlayer(player);
        
        if (targetB == null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cNo player found for second mark!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            showTargetingCone(player, null);
            return;
        }
        
        Player targetA;
        if (markA.isSelfMark) {
            targetA = player;
        } else {
            targetA = Bukkit.getPlayer(markA.targetUuid);
            if (targetA == null || !targetA.isOnline()) {
                playerMarks.remove(player.getUniqueId());
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§cFirst target is no longer online!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
                return;
            }
        }
        
        if (targetB.equals(targetA)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacy("§cCannot swap with the same target!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
            return;
        }
        
        executeSwap(player, targetA, targetB, markA.isSelfMark);
    }
    
    private void executeSwap(Player user, Player targetA, Player targetB, boolean isSelfInvolved) {
        Location locA = targetA.getLocation().clone();
        Location locB = targetB.getLocation().clone();
        
        float yawA = locA.getYaw();
        float pitchA = locA.getPitch();
        float yawB = locB.getYaw();
        float pitchB = locB.getPitch();
        
        Location newLocA = locB.clone();
        newLocA.setYaw(yawA);
        newLocA.setPitch(pitchA);
        
        Location newLocB = locA.clone();
        newLocB.setYaw(yawB);
        newLocB.setPitch(pitchB);
        
        targetA.teleport(newLocA);
        targetB.teleport(newLocB);
        
        World world = user.getWorld();
        
        spawnSwapTrail(locA, locB, MARKER_COLOR_A, MARKER_COLOR_B);
        
        world.spawnParticle(Particle.FLASH, locA.add(0, 1, 0), 1);
        world.spawnParticle(Particle.FLASH, locB.add(0, 1, 0), 1);
        world.spawnParticle(Particle.END_ROD, locA, 30, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.END_ROD, locB, 30, 0.5, 0.5, 0.5, 0.1);
        
        world.playSound(locA, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);
        world.playSound(locB, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.0f);
        world.playSound(locA, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.5f);
        world.playSound(locB, Sound.BLOCK_BEACON_POWER_SELECT, 0.8f, 1.5f);
        
        String msgA = isSelfInvolved ? 
            "§e§lSWAP! §r§fYou traded places with §e" + targetB.getName() : 
            "§e§lSWAP! §r§fYou traded places with §e" + targetB.getName();
        String msgB = isSelfInvolved ? 
            "§e§lSWAP! §r§fYou traded places with §e" + user.getName() : 
            "§e§lSWAP! §r§fYou traded places with §e" + targetA.getName();
        
        targetA.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(msgA));
        targetB.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(msgB));
        
        clearMark(user);
        applyCooldown(user.getUniqueId());
        
        targetA.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10, 4, false, false, false));
        targetB.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10, 4, false, false, false));
    }
    
    private Player findTargetPlayer(Player player) {
        Location eyeLoc = player.getEyeLocation();
        Vector lookDir = eyeLoc.getDirection().normalize();
        
        Player closest = null;
        double closestScore = Double.MAX_VALUE;
        
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) continue;
            if (target.getGameMode() == GameMode.SPECTATOR) continue;
            if (!target.getWorld().equals(player.getWorld())) continue;
            
            double distance = target.getLocation().distance(eyeLoc);
            
            if (distance < MIN_MARK_RANGE || distance > MAX_MARK_RANGE) continue;
            
            Location targetEyeLoc = target.getEyeLocation();
            Vector toTarget = targetEyeLoc.toVector().subtract(eyeLoc.toVector());
            double distToTarget = toTarget.length();
            toTarget.normalize();
            
            double angle = lookDir.angle(toTarget);
            double maxAngle = calculateMaxAngle(distance);
            
            if (angle > maxAngle) continue;
            
            double score = angle + (distance * 0.01);
            
            if (score < closestScore) {
                closestScore = score;
                closest = target;
            }
        }
        
        return closest;
    }
    
    /**
     * Dynamic cone: Tight (12°) at close range, lenient (35°) at long range
     * This makes it obvious who you're clicking up close, but forgiving at distance
     */
    private double calculateMaxAngle(double distance) {
        if (distance <= LENIENCY_START_DISTANCE) {
            return BASE_CONE_ANGLE; // 12 degrees - precise up close
        }
        
        if (distance >= MAX_LENIENCY_DISTANCE) {
            return MAX_CONE_ANGLE; // 35 degrees - lenient far away
        }
        
        double t = (distance - LENIENCY_START_DISTANCE) / (MAX_LENIENCY_DISTANCE - LENIENCY_START_DISTANCE);
        return BASE_CONE_ANGLE + (MAX_CONE_ANGLE - BASE_CONE_ANGLE) * t;
    }
    
    private void showTargetingCone(Player player, Player target) {
        Location eyeLoc = player.getEyeLocation();
        Vector lookDir = eyeLoc.getDirection().normalize();
        World world = player.getWorld();
        
        int rings = 12;
        double maxDist = 20;
        
        for (int r = 1; r <= rings; r++) {
            double distance = (maxDist / rings) * r;
            double maxAngle = calculateMaxAngle(distance);
            double radiusAtDist = distance * Math.tan(maxAngle);
            
            int points = Math.max(8, (int)(radiusAtDist * 4));
            
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                
                Vector perp1 = getPerpendicular(lookDir);
                Vector perp2 = lookDir.clone().crossProduct(perp1).normalize();
                
                double x = Math.cos(angle) * radiusAtDist;
                double y = Math.sin(angle) * radiusAtDist;
                
                Vector offset = perp1.clone().multiply(x).add(perp2.clone().multiply(y));
                Location particleLoc = eyeLoc.clone().add(lookDir.clone().multiply(distance)).add(offset);
                
                Color color = (i % 4 == 0) ? CONE_CENTER_COLOR : CONE_COLOR;
                float size = 0.8f - (float)(distance / maxDist * 0.3f);
                
                world.spawnParticle(Particle.DUST, particleLoc, 1, 
                    new Particle.DustOptions(color, size));
            }
        }
        
        for (int i = 0; i < 20; i++) {
            double t = i / 20.0;
            Location lineLoc = eyeLoc.clone().add(lookDir.clone().multiply(maxDist * t));
            world.spawnParticle(Particle.DUST, lineLoc, 1, 
                new Particle.DustOptions(CONE_CENTER_COLOR, 1.0f));
        }
        
        if (target != null) {
            world.spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 15, 
                0.3, 0.5, 0.3, new Particle.DustOptions(Color.LIME, 1.5f));
        }
    }
    
    private Vector getPerpendicular(Vector v) {
        Vector perp = new Vector(1, 0, 0);
        if (Math.abs(v.getX()) > 0.9) {
            perp = new Vector(0, 1, 0);
        }
        perp = v.clone().crossProduct(perp).normalize();
        if (perp.length() < 0.001) {
            perp = new Vector(0, 0, 1);
            perp = v.clone().crossProduct(perp).normalize();
        }
        return perp;
    }
    
    private void spawnSwapTrail(Location from, Location to, Color colorA, Color colorB) {
        World world = from.getWorld();
        if (!to.getWorld().equals(world)) return;
        
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();
        
        int particles = (int) (distance * 2);
        
        for (int i = 0; i <= particles; i++) {
            double t = (double) i / particles;
            Location loc = from.clone().add(direction.clone().multiply(distance * t));
            
            Color blended = blendColors(colorA, colorB, t);
            
            world.spawnParticle(Particle.DUST, loc.add(0, 1, 0), 2, 
                0.1, 0.1, 0.1, new Particle.DustOptions(blended, 1.2f));
            
            if (i % 3 == 0) {
                world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
            }
        }
    }
    
    private Color blendColors(Color a, Color b, double t) {
        int red = (int) (a.getRed() * (1-t) + b.getRed() * t);
        int green = (int) (a.getGreen() * (1-t) + b.getGreen() * t);
        int blue = (int) (a.getBlue() * (1-t) + b.getBlue() * t);
        return Color.fromRGB(red, green, blue);
    }
    
    private void clearMark(Player player) {
        UUID uuid = player.getUniqueId();
        MarkData mark = playerMarks.remove(uuid);
        
        if (mark != null && !mark.isSelfMark) {
            Player target = Bukkit.getPlayer(mark.targetUuid);
            if (target != null && target.isOnline()) {
                target.removePotionEffect(PotionEffectType.GLOWING);
            }
        }
        
        if (mark != null && mark.isSelfMark) {
            player.removePotionEffect(PotionEffectType.GLOWING);
        }
    }
    
    public void clearPlayerData(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            clearMark(player);
        } else {
            playerMarks.remove(uuid);
        }
    }
    
    public void onDisable() {
        for (Map.Entry<UUID, MarkData> entry : playerMarks.entrySet()) {
            Player marker = Bukkit.getPlayer(entry.getKey());
            if (marker != null) {
                marker.removePotionEffect(PotionEffectType.GLOWING);
            }
            
            if (!entry.getValue().isSelfMark) {
                Player target = Bukkit.getPlayer(entry.getValue().targetUuid);
                if (target != null) {
                    target.removePotionEffect(PotionEffectType.GLOWING);
                }
            }
        }
        playerMarks.clear();
    }
    
    @Override
    public long getBaseCooldown() {
        return 500L; // 0.5 seconds
    }
    
    @Override
    public String getId() {
        return "boogiewoogie";
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
        return "§dBoogie Woogie";
    }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§dShift+RClick to mark a player (or look down for self)",
            "§dRClick to mark second player and §l§nSWAP§d their positions",
            "§dLenient targeting at range, precise up close",
            "",
            "§5Castable",
            "§d§obeangame"
        );
    }
    
    @Override
    public Material getMaterial() {
        return Material.MUSIC_DISC_CAT;
    }
    
    @Override
    public int getCustomModelData() {
        return 0;
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
    }
    
    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }
    
    @Override
    public Color getColor() {
        return null;
    }
    
    @Override
    public int getArmor() {
        return 0;
    }
    
    @Override
    public EquipmentSlotGroup getSlot() {
        return null;
    }
    
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean getRightClickAnimation(){
        return false;
    }
}