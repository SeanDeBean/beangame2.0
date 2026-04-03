package com.beangamecore.items;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import com.beangamecore.util.ItemNBT;

public class ItsJeff extends BeangameItem implements BGRClickableI {
    
    private static HashMap<UUID, UUID> playerToJeff = new HashMap<>();
    private static final double RADIUS = 4.75;
    private static final Long CAST_TIME = 35L;
    private static final Long HOLD_TIME = 100L;
    private static final int PARTICLE_TASK_MAX_COUNT = 7;
    private static final double SOUND_CHANCE_THRESHOLD = 0.35;
    private static final double HURT_SOUND_CHANCE = 0.8;

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player jeff = event.getPlayer();
        UUID jeffUuid = jeff.getUniqueId();

        if (onCooldown(jeffUuid)) {
            sendCooldownMessage(jeff);
            return false;
        }
        applyCooldown(jeffUuid);

        JeffAbilityExecutor executor = new JeffAbilityExecutor(jeffUuid);
        executor.execute();
        
        return true;
    }

    private static class JeffAbilityExecutor {
        private final UUID jeffUuid;
        
        public JeffAbilityExecutor(UUID jeffUuid) {
            this.jeffUuid = jeffUuid;
        }
        
        public void execute() {
            startParticleTask();
            scheduleCastExecution();
            scheduleThrowExecution();
            startMonitoringTask();
        }
        
        private void startParticleTask() {
            AtomicInteger particleCount = new AtomicInteger(0);
            int[] taskId = new int[1];
            
            taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
                int count = particleCount.incrementAndGet();
                Player jeff = Bukkit.getServer().getPlayer(jeffUuid);
                
                if (shouldCancelParticleTask(jeff, count)) {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                
                spawnParticleEffects(jeff);
                playParticleSounds(jeff, count);
            }, 1L, 6L).getTaskId();
        }
        
        private boolean shouldCancelParticleTask(Player jeff, int count) {
            return jeff == null || jeff.isDead() || jeff.getGameMode() == GameMode.SPECTATOR 
                || count >= PARTICLE_TASK_MAX_COUNT;
        }
        
        private void spawnParticleEffects(Player jeff) {
            DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 191, 255), 2);
            Main.getPlugin().getParticleManager().spawnParticleSphere(
                jeff.getLocation(), RADIUS, Particle.DUST, dustOptions, 250);
        }
        
        private void playParticleSounds(Player jeff, int count) {
            if (Math.random() > SOUND_CHANCE_THRESHOLD && count < 6) {
                jeff.getWorld().playSound(jeff.getLocation(), Sound.ENTITY_GUARDIAN_FLOP, 2, 1);
            } else if (count >= 6) {
                jeff.getWorld().playSound(jeff.getLocation(), Sound.ENTITY_PANDA_EAT, 1, 2);
            }
        }
        
        private void scheduleCastExecution() {
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                Player jeff = Bukkit.getServer().getPlayer(jeffUuid);
                if (!isJeffValid(jeff)) return;
                    
                captureNearbyPlayers(jeff);
            }, CAST_TIME);
        }
        
        private void captureNearbyPlayers(Player jeff) {
            World world = jeff.getWorld();
            Location loc = jeff.getLocation();
            
            for (Entity entity : world.getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
                if (entity instanceof Player target) {
                    if (shouldSkipPlayer(target, jeff)) continue;
                    if (isPlayerProtected(target)) continue;
                    
                    capturePlayer(target, jeffUuid);
                }
            }
        }
        
        private boolean shouldSkipPlayer(Player target, Player jeff) {
            if (target.equals(jeff) || target.getGameMode() == GameMode.CREATIVE) {
                return true;
            }
            
            return target.getGameMode().equals(GameMode.SPECTATOR) 
                && !playerToJeff.containsKey(target.getUniqueId());
        }
        
        private boolean isPlayerProtected(Player target) {
            for (ItemStack item : target.getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) continue;
                
                if (ItemNBT.hasBeanGameTag(item) 
                    && ItemNBT.isBeanGame(item, NamespacedKey.fromString("beangame:superstar"))) {
                    return true;
                }
            }
            return false;
        }
        
        private void capturePlayer(Player target, UUID jeffUuid) {
            UUID targetUUID = target.getUniqueId();
            playerToJeff.put(targetUUID, jeffUuid);
            if(!Revive.noRevive.contains(targetUUID)) Revive.noRevive.add(targetUUID);
            target.setGameMode(GameMode.SPECTATOR);
            
            Player jeff = Bukkit.getServer().getPlayer(jeffUuid);
            if (jeff != null) {
                target.setSpectatorTarget(jeff);
            }
        }
        
        private void scheduleThrowExecution() {
            
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                Player jeff = Bukkit.getServer().getPlayer(jeffUuid);
                if (!isJeffValid(jeff)) return;
                    
                throwCapturedPlayers(jeff);
                cleanupJeffEffects(jeff);
            }, CAST_TIME + HOLD_TIME);
        }
        
        private void throwCapturedPlayers(Player jeff) {
            Location jeffLoc = jeff.getLocation();
            Vector throwDirection = jeff.getEyeLocation().getDirection().multiply(2);
            
            Iterator<Map.Entry<UUID, UUID>> iterator = playerToJeff.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, UUID> entry = iterator.next();
                if (!entry.getValue().equals(jeffUuid)) continue;
                
                Player target = Bukkit.getServer().getPlayer(entry.getKey());
                if (target != null && target.getGameMode() == GameMode.SPECTATOR) {
                    releaseAndThrowPlayer(target, jeffLoc, throwDirection);
                    iterator.remove();
                }
            }
            
            jeffLoc.getWorld().playSound(jeffLoc, Sound.ENTITY_LLAMA_SPIT, 1, 1);
        }
        
        private void releaseAndThrowPlayer(Player target, Location jeffLoc, Vector throwDirection) {
            target.setGameMode(GameMode.SURVIVAL);
            
            Location spawnLoc = calculateSpawnLocation(jeffLoc);

            Revive.noRevive.remove(target.getUniqueId());

            target.teleport(spawnLoc);

            if(target.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        target.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7){
                return;
            }

            target.setVelocity(throwDirection);
            
        }
        
        private Location calculateSpawnLocation(Location jeffLoc) {
            Location spawnLoc = jeffLoc.clone().add(jeffLoc.getDirection().normalize().multiply(0.75));
            Vector directionToJeff = jeffLoc.toVector().subtract(spawnLoc.toVector()).normalize();
            spawnLoc.setDirection(directionToJeff);
            return spawnLoc;
        }
        
        private void cleanupJeffEffects(Player jeff) {
            jeff.removePotionEffect(PotionEffectType.SLOWNESS);
            Cooldowns.setCooldown("attack", jeff.getUniqueId(), 0);
        }
        
        private void startMonitoringTask() {
            int[] taskId = new int[1];
            
            taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
                Player jeff = Bukkit.getServer().getPlayer(jeffUuid);
                
                if (shouldReleaseAllPlayers(jeff)) {
                    releaseAllCapturedPlayers();
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                    return;
                }
                
                if (updateCapturedPlayers(jeff)) {
                    applyJeffEffects(jeff);
                    playHurtSound(jeff);
                } else {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                }
            }, CAST_TIME + 1, 1L).getTaskId();
        }
        
        private boolean shouldReleaseAllPlayers(Player jeff) {
            return jeff == null || jeff.isDead() || jeff.getGameMode() == GameMode.SPECTATOR;
        }
        
        private void releaseAllCapturedPlayers() {
            Iterator<Map.Entry<UUID, UUID>> iterator = playerToJeff.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, UUID> entry = iterator.next();
                if (!entry.getValue().equals(jeffUuid)) continue;
                
                Player target = Bukkit.getServer().getPlayer(entry.getKey());
                if (target != null && target.getGameMode() == GameMode.SPECTATOR) {
                    target.setGameMode(GameMode.SURVIVAL);
                }
                Revive.noRevive.remove(target.getUniqueId());
                iterator.remove();
            }
        }
        
        private boolean updateCapturedPlayers(Player jeff) {
            boolean hasPlayers = false;
            
            Iterator<Map.Entry<UUID, UUID>> iterator = playerToJeff.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, UUID> entry = iterator.next();
                if (!entry.getValue().equals(jeffUuid)) continue;
                
                Player target = Bukkit.getServer().getPlayer(entry.getKey());
                if (target != null && target.getGameMode() == GameMode.SPECTATOR) {
                    target.setSpectatorTarget(jeff);
                    hasPlayers = true;
                }
            }
            
            return hasPlayers;
        }
        
        private void applyJeffEffects(Player jeff) {
            int playerCount = getCapturedPlayerCount();
            if (playerCount >= 1) {
                jeff.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, playerCount - 1));
                Cooldowns.setCooldown("attack", jeff.getUniqueId(), 5000);
            }
        }
        
        private int getCapturedPlayerCount() {
            int count = 0;
            for (Map.Entry<UUID, UUID> entry : playerToJeff.entrySet()) {
                if (entry.getValue().equals(jeffUuid)) {
                    count++;
                }
            }
            return count;
        }
        
        private void playHurtSound(Player jeff) {
            if (Math.random() > HURT_SOUND_CHANCE) {
                jeff.getWorld().playSound(jeff.getLocation(), Sound.ENTITY_PLAYER_HURT_DROWN, 0.75F, 1);
            }
        }

        private boolean isJeffValid(Player jeff) {
            return jeff != null && jeff.getGameMode() == GameMode.SURVIVAL;
        }
    }

    @Override
    public long getBaseCooldown() {
        return 65000L;
    }

    @Override
    public String getId() {
        return "itsjeff";
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
        return "§9IT'S JEFF!";
    }

    public List<String> getLore() {
        return List.of(
            "§9Right-click to capture all players",
            "§9within 4.75 blocks after 1.75 seconds.",
            "§9Captured players become spectators",
            "§9for 5 seconds then get thrown forward.",
            "§9You gain slowness based on players captured.",
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
        return Material.COD;
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

