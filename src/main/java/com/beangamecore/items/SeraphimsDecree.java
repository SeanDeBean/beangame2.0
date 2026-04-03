package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.util.Cooldowns;

public class SeraphimsDecree extends BeangameItem implements BGLClickableI, BGMPTalismanI, BGDDealerHeldI {

    private final Map<UUID, Integer> chargedStacks = new HashMap<>();
    private final Map<UUID, Integer> heavenfallCount = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> isAirborne = new ConcurrentHashMap<>();
    private final Map<UUID, Long> airborneStartTime = new ConcurrentHashMap<>();

    private static final int MAX_CHARGED_STACKS = 5;
    private static final int CHAIN_BASE_TARGETS = 3;
    private static final double CHAIN_RANGE = 8.0;
    private static final double CHAIN_DAMAGE = 2.0;
    private static final double CHAIN_DAMAGE_REDUCTION = 0.2; // 20% reduction per chain
    private static final int HEAVENFALL_REQUIRED = 3;
    private static final int HEAVENFALL_COMBO_REQUIRED = 3;
    private static final double HEAVENFALL_LAUNCH_Y = 0.65;
    private static final double HEAVENFALL_LAUNCH_FORWARD = 0.5;
    private static final long AIRBORNE_THRESHOLD = 3000; // 3 seconds in milliseconds
    private static final double AOE_RADIUS = 5.0;
    private static final int AOE_STUN_DURATION = 20; // 3 seconds in ticks
    private static final double AOE_DAMAGE = 4.0;

    private static final Color GOLD_COLOR = Color.fromRGB(255, 215, 0);
    private static final Color WHITE_COLOR = Color.fromRGB(255, 255, 255);
    private static final Color BLUE_COLOR = Color.fromRGB(135, 206, 250);
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();

        if(Math.random() < 0.55)decayChargedStacks(player);

        showChargedIndicator(player);
        
        // Add aura effect based on charged level
        showChargedAura(player);

        if(chargedStacks.getOrDefault(uuid, 0) >= MAX_CHARGED_STACKS) {
            applyHeavenlyGrace(player);
        }

