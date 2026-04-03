package com.beangamecore.items;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.general.BG1tTickingI;
import com.beangamecore.items.type.general.BGResetableI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.util.Cooldowns;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.joml.Quaternionf;

public class CarrionCall extends BeangameItem implements BGLClickableI, BGMPTalismanI, BG1tTickingI, BGResetableI {

    // Configuration constants
    private static final int MAX_BIRDS = 8;
    private static final int BIRD_SPAWN_INTERVAL_TICKS = 20 * 8; // 1 second base
    private static final int BIRD_SPAWN_FAST_INTERVAL_TICKS = 20 * 6;
    private static final int BIRD_LIFETIME_TICKS = 20*60; // 30 seconds
    private static final double BIRD_ORBIT_RADIUS_BASE = 4.5; // Increased by 2 from original 2.5
    private static final double BIRD_ATTACK_RANGE = 5.0;
    private static final int BLINDNESS_DURATION_TICKS = 10; // 1 second
    private static final int BLINDNESS_AMPLIFIER = 0;
    private static final double ATTACK_1_2_DAMAGE = 1.0;
    private static final double ATTACK_3_DAMAGE = 3.0;
    private static final double OUTWARD_VARIATION_CHANCE = 0.3; // 30% chance to fly further out
    private static final double OUTWARD_VARIATION_AMOUNT = 2.0; // Extra radius when varying
    
    // Visual colors
    private static final Color DARK_COLOR = Color.fromRGB(20, 20, 20);
    private static final Color PURPLE_COLOR = Color.fromRGB(138, 43, 226);
    
    // Global bird tracking - ALL birds across all players
    private static final List<BlackBird> allBirds = Collections.synchronizedList(new ArrayList<>());
    
    // Per-player tracking
    private final Map<UUID, Long> lastSpawnTime = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerBirdCount = new ConcurrentHashMap<>();

    private static final Map<UUID, Map<UUID, Long>> targetAttackCooldowns = new ConcurrentHashMap<>(); // targetUUID -> (birdOwnerUUID -> lastAttackTime)
    private static final Set<UUID> recentlyAttacked = ConcurrentHashMap.newKeySet();
    
    /**
     * Call this from your main plugin's tick handler (e.g., every tick or every 2 ticks)
     * Updates ALL birds globally, separate from talisman effect application
     */
    @Override
    public void tick() {
        synchronized (allBirds) {
            Iterator<BlackBird> iterator = allBirds.iterator();
            while (iterator.hasNext()) {
                BlackBird bird = iterator.next();
                
                // Check if bird should be removed
                if (bird.shouldRemove()) {
                    bird.explodeAndRemove();
                    iterator.remove();
                    continue;
                }
                
                // Tick the bird (movement and attacks)
                bird.tick();
            }
        }
    }
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        lastSpawnTime.putIfAbsent(uuid, 0L);
        
        // Count this player's current birds
        int currentBirds = 0;
        boolean hasPurpleAura = false;
        synchronized (allBirds) {
            for (BlackBird bird : allBirds) {
                if (bird.getOwnerUUID().equals(uuid)) {
                    currentBirds++;
                }
            }
        }
        
        // Check if we should show purple aura (high bird count)
        if (currentBirds >= 2 * MAX_BIRDS / 3) {
            hasPurpleAura = true;
        }
        
        playerBirdCount.put(uuid, currentBirds);
        
        // Determine spawn interval based on aura
        int spawnInterval = hasPurpleAura ? BIRD_SPAWN_FAST_INTERVAL_TICKS : BIRD_SPAWN_INTERVAL_TICKS;
        
        // Spawn new bird if under max and interval passed
        long lastSpawn = lastSpawnTime.get(uuid);
        if (currentBirds < MAX_BIRDS && currentTime - lastSpawn >= spawnInterval * 50) {
            spawnBird(player);
            lastSpawnTime.put(uuid, currentTime);
        }
        