        trackAirborneState(player);
    }

    private void showChargedAura(Player player) {
        UUID uuid = player.getUniqueId();
        int stacks = chargedStacks.getOrDefault(uuid, 0);
        
        if (stacks > 0) {
            Location loc = player.getLocation().add(0, 0.5, 0);
            World world = player.getWorld();
            
            // Aura effect around player
            double radius = 1.0 + (stacks * 0.1);
            int particles = 8 + (stacks * 2);
            
            for (int i = 0; i < particles; i++) {
                double angle = 2 * Math.PI * i / particles;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                Location particleLoc = loc.clone().add(x, 0, z);
                
                // Color based on stacks
                Color auraColor;
                if (stacks >= MAX_CHARGED_STACKS) {
                    auraColor = GOLD_COLOR;
                } else if (stacks >= HEAVENFALL_REQUIRED) {
                    auraColor = WHITE_COLOR;
                } else {
                    auraColor = BLUE_COLOR;
                }
                
                // Glowing aura particles
                world.spawnParticle(Particle.DUST, particleLoc, 1,
                    new Particle.DustOptions(auraColor, 1.5f));
                
                // Glow effect for high stacks
                if (stacks >= HEAVENFALL_REQUIRED && i % 2 == 0) {
                    world.spawnParticle(Particle.GLOW, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                }
            }
            
            // Rising particles for visual effect
            if (stacks >= HEAVENFALL_REQUIRED) {
                for (int i = 0; i < 2; i++) {
                    double offsetX = (Math.random() - 0.5) * radius;
                    double offsetZ = (Math.random() - 0.5) * radius;
                    
                    Location riseLoc = loc.clone().add(offsetX, 0.1, offsetZ);
                    for (double y = 0; y < 2; y += 0.3) {
                        world.spawnParticle(Particle.DUST, riseLoc.clone().add(0, y, 0), 1,
                            new Particle.DustOptions(WHITE_COLOR, 0.8f));
                    }
                }
            }
        }
    }

    private void decayChargedStacks(Player player) {
        UUID uuid = player.getUniqueId();
        int currentStacks = chargedStacks.getOrDefault(uuid, 0);
        
        if (currentStacks > 0) {
            chargedStacks.put(uuid, currentStacks - 1);
        }
    }

    private void showChargedIndicator(Player player) {
        UUID uuid = player.getUniqueId();
        int stacks = chargedStacks.getOrDefault(uuid, 0);

        if(stacks > 0){
            Location loc = player.getLocation().add(0, 2.2, 0);
            World world = player.getWorld();

            long time = System.currentTimeMillis();
            double baseAngle = (time % 2000) / 2000.0 * Math.PI * 2;

            for(int i = 0; i < stacks; i++) {
                double angle = baseAngle + (2 * Math.PI * i / Math.max(1, stacks));
                double radius = 0.5 + (stacks * 0.1);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                Location particleLoc = loc.clone().add(x, 0, z);

                Color particleColor;
                if(stacks >= MAX_CHARGED_STACKS) {
                    particleColor = GOLD_COLOR;
                    world.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0, 0, 0, 0);
                } else if(stacks >= HEAVENFALL_REQUIRED) {
                    particleColor = WHITE_COLOR;
                } else {
                    particleColor = BLUE_COLOR;
                }

                world.spawnParticle(Particle.DUST, particleLoc, 1, new Particle.DustOptions(particleColor, 1.0f));

                if(stacks >= HEAVENFALL_REQUIRED && i % 2 == 0) {
                    world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private void applyHeavenlyGrace(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        for (int i = 0; i < 3; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = 1.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
                
            Location particleLoc = loc.clone().add(x, 1.0 + (Math.random() * 0.5), z);
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private void trackAirborneState(Player player) {
        UUID uuid = player.getUniqueId();
        boolean wasAirborne = isAirborne.getOrDefault(uuid, false);
        boolean currentlyAirborne = !player.isOnGround();

        if(!wasAirborne && currentlyAirborne) {
            airborneStartTime.put(uuid, System.currentTimeMillis());
            isAirborne.put(uuid, true);
        } else if(wasAirborne && !currentlyAirborne) {
            isAirborne.put(uuid, false);
            checkHeavenfallCombo(player);
        }
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if (!(event.getDamager() instanceof Player player)) return;
        
        UUID uuid = player.getUniqueId();
        LivingEntity target = (LivingEntity) event.getEntity();

        if (target != null && !target.equals(player) && !(target instanceof ArmorStand)) {
            int currentStacks = chargedStacks.getOrDefault(uuid, 0);
            int newStacks = Math.min(MAX_CHARGED_STACKS, currentStacks + 1);
            chargedStacks.put(uuid, newStacks);
            
            // Visual feedback for gaining stacks
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.3f, 0.8f + (newStacks * 0.1f));
        }
    }

    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Check cooldown
        if (onCooldown(uuid)) {
            return;
        }
        
        // Check for targets in range first
        LivingEntity initialTarget = findTargetInDirection(player, player.getEyeLocation(), 
            player.getLocation().getDirection(), CHAIN_RANGE);
        
        if (initialTarget == null) {
            // No targets in range
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.2f);
            return;
        }
        
        applyCooldown(uuid);
        
        int charged = chargedStacks.getOrDefault(uuid, 0);
        
        if (charged >= HEAVENFALL_REQUIRED) {
            activateHeavenfall(player, initialTarget);
        } else {
            castJudgement(player);
        }
    }

    private void castJudgement(Player player) {
        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();
        World world = player.getWorld();
        
        // Initial sound
        world.playSound(startLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.7f, 1.8f);
        world.playSound(startLoc, Sound.ITEM_TRIDENT_THUNDER, 0.5f, 1.5f);
        
        // Find initial target
        LivingEntity initialTarget = findTargetInDirection(player, startLoc, direction, CHAIN_RANGE);
        
        if (initialTarget != null) {
            // Draw lightning from player to first target
            drawZigZagLightningBolt(player.getEyeLocation(), 
                                initialTarget.getLocation().add(0, 1, 0), 
                                world);
            
            // Chain lightning effect with delay
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                chainLightning(player, initialTarget, CHAIN_BASE_TARGETS, CHAIN_DAMAGE, new HashSet<>());
            }, 1L);
        } else {
            // Miss feedback
            world.playSound(startLoc, Sound.BLOCK_FIRE_EXTINGUISH, 0.4f, 1.2f);
        }
    }

    private LivingEntity findTargetInDirection(Player player, Location start, Vector direction, double range) {
        // Check for any living entity in a cone in front of the player
        for (double d = 1; d <= range; d += 0.5) {
            Location checkLoc = start.clone().add(direction.clone().multiply(d));
            
            // Check in a small sphere around the check location
            for (Entity entity : checkLoc.getWorld().getNearbyEntities(checkLoc, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity living && 
                    !entity.equals(player) && 
                    !(entity instanceof ArmorStand) &&
                    !living.isDead() &&
                    !(entity instanceof Player sp && sp.getGameMode() == GameMode.SPECTATOR)) {
                    return living;
                }
            }
        }
        return null;
    }

    private void chainLightning(Player source, LivingEntity currentEntity, int maxTargets, double baseDamage, Set<UUID> alreadyHit) {
        if (alreadyHit.contains(currentEntity.getUniqueId()) || alreadyHit.size() >= maxTargets) {
            return;
        }
        
        alreadyHit.add(currentEntity.getUniqueId());
        World world = currentEntity.getWorld();
        Location entityLoc = currentEntity.getLocation();
        
        // Apply damage with reduction based on chain length
        double damage = baseDamage * Math.pow(1 - CHAIN_DAMAGE_REDUCTION, alreadyHit.size() - 1);
        currentEntity.damage(damage, source);
        
        // Visual effects
        world.playSound(entityLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.6f, 1.5f);
        
        // Lightning particle effect at target
        for (int i = 0; i < 3; i++) {
            world.spawnParticle(Particle.ELECTRIC_SPARK, entityLoc.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0);
        }
        
        // Find next target for chain FROM THE CURRENT ENTITY
        if (alreadyHit.size() < maxTargets) {
            LivingEntity nextTarget = findNextChainTarget(source, currentEntity, alreadyHit, CHAIN_RANGE);
            if (nextTarget != null) {
                // Draw ZIG-ZAG lightning bolt between entities
                drawZigZagLightningBolt(entityLoc.clone().add(0, 1, 0), 
                                    nextTarget.getLocation().clone().add(0, 1, 0), 
                                    world);
                
                // Recursively chain with small delay - pass a NEW Set with same elements
                final Set<UUID> nextAlreadyHit = new HashSet<>(alreadyHit);
                final LivingEntity finalNextTarget = nextTarget;

                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    chainLightning(source, finalNextTarget, maxTargets, baseDamage, nextAlreadyHit);
                }, 2L);
            }
        }
    }

    private LivingEntity findNextChainTarget(Player sourcePlayer, LivingEntity fromEntity, Set<UUID> alreadyHit, double range) {
        Location fromLoc = fromEntity.getLocation();
        List<LivingEntity> possibleTargets = new ArrayList<>();
        
        for (Entity entity : fromLoc.getWorld().getNearbyEntities(fromLoc, range, range, range)) {
            if (entity instanceof LivingEntity living && 
                !alreadyHit.contains(living.getUniqueId()) &&
                !(entity instanceof ArmorStand) &&
                !living.isDead() &&
                !entity.equals(fromEntity) &&
                !(entity instanceof Player sp && sp.getGameMode() == GameMode.SPECTATOR)) {
                
                // CRITICAL FIX: Don't chain back to the source player
                if (entity.equals(sourcePlayer)) {
                    continue; // Skip the source player
                }
                
                possibleTargets.add(living);
            }
        }
        
        // Return the closest target
        if (!possibleTargets.isEmpty()) {
            LivingEntity closest = possibleTargets.get(0);
            double closestDistance = closest.getLocation().distance(fromLoc);
            
            for (LivingEntity target : possibleTargets) {
                double distance = target.getLocation().distance(fromLoc);
                if (distance < closestDistance) {
                    closest = target;
                    closestDistance = distance;
                }
            }
            return closest;
        }
        
        return null;
    }

    private void drawZigZagLightningBolt(Location start, Location end, World world) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();
        
        // Create multiple jagged segments for zig-zag effect
        int segments = 6; // Reduced for better visibility
        List<Location> points = new ArrayList<>();
        points.add(start);
        
        // Create zig-zag path
        for (int i = 1; i < segments; i++) {
            double progress = (double) i / segments;
            Location basePoint = start.clone().add(direction.clone().multiply(distance * progress));
            
            // Add zig-zag offset perpendicular to the direction
            Vector perpendicular;
            if (Math.abs(direction.getY()) < 0.9) {
                perpendicular = direction.clone().crossProduct(new Vector(0, 1, 0)).normalize();
            } else {
                perpendicular = direction.clone().crossProduct(new Vector(1, 0, 0)).normalize();
            }
            
            // Alternate zig-zag direction
            double zigZagAmount = 0.3 * Math.sin(progress * Math.PI * 4); // Reduced amount
            basePoint.add(perpendicular.clone().multiply(zigZagAmount));
            
            points.add(basePoint);
        }
        points.add(end);
        
        // Draw particles between all points
        for (int i = 0; i < points.size() - 1; i++) {
            Location p1 = points.get(i);
            Location p2 = points.get(i + 1);
            
            Vector segDirection = p2.toVector().subtract(p1.toVector());
            double segDistance = segDirection.length();
            segDirection.normalize();
            
            int segParticles = Math.max(3, (int) (segDistance * 6));
            
            for (int j = 0; j <= segParticles; j++) {
                double segProgress = (double) j / segParticles;
                Location particleLoc = p1.clone().add(segDirection.clone().multiply(segDistance * segProgress));
                
                // Main lightning particles - use ELECTRIC_SPARK which is very visible
                world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 2, 0, 0, 0, 0);
                
                // Blue trail particles
                if (j % 2 == 0) {
                    world.spawnParticle(Particle.DUST, particleLoc, 1,
                        new Particle.DustOptions(BLUE_COLOR, 1.0f));
                }
            }
        }
    }

    private void activateHeavenfall(Player player, LivingEntity initial) {
        UUID uuid = player.getUniqueId();
        World world = player.getWorld();
        Location loc = player.getLocation();
        
        // Launch player
        Vector launchVector = new Vector(
            loc.getDirection().getX() * HEAVENFALL_LAUNCH_FORWARD,
            HEAVENFALL_LAUNCH_Y,
            loc.getDirection().getZ() * HEAVENFALL_LAUNCH_FORWARD
        );

        if(initial instanceof Player) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 30, 0));
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0));

        if(!(player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
            player.setVelocity(launchVector);
            Cooldowns.setCooldown("fall_damage_immunity", player.getUniqueId(), 2000L);
        }
        
        // Increment Heavenfall combo counter
        int currentCount = heavenfallCount.getOrDefault(uuid, 0) + 1;
        heavenfallCount.put(uuid, currentCount);
        
        // Sound effects
        world.playSound(loc, Sound.ITEM_TRIDENT_THROW, 1.0f, 0.7f);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.5f);
        
        // Launch particles
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            double radius = 1.2;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = loc.clone().add(x, 0.1, z);
            world.spawnParticle(Particle.CLOUD, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
            world.spawnParticle(Particle.DUST, particleLoc, 1,
                new Particle.DustOptions(WHITE_COLOR, 1.2f));
        }
        
        int[] taskId = new int[1];
        int[] tick = {0};

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (tick[0] >= 20 || player.isOnGround()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            
            // Create wing particles
            Location leftWing = loc.clone().add(-0.8, 0.5, 0);
            Location rightWing = loc.clone().add(0.8, 0.5, 0);
            
            for (int i = 0; i < 3; i++) {
                world.spawnParticle(Particle.DUST, leftWing, 1,
                    new Particle.DustOptions(WHITE_COLOR, 1.0f));
                world.spawnParticle(Particle.DUST, rightWing, 1,
                    new Particle.DustOptions(WHITE_COLOR, 1.0f));
                
                // Feather particles
                if (tick[0] % 5 == 0) {
                    world.spawnParticle(Particle.FALLING_DUST, 
                        leftWing.clone().add(Math.random() - 0.5, 0, Math.random() - 0.5),
                        1, Material.WHITE_WOOL.createBlockData());
                    world.spawnParticle(Particle.FALLING_DUST,
                        rightWing.clone().add(Math.random() - 0.5, 0, Math.random() - 0.5),
                        1, Material.WHITE_WOOL.createBlockData());
                }
            }
            
            tick[0]++;
        }, 0L, 1L).getTaskId();
        
        // Chain lightning from launch position (enhanced)
        LivingEntity initialTarget = findTargetInDirection(player, loc, player.getLocation().getDirection(), CHAIN_RANGE * 1.5);
        if (initialTarget != null) {
            initialTarget.damage(CHAIN_DAMAGE * 1.5, player);
            drawZigZagLightningBolt(player.getLocation().add(0, 1, 0), 
                                initialTarget.getLocation().add(0, 1, 0), 
                                world);
        }
    }

    private void checkHeavenfallCombo(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (heavenfallCount.getOrDefault(uuid, 0) >= HEAVENFALL_COMBO_REQUIRED) {
            Long startTime = airborneStartTime.get(uuid);
            if (startTime != null && System.currentTimeMillis() - startTime >= AIRBORNE_THRESHOLD) {
                // Successful Heavenfall combo - trigger AOE
                triggerHeavenfallImpact(player);
                chargedStacks.put(uuid, 0); // Reset charged stacks after Heavenfall
            }
        }
        
        // Reset combo counter
        heavenfallCount.put(uuid, 0);
        airborneStartTime.remove(uuid);
    }

    private void triggerHeavenfallImpact(Player player) {
        Location impactLoc = player.getLocation();
        World world = player.getWorld();
        
        // Major sound effects
        world.playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
        world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
        world.playSound(impactLoc, Sound.BLOCK_BELL_USE, 0.8f, 0.5f);
        
        // Shockwave effect
        for (int ring = 0; ring < 4; ring++) {
            final int currentRing = ring;

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                double radius = (currentRing + 1) * (AOE_RADIUS / 4);
                    
                // Ring particles
                for (int i = 0; i < 24; i++) {
                    double angle = 2 * Math.PI * i / 24;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location particleLoc = impactLoc.clone().add(x, 0.1, z);
                        
                    // Alternating colors
                    Color ringColor = (i % 2 == 0) ? GOLD_COLOR : WHITE_COLOR;
                    world.spawnParticle(Particle.DUST, particleLoc, 1,
                        new Particle.DustOptions(ringColor, 2.0f));
                    
                    // Spark particles
                    if (currentRing == 3) {
                        world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                // Apply effects on final ring
                if (currentRing == 3) {
                    for (Entity entity : world.getNearbyEntities(impactLoc, AOE_RADIUS, AOE_RADIUS, AOE_RADIUS)) {
                        if (entity instanceof LivingEntity living && 
                            !entity.equals(player) && 
                            !(entity instanceof ArmorStand)) {
                            
                            // Stun effect
                            if(living instanceof Player targetPlayer) {
                                Cooldowns.setCooldown("immobilized", targetPlayer.getUniqueId(), AOE_STUN_DURATION * 50L);
                            } else {
                                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, AOE_STUN_DURATION, 4));
                            }
                            
                            // Damage
                            double distance = entity.getLocation().distance(impactLoc);
                            double damageMultiplier = 1.0 - (distance / AOE_RADIUS);
                            living.damage(AOE_DAMAGE * damageMultiplier, player);
                            
                            // Launch entities away
                            Vector knockback = entity.getLocation().toVector()
                                .subtract(impactLoc.toVector())
                                .normalize()
                                .multiply(1.5)
                                .setY(0.5);
                            living.setVelocity(knockback);
                            
                        }
                    }
                        
                    // Center column effect
                    for (double y = 0; y < 4; y += 0.2) {
                        Location columnLoc = impactLoc.clone().add(0, y, 0);
                        world.spawnParticle(Particle.DRAGON_BREATH, columnLoc, 2, 0.1, 0.1, 0.1, 0.02);
                        
                        if (y % 1 == 0) {
                            world.spawnParticle(Particle.FIREWORK, columnLoc, 3, 0.2, 0.2, 0.2, 0);
                        }
                    }
                }
            }, ring * 3L);

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                double radius = (currentRing + 1) * (AOE_RADIUS / 4);
                    
                // Ring particles
                for (int i = 0; i < 24; i++) {
                    double angle = 2 * Math.PI * i / 24;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                        
                    Location particleLoc = impactLoc.clone().add(x, 0.1, z);
                    
                    // Alternating colors
                    Color ringColor = (i % 2 == 0) ? GOLD_COLOR : WHITE_COLOR;
                    world.spawnParticle(Particle.DUST, particleLoc, 1,
                        new Particle.DustOptions(ringColor, 2.0f));
                        
                    // Spark particles
                    if (currentRing == 3) {
                        world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0, 0, 0, 0);
                    }
                }
                    
                // Apply effects on final ring
                if (currentRing == 3) {
                    for (Entity entity : world.getNearbyEntities(impactLoc, AOE_RADIUS, AOE_RADIUS, AOE_RADIUS)) {
                        if (entity instanceof LivingEntity living && 
                            !entity.equals(player) && 
                            !(entity instanceof ArmorStand)) {
                                
                            // Stun effect
                            if(living instanceof Player targetPlayer) {
                                Cooldowns.setCooldown("immobilized", targetPlayer.getUniqueId(), AOE_STUN_DURATION * 50L);
                            } else {
                                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, AOE_STUN_DURATION, 4));
                            }
                                
                            // Damage
                            double distance = entity.getLocation().distance(impactLoc);
                            double damageMultiplier = 1.0 - (distance / AOE_RADIUS);
                            living.damage(AOE_DAMAGE * damageMultiplier, player);
                                
                            // Launch entities away
                            Vector knockback = entity.getLocation().toVector()
                                .subtract(impactLoc.toVector())
                                .normalize()
                                .multiply(1.5)
                                .setY(0.5);
                            living.setVelocity(knockback);
                                
                        }
                    }
                        
                    // Center column effect
                    for (double y = 0; y < 4; y += 0.2) {
                        Location columnLoc = impactLoc.clone().add(0, y, 0);
                        world.spawnParticle(Particle.DRAGON_BREATH, columnLoc, 2, 0.1, 0.1, 0.1, 0.02);
                            
                        if (y % 1 == 0) {
                            world.spawnParticle(Particle.FIREWORK, columnLoc, 3, 0.2, 0.2, 0.2, 0);
                        }
                    }
                }
            }, ring * 3L);
        }
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 1600L;
    }

    @Override
    public String getId() {
        return "seraphimsdecree";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§eSeraphim's Decree";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§dLeft-click shoots chaining lightning",
            "§dto nearby enemies, gaining Charged stacks.",
            "§dCharged decays 1 per second.",
            "§dAt 3+ Charged stacks, gains",
            "§da passive Heavenly Grace effect.",
            "",
            "§cOn Hit",
            "§dOn Hit Applier",
            "§fMovement",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 106;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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