        // Visual indicator of bird count (only purple and dark, no red)
        showBirdIndicator(player, currentBirds, hasPurpleAura);
    }
    
    private void spawnBird(Player player) {
        Location spawnLoc = player.getLocation().add(
            (Math.random() - 0.5) * 2,
            1 + Math.random(),
            (Math.random() - 0.5) * 2
        );
        
        BlackBird bird = new BlackBird(spawnLoc, player);
        synchronized (allBirds) {
            allBirds.add(bird);
        }
        
        // Spawn effect
        player.getWorld().playSound(spawnLoc, Sound.ENTITY_BAT_AMBIENT, 0.1f, 0.8f + (float)Math.random() * 0.4f);
        player.getWorld().spawnParticle(Particle.SMOKE, spawnLoc, 5, 0.2, 0.2, 0.2, 0.01);
    }
    
    private void showBirdIndicator(Player player, int count, boolean hasPurpleAura) {
        if (count <= 0) return;
        
        Location loc = player.getLocation().add(0, 2.5, 0);
        World world = player.getWorld();
        
        // Orbiting indicator particles
        long time = System.currentTimeMillis();
        double baseAngle = (time % 3000) / 3000.0 * Math.PI * 2;
        
        int displayCount = Math.min(count, 8); // Cap visual display at 8 dots
        
        for (int i = 0; i < displayCount; i++) {
            double angle = baseAngle + (2 * Math.PI * i / displayCount);
            double radius = 0.6;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            Location particleLoc = loc.clone().add(x, 0, z);
            
            Color indicatorColor = hasPurpleAura ? PURPLE_COLOR : DARK_COLOR;
            
            world.spawnParticle(Particle.DUST, particleLoc, 1, 
                new Particle.DustOptions(indicatorColor, 1.2f));
        }
        
        // Purple aura when many birds (spawns birds faster)
        if (hasPurpleAura) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (Math.random() - 0.5) * 2;
                double offsetZ = (Math.random() - 0.5) * 2;
                Location auraLoc = player.getLocation().add(offsetX, 0.1, offsetZ);
                world.spawnParticle(Particle.DUST, auraLoc, 1,
                    new Particle.DustOptions(PURPLE_COLOR, 2.0f));
            }
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
        
        // Find this player's birds that aren't already homing
        List<BlackBird> playerBirds = new ArrayList<>();
        synchronized (allBirds) {
            for (BlackBird bird : allBirds) {
                if (bird.getOwnerUUID().equals(uuid) && !bird.isHoming()) {
                    playerBirds.add(bird);
                }
            }
        }
        
        if (playerBirds.isEmpty()) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.4f, 1.2f);
            return;
        }
        
        // Find target
        LivingEntity target = findTargetInDirection(player, player.getEyeLocation(),
            player.getLocation().getDirection(), 20.0);
        
        if (target == null) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.4f, 1.2f);
            return;
        }
        
        applyCooldown(uuid);
        
        // Consume first available bird and launch it
        BlackBird bird = playerBirds.get(0);
        bird.launchAsHoming(target, () -> {
            // Callback when bird returns - bird handles its own state reset
        });
        
        // Sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.6f, 0.8f);
    }
    
    private LivingEntity findTargetInDirection(Player player, Location start, Vector direction, double range) {
        for (double d = 1; d <= range; d += 0.5) {
            Location checkLoc = start.clone().add(direction.clone().multiply(d));
            
            for (Entity entity : checkLoc.getWorld().getNearbyEntities(checkLoc, 1.0, 1.0, 1.0)) {
                if (entity instanceof LivingEntity living && 
                    !entity.equals(player) && 
                    !(entity instanceof ArmorStand) &&
                    !living.isDead() &&
                    !(entity instanceof Player sp && sp.getGameMode() == GameMode.SPECTATOR) &&
                    !(entity instanceof Bat)) {
                    return living;
                }
            }
        }
        return null;
    }

    @Override
    public void resetItem() {
        synchronized (allBirds) {
            for (BlackBird bird : allBirds) {
                bird.remove();
            }
            allBirds.clear();
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
        return "carrioncall";
    }
    
    @Override
    public boolean isInItemRotation() {
        return true;
    }
    
    @Override
    public String getName() {
        return "§5Carrion Call";
    }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§dPassively summons a murder of crows",
            "§dthat orbit and attack nearby enemies.",
            "§dBirds blind targets and explode after",
            "§d3 attacks or when their lifetime ends.",
            "",
            "§9Summon",
            "§dOn Hit Extender",
            "§3Talisman",
            "§9§obeangame"
        );
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }
    
    @Override
    public Material getMaterial() {
        return Material.AMETHYST_SHARD;
    }
    
    @Override
    public int getCustomModelData() {
        return 0;
    }
    
    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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
    
    // Inner class for individual bird behavior
    public static class BlackBird {
        
        private Bat base;
        private ItemDisplay headDisplay;
        private ItemDisplay leftWing;
        private ItemDisplay rightWing;
        private int ticksAlive;
        private int attacksRemaining;
        private boolean isHoming = false;
        private boolean hasExploded = false;
        private LivingEntity homingTarget = null;
        private Player owner;
        private UUID ownerUUID;
        private Runnable returnCallback = null;
        private double orbitAngle;
        private double orbitYOffset;
        private double orbitRadius;
        private double orbitSpeed;
        private double orbitDirection;
        private double currentRadius; // Current radius that varies
        private boolean returningToOrbit = false;
        private Location orbitReturnTarget = null;
        private int wingAnimationTick = 0;
        
        private static ItemStack crowSkull = null;
        private static ItemStack blackCarpet = null;
        private static final String CROW_TEXTURE_URL = "https://textures.minecraft.net/texture/48ebd71363e6bf0ce67905e9cf42e182e6924abbd86bf1f5f52a45f08b9255e8";
        
        public BlackBird(Location loc, Player owner) {
            this.owner = owner;
            this.ownerUUID = owner.getUniqueId();
            this.ticksAlive = 0;
            this.attacksRemaining = 3;
            this.orbitAngle = Math.random() * Math.PI * 2;
            this.orbitYOffset = 0.5 + Math.random() * 1.5;
            this.orbitSpeed = 0.08 + Math.random() * 0.02;
            this.orbitDirection = Math.random() < 0.5 ? 1.0 : -1.0;
            
            // Random outward variation
            this.currentRadius = BIRD_ORBIT_RADIUS_BASE;
            if (Math.random() < OUTWARD_VARIATION_CHANCE) {
                this.currentRadius += Math.random() * OUTWARD_VARIATION_AMOUNT;
            }
            this.orbitRadius = this.currentRadius;
            
            World world = loc.getWorld();
            
            // Create bat base - INVISIBLE with long invisibility
            base = world.spawn(loc, Bat.class);
            for(Player player : Bukkit.getOnlinePlayers()) {
                player.hideEntity(Main.getPlugin(), base);
            }
            base.setAwake(true);
            base.setAI(false);
            base.setSilent(true);
            base.setInvulnerable(false);
            base.setHealth(4);
            base.setPersistent(false);
            base.setRemoveWhenFarAway(true);
            
            // Make bat invisible for longer than lifespan (600 ticks = 30 seconds, give 5 minutes)
            base.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 6000, 0, false, false));
            
            // Create head display
            if (crowSkull == null) createCrowSkull();
            if (blackCarpet == null) createBlackCarpet();
            
            headDisplay = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
            headDisplay.setItemStack(crowSkull);
            headDisplay.setTeleportDuration(1);
            headDisplay.setBillboard(Display.Billboard.FIXED); // Fixed rotation so we control it
            
            // Create wings
            leftWing = createWing(world, loc, true);
            rightWing = createWing(world, loc, false);
            
        }
        
        private ItemDisplay createWing(World world, Location loc, boolean isLeft) {
            ItemDisplay wing = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
            wing.setItemStack(blackCarpet);
            wing.setTeleportDuration(1);
            wing.setBillboard(Display.Billboard.FIXED);
            
            // Initial wing transformation
            float xOffset = isLeft ? -0.4f : 0.4f;
            wing.setTransformation(new Transformation(
                new Vector3f(xOffset, 0, 0),
                new Quaternionf(),
                new Vector3f(0.4f, 0.05f, 0.25f), // Flat, wide, short
                new Quaternionf()
            ));
            
            return wing;
        }
        
        private static void createCrowSkull() {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "Crow");
            
            try {
                URI uri = new URI(CROW_TEXTURE_URL);
                URL url = uri.toURL();
                profile.getTextures().setSkin(url);
            } catch (URISyntaxException | MalformedURLException e) {
                e.printStackTrace();
                return;
            }
            
            headMeta.setOwnerProfile(profile);
            head.setItemMeta(headMeta);
            crowSkull = head;
        }
        
        private static void createBlackCarpet() {
            blackCarpet = new ItemStack(Material.BLACK_CARPET, 1);
        }
        
        /**
         * Main tick function - called from static tickAllBirds()
         */
        public void tick() {
            ticksAlive++;
            wingAnimationTick++;
            
            // Owner offline or null, remove bird
            if (owner == null || !owner.isOnline() || owner.getWorld().getName().toString() != base.getWorld().getName().toString() || owner.getGameMode().equals(GameMode.SPECTATOR)) {
                remove();
                return;
            }
            
            // Check if bird is dead from damage (not expired)
            if (!base.isValid() || base.isDead() || base.getHealth() <= 0) {
                hasExploded = true;
                remove();
                return;
            }
            
            if (isHoming && homingTarget != null && !homingTarget.isDead()) {
                tickHoming();
            } else if (returningToOrbit) {
                tickReturnToOrbit();
            } else {
                tickOrbit();
                if(ticksAlive % 20 == 0) checkForAttack();
            }
            
            // Sync displays to bat with proper rotation
            if (base.isValid()) {
                Location loc = base.getLocation();
                float yaw = loc.getYaw();
                updateDisplayRotations(loc, yaw);
            }
        }
        
        private void updateDisplayRotations(Location loc, float yaw) {
            double yawRad = Math.toRadians(yaw);
            
            // Head position and rotation - faces movement direction
            Location headLoc = loc.clone().add(0, 0.3, 0);
            headDisplay.teleport(headLoc);
            
            // Head rotation - faces forward in movement direction
            Quaternionf headRotation = new Quaternionf().rotateY((float) -yawRad);
            headDisplay.setTransformation(new Transformation(
                new Vector3f(0, 0.3f, 0),
                headRotation,
                new Vector3f(0.6f, 0.6f, 0.6f),
                new Quaternionf()
            ));
            
            // Update wings with matching rotation plus flapping
            updateWings(loc, yaw);
        }

        private void updateWings(Location batLoc, float yaw) {
            // 3-stage wing animation
            int cycle = wingAnimationTick % 30;
            float wingAngle;
            
            if (cycle < 10) {
                wingAngle = 0f;
            } else if (cycle < 20) {
                float progress = (cycle - 10) / 10f;
                wingAngle = (float) Math.toRadians(-35 * Math.sin(progress * Math.PI));
            } else {
                float progress = (cycle - 20) / 10f;
                wingAngle = (float) Math.toRadians(35 * Math.sin(progress * Math.PI));
            }
            
            double yawRad = Math.toRadians(yaw);
            
            // Left wing - positioned to left side, rotated to match bird facing
            Vector3f leftOffset = new Vector3f(-0.25f, 0.33f, 0);
            leftOffset.rotateY((float) -yawRad);
            Location leftLoc = batLoc.clone().add(leftOffset.x, leftOffset.y, leftOffset.z);
            leftWing.teleport(leftLoc);
            
            // Left wing: match bird rotation + flap rotation around Z axis
            Quaternionf leftRotation = new Quaternionf()
                .rotateY((float) -yawRad)  // Match bird facing
                .rotateX(wingAngle);        // Flap up/down (changed to X axis for proper wing flap)
            leftWing.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                leftRotation,
                new Vector3f(0.4f, 0.05f, 0.25f),
                new Quaternionf()
            ));
            
            // Right wing - positioned to right side, rotated to match bird facing
            Vector3f rightOffset = new Vector3f(0.25f, 0.33f, 0);
            rightOffset.rotateY((float) -yawRad);
            Location rightLoc = batLoc.clone().add(rightOffset.x, rightOffset.y, rightOffset.z);
            rightWing.teleport(rightLoc);
            
            // Right wing: match bird rotation + opposite flap
            Quaternionf rightRotation = new Quaternionf()
                .rotateY((float) -yawRad)   // Match bird facing
                .rotateX(-wingAngle);        // Mirror flap
            rightWing.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                rightRotation,
                new Vector3f(0.4f, 0.05f, 0.25f),
                new Quaternionf()
            ));
        }
                
        private void tickOrbit() {
            // Update orbit angle - 50% chance to orbit in opposite direction
            orbitAngle += orbitSpeed * orbitDirection;
            
            // Normalize angle to prevent overflow
            while (orbitAngle > 2 * Math.PI) orbitAngle -= 2 * Math.PI;
            while (orbitAngle < 0) orbitAngle += 2 * Math.PI;
            
            // Occasionally change radius slightly for more variation
            if (ticksAlive % 100 == 0 && Math.random() < 0.3) {
                double targetRadius = BIRD_ORBIT_RADIUS_BASE + (Math.random() < OUTWARD_VARIATION_CHANCE ? 
                    Math.random() * OUTWARD_VARIATION_AMOUNT : 0);
                currentRadius += (targetRadius - currentRadius) * 0.1;
                orbitRadius = currentRadius;
            }
            
            // Calculate position with wonky y-level variation
            double timeOffset = ticksAlive * 0.05;
            double yOffset = orbitYOffset + Math.sin(timeOffset + orbitAngle * 2) * 0.3;
            
            // Get owner location and calculate target position
            Location ownerLoc = owner.getLocation();
            double targetX = ownerLoc.getX() + -Math.cos(orbitAngle) * orbitRadius;
            double targetZ = ownerLoc.getZ() + -Math.sin(orbitAngle) * orbitRadius;
            double targetY = ownerLoc.getY() + yOffset;
            
            // Ensure Y is reasonable
            targetY = Math.max(ownerLoc.getWorld().getMinHeight() + 1, Math.min(targetY, ownerLoc.getWorld().getMaxHeight() - 1));
            
            Location targetLoc = new Location(owner.getWorld(), targetX, targetY, targetZ);
            
            // Calculate tangent direction for facing
            // Tangent vector is perpendicular to radius vector
            double tangentX = -Math.sin(orbitAngle) * orbitDirection;
            double tangentZ = Math.cos(orbitAngle) * orbitDirection;
            
            // Calculate yaw from tangent vector (negative x to match Minecraft convention)
            float yaw = (float) Math.toDegrees(Math.atan2(-tangentX, tangentZ));
            
            float adjustedYaw = yaw/2;
            targetLoc.setYaw(adjustedYaw);
            targetLoc.setPitch(0);
            
            // Teleport bat to new position
            if(!base.hasPotionEffect(PotionEffectType.SLOWNESS)) base.teleport(targetLoc);
            base.getWorld().spawnParticle(Particle.SMOKE, targetLoc.add(0, 0.33, 0), 1, 0.1, 0.1, 0.1, 0.01);
        }
        
        private void checkForAttack() {
            if (attacksRemaining <= 0) return;
            
            Location birdLoc = base.getLocation();
            World world = birdLoc.getWorld();
            
            // Clean up old entries from this bird's recent attacks
            long currentTime = System.currentTimeMillis();
            recentlyAttacked.removeIf(uuid -> {
                Long lastAttack = getLastAttackTime(uuid);
                return lastAttack != null && currentTime - lastAttack >= 1000;
            });
            
            // Find nearby enemies that haven't been attacked recently
            LivingEntity bestTarget = null;
            double bestDistance = Double.MAX_VALUE;
            
            for (Entity entity : world.getNearbyEntities(birdLoc, BIRD_ATTACK_RANGE, BIRD_ATTACK_RANGE, BIRD_ATTACK_RANGE)) {
                if (isValidTarget(entity) && entity instanceof LivingEntity living) {
                    UUID targetUUID = entity.getUniqueId();

                    if(living instanceof Player p && p.getGameMode().equals(GameMode.SPECTATOR)) continue;
                    
                    // Skip if this bird already attacked this target recently
                    if (recentlyAttacked.contains(targetUUID)) continue;
                    
                    // Skip if any bird from this owner attacked this target in the last second
                    if (isTargetOnCooldown(targetUUID)) continue;
                    
                    double distance = birdLoc.distanceSquared(entity.getLocation());
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestTarget = living;
                    }
                }
            }
            
            if (bestTarget != null) {
                performAttack(bestTarget);
            }
        }

        private boolean isTargetOnCooldown(UUID targetUUID) {
            Map<UUID, Long> ownerCooldowns = targetAttackCooldowns.get(targetUUID);
            if (ownerCooldowns == null) return false;
            
            Long lastAttack = ownerCooldowns.get(ownerUUID);
            if (lastAttack == null) return false;
            
            return System.currentTimeMillis() - lastAttack < 1000; // 1 second cooldown
        }

        private Long getLastAttackTime(UUID targetUUID) {
            Map<UUID, Long> ownerCooldowns = targetAttackCooldowns.get(targetUUID);
            if (ownerCooldowns == null) return null;
            return ownerCooldowns.get(ownerUUID);
        }

        private void recordAttack(UUID targetUUID) {
            targetAttackCooldowns.computeIfAbsent(targetUUID, k -> new ConcurrentHashMap<>())
                .put(ownerUUID, System.currentTimeMillis());
            recentlyAttacked.add(targetUUID);
        }

        private void clearTargetCooldown(UUID targetUUID) {
            Map<UUID, Long> ownerCooldowns = targetAttackCooldowns.get(targetUUID);
            if (ownerCooldowns != null) {
                ownerCooldowns.remove(ownerUUID);
                // Clean up empty maps to prevent memory leak
                if (ownerCooldowns.isEmpty()) {
                    targetAttackCooldowns.remove(targetUUID);
                }
            }
        }
        
        private void performAttack(LivingEntity target) {
            attacksRemaining--;
            
            // Record this attack for cooldown tracking
            recordAttack(target.getUniqueId());
            
            // ... rest of the method remains the same
            World world = base.getWorld();
            Location targetLoc = target.getLocation();
            
            // Visual effect - dash to target
            Vector dashDir = targetLoc.toVector().subtract(base.getLocation().toVector()).normalize();
            Location dashLoc = base.getLocation().add(dashDir.multiply(0.5));
            base.teleport(dashLoc);
            
            if (attacksRemaining > 0) {
                // Attacks 1 and 2: Blind + 1 damage
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, BLINDNESS_DURATION_TICKS, BLINDNESS_AMPLIFIER));
                Cooldowns.setCooldown("silenced", target.getUniqueId(), 1000);
                target.damage(ATTACK_1_2_DAMAGE, owner);
                
                // Effects
                world.playSound(targetLoc, Sound.ENTITY_BAT_HURT, 0.2f, 1.2f);
                world.spawnParticle(Particle.SQUID_INK, targetLoc.add(0, 1, 0), 3, 0.2, 0.2, 0.2, 0.01);
                
            } else {
                // Attack 3: Explosion + 3 damage
                explodeAndRemove();
            }
        }
        
        private void tickHoming() {
            if (homingTarget == null || homingTarget.isDead()) {
                beginReturnToOrbit();
                return;
            }
            
            Location birdLoc = base.getLocation();
            Location targetLoc = homingTarget.getLocation().add(0, 1, 0);
            
            Vector direction = targetLoc.toVector().subtract(birdLoc.toVector());
            double distance = direction.length();
            direction.normalize();
            
            // Move towards target
            double speed = 0.9;
            Location newLoc = birdLoc.clone().add(direction.multiply(speed));
            
            // Set facing direction
            float yaw = (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ()));
            newLoc.setYaw(yaw);
            newLoc.setPitch(0);
            
            base.teleport(newLoc);
            
            // Trail particles
            World world = birdLoc.getWorld();
            world.spawnParticle(Particle.SMOKE, birdLoc.add(0, 0.33, 0), 1, 0.1, 0.1, 0.1, 0.01);
            
            // Check collision
            if (distance < 1.5) {
                homingTarget.damage(ATTACK_3_DAMAGE, owner);
                homingTarget.getWorld().playSound(homingTarget.getLocation(), Sound.ENTITY_BAT_DEATH, 0.3f, 0.6f);
                homingTarget.getWorld().spawnParticle(Particle.EXPLOSION, homingTarget.getLocation().add(0, 1, 0), 1);
                
                beginReturnToOrbit();
            }
        }
        
        private void beginReturnToOrbit() {
            isHoming = false;
            homingTarget = null;
            returningToOrbit = true;
            
            // Calculate a position in the orbit to return to
            orbitAngle = Math.random() * Math.PI * 2;
            double yOffset = orbitYOffset + Math.sin(ticksAlive * 0.05 + orbitAngle * 2) * 0.3;
            double x = owner.getLocation().getX() + Math.cos(orbitAngle) * orbitRadius;
            double z = owner.getLocation().getZ() + Math.sin(orbitAngle) * orbitRadius;
            double y = owner.getLocation().getY() + yOffset;
            
            orbitReturnTarget = new Location(owner.getWorld(), x, y, z);
        }
        
        private void tickReturnToOrbit() {
            if (orbitReturnTarget == null) {
                returningToOrbit = false;
                return;
            }
            
            Location birdLoc = base.getLocation();
            Vector direction = orbitReturnTarget.toVector().subtract(birdLoc.toVector());
            double distance = direction.length();
            direction.normalize();
            
            // Move towards orbit position
            double speed = 0.7;
            if (distance > 2.0) speed = 0.9;
            
            Location newLoc = birdLoc.clone().add(direction.multiply(speed));
            float yaw = (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ()));
            newLoc.setYaw(yaw);
            newLoc.setPitch(0);
            
            base.teleport(newLoc);
            
            // Trail particles
            base.getWorld().spawnParticle(Particle.DUST, birdLoc, 1, 
                new Particle.DustOptions(DARK_COLOR, 0.8f));
            
            // Check if arrived
            if (distance < 1.0) {
                returningToOrbit = false;
                orbitReturnTarget = null;
                resetForOrbit();
                
                if (returnCallback != null) {
                    returnCallback.run();
                }
            }
        }
        
        public void launchAsHoming(LivingEntity target, Runnable onReturn) {
            isHoming = true;
            homingTarget = target;
            returnCallback = onReturn;
            
            base.getWorld().playSound(base.getLocation(), Sound.ENTITY_BAT_LOOP, 0.5f, 1.5f);
        }
        
        private void resetForOrbit() {
            isHoming = false;
            homingTarget = null;
            returningToOrbit = false;
            orbitReturnTarget = null;
            attacksRemaining = 3;
            ticksAlive = 0;
            
            // Clear all target cooldowns for this bird's owner so targets can be attacked again
            for (UUID targetUUID : new HashSet<>(recentlyAttacked)) {
                clearTargetCooldown(targetUUID);
            }
            recentlyAttacked.clear();
            
            // New orbit parameters for variety
            orbitAngle = Math.random() * Math.PI * 2;
            orbitYOffset = 0.5 + Math.random() * 1.5;
            
            // Random radius variation
            currentRadius = BIRD_ORBIT_RADIUS_BASE;
            if (Math.random() < OUTWARD_VARIATION_CHANCE) {
                currentRadius += Math.random() * OUTWARD_VARIATION_AMOUNT;
            }
            orbitRadius = currentRadius;

            this.orbitDirection = Math.random() < 0.5 ? 1.0 : -1.0;
        }
        
        public void explodeAndRemove() {
            if (hasExploded) return;
            hasExploded = true;
            
            Location loc = base.getLocation();
            World world = loc.getWorld();
            
            // Explosion effect
            world.playSound(loc, Sound.ENTITY_BAT_DEATH, 0.2f, 0.5f);
            world.spawnParticle(Particle.EXPLOSION, loc, 1);
            world.spawnParticle(Particle.SMOKE, loc, 10, 0.3, 0.3, 0.3, 0.05);
            
            // Damage nearby enemies
            for (Entity entity : world.getNearbyEntities(loc, 2.0, 2.0, 2.0)) {
                if (isValidTarget(entity)) {
                    ((LivingEntity) entity).damage(ATTACK_3_DAMAGE, owner);
                }
            }
            
            // Clear cooldowns before removing
            for (UUID targetUUID : new HashSet<>(recentlyAttacked)) {
                clearTargetCooldown(targetUUID);
            }
            recentlyAttacked.clear();
            
            remove();
        }

        public boolean isValidTarget(Entity entity) {
            return (entity instanceof LivingEntity && 
                    !entity.equals(owner) && 
                    !(entity instanceof ArmorStand) &&
                    !(entity instanceof ItemDisplay) &&
                    !(entity instanceof Player sp && sp.getGameMode() == GameMode.SPECTATOR) &&
                    !(entity instanceof Bat));
        }
        
        public boolean shouldRemove() {
            return hasExploded || ticksAlive > BIRD_LIFETIME_TICKS || !base.isValid() || !headDisplay.isValid() || 
                   !leftWing.isValid() || !rightWing.isValid();
        }
        
        public boolean hasExploded() {
            return hasExploded;
        }
        
        public boolean isHoming() {
            return isHoming || returningToOrbit;
        }
        
        public UUID getOwnerUUID() {
            return ownerUUID;
        }
        
        public void remove() {
            for (Map<UUID, Long> ownerCooldowns : targetAttackCooldowns.values()) {
                ownerCooldowns.remove(ownerUUID);
            }
            if (base.isValid()) base.remove();
            if (headDisplay.isValid()) headDisplay.remove();
            if (leftWing.isValid()) leftWing.remove();
            if (rightWing.isValid()) rightWing.remove();
        }
    }
}