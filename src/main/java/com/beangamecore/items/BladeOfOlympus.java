package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

public class BladeOfOlympus extends BeangameItem implements BGDDealerHeldI {
    
    private final static Random random = new Random();

    private final double MIN_CLOUD_HEIGHT = 3.5;
    private final double MAX_CLOUD_HEIGHT = 5.5;
    private final double CLOUD_DRIFT_RANGE = 1.5;
    private final int CLOUD_DURATION_TICKS = 25;

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity damager = (LivingEntity) event.getDamager();
        UUID uuid = damager.getUniqueId();
        if (onCooldown(uuid)) {
            return;
        }
        if (isInvalidDamager(damager)) {
            return;
        }

        LivingEntity target = (LivingEntity) event.getEntity();
        spawnDynamicLightningEffect(target, damager);
    }

    private boolean isInvalidDamager(LivingEntity damager) {
        if (damager instanceof Player p) {
            if (p.getGameMode().equals(GameMode.SPECTATOR)) {
                return true;
            }
            applyCooldown(damager.getUniqueId());
            return false;
        } else {
            return isNonPlayerDamagerInvalid(damager);
        }
    }

    private boolean isNonPlayerDamagerInvalid(LivingEntity damager) {
        return damager.isDead() || !damager.isValid();
    }

    private void spawnDynamicLightningEffect(LivingEntity target, LivingEntity damager) {
        double height = MIN_CLOUD_HEIGHT + random.nextDouble() * (MAX_CLOUD_HEIGHT - MIN_CLOUD_HEIGHT);
        double offsetX = (random.nextDouble() - 0.5) * CLOUD_DRIFT_RANGE;
        double offsetZ = (random.nextDouble() - 0.5) * CLOUD_DRIFT_RANGE;

        Location cloudLoc = target.getLocation().clone().add(offsetX, height, offsetZ);
        Location targetLoc = target.getLocation().clone().add(0, 1, 0);

        // Darker, harsher sound
        float pitch = 0.5f + random.nextFloat() * 0.3f;
        target.getWorld().playSound(cloudLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, pitch);
        
        createDriftingCloud(cloudLoc, target);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            animateZigZagLightning(cloudLoc, targetLoc, target, damager);
        }, 8);
    }

    private void createDriftingCloud(Location initialLoc, LivingEntity target) {
        int[] duration = {CLOUD_DURATION_TICKS};
        Location currentLoc = initialLoc.clone();
        Vector driftDirection = new Vector(
            (random.nextDouble() - 0.5) * 0.08,
                0, 
            (random.nextDouble() - 0.5) * 0.08
        );

        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (duration[0] <= 0 || target.isDead()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
                
            Location targetLocation = target.getLocation();
            currentLoc.setX(targetLocation.getX() + (currentLoc.getX() - targetLocation.getX()) * 0.92);
            currentLoc.setZ(targetLocation.getZ() + (currentLoc.getZ() - targetLocation.getZ()) * 0.92);
            currentLoc.add(driftDirection);
            
            currentLoc.getWorld().spawnParticle(Particle.CLOUD, currentLoc, 8, 
                0.6, 0.3, 0.6, 0.03);
                
            if (duration[0] % 5 == 0) {
                spawnCloudElectricity(currentLoc);
            }
                
            duration[0]--;
        }, 0, 1).getTaskId();

        
    }

    private void spawnCloudElectricity(Location cloudLoc) {
        for (int i = 0; i < 3; i++) {
            Location electricLoc = cloudLoc.clone().add(
                (random.nextDouble() - 0.5) * 0.8,
                random.nextDouble() * 0.3,
                (random.nextDouble() - 0.5) * 0.8
            );
            // Thicker blue particles
            cloudLoc.getWorld().spawnParticle(Particle.DUST, electricLoc, 2,
                new Particle.DustOptions(Color.fromRGB(80, 140, 255), 0.2f));
        }
    }

    private void animateZigZagLightning(Location from, Location to, LivingEntity target, LivingEntity damager) {
        LightningAnimator animator = new LightningAnimator(from, target, damager);
        animator.start();
    }

    private static class LightningAnimator extends BukkitRunnable {
        // Constants
        private static final double STEP_SIZE = 0.15;
        private static final double ZIGZAG_MAGNITUDE = 0.4;
        private static final double ZIGZAG_FREQUENCY = 0.3;
        private static final double IMPACT_THRESHOLD = 0.25;
        private static final double BRANCH_PROBABILITY = 0.4;
        
        // Animation state
        private final Vector initialOffset;
        private final LivingEntity target;
        private final LivingEntity damager;
        private final World world;
        private int currentStep = 0;
        private boolean hasImpacted = false;
        private int totalSteps = -1;
        
        // Pre-calculated dust options
        private final Particle.DustOptions primaryDust;
        private final Particle.DustOptions secondaryDust;
        private final Particle.DustOptions branchPrimaryDust;
        private final Particle.DustOptions branchSecondaryDust;
        
        // Reusable objects to reduce GC pressure
        private final LocationPool locationPool;
        private final VectorPool vectorPool;
        
        public LightningAnimator(Location from, LivingEntity target, LivingEntity damager) {
            this.initialOffset = from.clone().subtract(target.getLocation()).toVector();
            this.target = target;
            this.damager = damager;
            this.world = target.getWorld();
            
            this.primaryDust = new Particle.DustOptions(Color.fromRGB(50, 100, 255), 1.5f);
            this.secondaryDust = new Particle.DustOptions(Color.fromRGB(100, 180, 255), 1.2f);
            this.branchPrimaryDust = new Particle.DustOptions(Color.fromRGB(200, 220, 255), 0.8f);
            this.branchSecondaryDust = new Particle.DustOptions(Color.fromRGB(80, 120, 255), 0.5f);
            
            this.locationPool = new LocationPool(world);
            this.vectorPool = new VectorPool();
        }
        
        public void start() {
            this.runTaskTimer(Main.getPlugin(), 0, 1);
        }

        @Override
        public void run() {
            if (shouldStop()) {
                handleCompletion();
                return;
            }
            
            LightningFrame frame = calculateCurrentFrame();
            
            if (shouldTriggerImpact(frame)) {
                triggerImpactEffects(frame.targetLocation);
                hasImpacted = true;
            }

            if(hasImpacted){
                handleCompletion();
            }
            
            renderLightningBolt(frame);
            maybeCreateBranch(frame);
            
            currentStep++;
        }
        
        private boolean shouldStop() {
            return target.isDead();
        }
        
        private void handleCompletion() {
            if (isAnimationComplete()) {
                LightningFrame finalFrame = calculateCurrentFrame();
                if (!hasImpacted) {
                    triggerImpactEffects(finalFrame.targetLocation);
                }
            }
            this.cancel();
        }
        
        private boolean isAnimationComplete() {
            if (totalSteps == -1) {
                calculateTotalSteps();
            }
            return currentStep >= totalSteps;
        }
        
        private LightningFrame calculateCurrentFrame() {
            Location targetLoc = locationPool.getTargetLocation(target);
            Location fromLoc = locationPool.getFromLocation(target, initialOffset);
            
            Vector direction = vectorPool.getDirection(fromLoc, targetLoc);
            double distance = calculateDistance(direction);
            
            if (totalSteps == -1) {
                totalSteps = (int) (distance / STEP_SIZE);
            }
            
            direction = vectorPool.normalize(direction, distance);
            Location currentLoc = locationPool.getCurrentLocation(fromLoc, direction, distance, currentStep, totalSteps);
            Location zigzagLoc = locationPool.getZigzagLocation(currentLoc, direction, currentStep);
            
            return new LightningFrame(targetLoc, currentLoc, zigzagLoc, direction, distance);
        }
        
        private void calculateTotalSteps() {
            LightningFrame frame = calculateCurrentFrame();
            totalSteps = (int) (frame.distance / STEP_SIZE);
        }
        
        private double calculateDistance(Vector direction) {
            return Math.sqrt(direction.getX() * direction.getX() + 
                            direction.getY() * direction.getY() + 
                            direction.getZ() * direction.getZ());
        }
        
        private boolean shouldTriggerImpact(LightningFrame frame) {
            if (hasImpacted) return false;
            
            double impactDistance = Math.sqrt(
                Math.pow(frame.currentLocation.getX() - frame.targetLocation.getX(), 2) +
                Math.pow(frame.currentLocation.getY() - frame.targetLocation.getY(), 2) +
                Math.pow(frame.currentLocation.getZ() - frame.targetLocation.getZ(), 2)
            );
            return impactDistance <= IMPACT_THRESHOLD;
        }
        
        private void triggerImpactEffects(Location impactLoc) {
            hasImpacted = true;
            world.playSound(impactLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.9f, 0.7f);
            world.spawnParticle(Particle.FLASH, impactLoc, 1);
            world.spawnParticle(Particle.ELECTRIC_SPARK, impactLoc, 8, 0.5, 0.5, 0.5, 0.2);
            target.damage(1, (Entity) damager);
        }
        
        private void renderLightningBolt(LightningFrame frame) {
            Particle.DustOptions dust = (currentStep % 2 == 0) ? primaryDust : secondaryDust;
            spawnMainLightningParticles(frame.zigzagLocation, dust);
        }
        
        private void spawnMainLightningParticles(Location zigzagLoc, Particle.DustOptions dust) {
            for (int i = 0; i < 3; i++) {
                Location particleLoc = locationPool.getParticleLocation(zigzagLoc);
                world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dust);
            }
        }
        
        private void maybeCreateBranch(LightningFrame frame) {
            if (random.nextDouble() < BRANCH_PROBABILITY) {
                createLightningBranch(frame.zigzagLocation, frame.direction);
            }
        }
        
        private void createLightningBranch(Location origin, Vector mainDirection) {
            Vector branchDirection = vectorPool.getBranchDirection(mainDirection);
            int branchLength = random.nextInt(3) + 2;
            
            Location branchLoc = locationPool.getBranchStartLocation(origin);
            Vector branchStep = vectorPool.getBranchStep(branchDirection);
            
            renderBranchSegments(branchLoc, branchStep, branchLength);
        }
        
        private void renderBranchSegments(Location branchLoc, Vector branchStep, int branchLength) {
            for (int i = 0; i < branchLength; i++) {
                branchLoc.add(branchStep);
                
                Particle.DustOptions fadingDust = createFadingDust(i, branchLength);
                world.spawnParticle(Particle.DUST, branchLoc, 1, 0, 0, 0, 0, fadingDust);
                
                if (i % 2 == 0) {
                    world.spawnParticle(Particle.DUST, branchLoc, 1, 0.05, 0.05, 0.05, 0, branchSecondaryDust);
                }
            }
        }
        
        private Particle.DustOptions createFadingDust(int currentSegment, int totalSegments) {
            float fadeSize = branchPrimaryDust.getSize() * (1 - (float) currentSegment / totalSegments);
            return new Particle.DustOptions(branchPrimaryDust.getColor(), fadeSize);
        }
        
        // Data classes and helper pools
        private static class LightningFrame {
            final Location targetLocation;
            final Location currentLocation;
            final Location zigzagLocation;
            final Vector direction;
            final double distance;
            
            LightningFrame(Location targetLocation, Location currentLocation, 
                        Location zigzagLocation, Vector direction, double distance) {
                this.targetLocation = targetLocation;
                this.currentLocation = currentLocation;
                this.zigzagLocation = zigzagLocation;
                this.direction = direction;
                this.distance = distance;
            }
        }
        
        private static class LocationPool {
            private final World world;
            private final Location targetLoc = new Location(null, 0, 0, 0);
            private final Location fromLoc = new Location(null, 0, 0, 0);
            private final Location currentLoc = new Location(null, 0, 0, 0);
            private final Location zigzagLoc = new Location(null, 0, 0, 0);
            private final Location particleLoc = new Location(null, 0, 0, 0);
            
            LocationPool(World world) {
                this.world = world;
            }
            
            Location getTargetLocation(LivingEntity target) {
                Location pos = target.getLocation();
                targetLoc.setWorld(world);
                targetLoc.setX(pos.getX());
                targetLoc.setY(pos.getY() + 1);
                targetLoc.setZ(pos.getZ());
                return targetLoc;
            }
            
            Location getFromLocation(LivingEntity target, Vector offset) {
                Location pos = target.getLocation();
                fromLoc.setWorld(world);
                fromLoc.setX(pos.getX() + offset.getX());
                fromLoc.setY(pos.getY() + offset.getY());
                fromLoc.setZ(pos.getZ() + offset.getZ());
                return fromLoc;
            }
            
            Location getCurrentLocation(Location from, Vector direction, double distance, int step, int totalSteps) {
                double stepDistance = distance / totalSteps;
                currentLoc.setWorld(world);
                currentLoc.setX(from.getX() + direction.getX() * stepDistance * step);
                currentLoc.setY(from.getY() + direction.getY() * stepDistance * step);
                currentLoc.setZ(from.getZ() + direction.getZ() * stepDistance * step);
                return currentLoc;
            }
            
            Location getZigzagLocation(Location current, Vector direction, int step) {
                double zigzagOffset = Math.sin(step * ZIGZAG_FREQUENCY) * ZIGZAG_MAGNITUDE;
                double perpX = -direction.getZ();
                double perpZ = direction.getX();
                double perpLength = Math.sqrt(perpX * perpX + perpZ * perpZ);
                
                if (perpLength > 0) {
                    perpX = (perpX / perpLength) * zigzagOffset;
                    perpZ = (perpZ / perpLength) * zigzagOffset;
                }
                
                zigzagLoc.setWorld(world);
                zigzagLoc.setX(current.getX() + perpX);
                zigzagLoc.setY(current.getY());
                zigzagLoc.setZ(current.getZ() + perpZ);
                return zigzagLoc;
            }
            
            Location getParticleLocation(Location base) {
                particleLoc.setWorld(world);
                particleLoc.setX(base.getX() + (random.nextDouble() - 0.5) * 0.1);
                particleLoc.setY(base.getY() + (random.nextDouble() - 0.5) * 0.1);
                particleLoc.setZ(base.getZ() + (random.nextDouble() - 0.5) * 0.1);
                return particleLoc;
            }
            
            Location getBranchStartLocation(Location origin) {
                return new Location(world, origin.getX(), origin.getY(), origin.getZ());
            }
        }
        
        private static class VectorPool {
            private final Vector direction = new Vector();
            private final Vector branchDirection = new Vector();
            private final Vector branchStep = new Vector();
            
            Vector getDirection(Location from, Location to) {
                direction.setX(to.getX() - from.getX());
                direction.setY(to.getY() - from.getY());
                direction.setZ(to.getZ() - from.getZ());
                return direction;
            }
            
            Vector normalize(Vector vector, double length) {
                if (length > 0) {
                    vector.multiply(1.0 / length);
                }
                return vector;
            }
            
            Vector getBranchDirection(Vector mainDirection) {
                branchDirection.setX(mainDirection.getX() + (random.nextDouble() - 0.5) * 0.7);
                branchDirection.setY(mainDirection.getY() + (random.nextDouble() - 0.5) * 0.7);
                branchDirection.setZ(mainDirection.getZ() + (random.nextDouble() - 0.5) * 0.7);
                
                double length = Math.sqrt(branchDirection.getX() * branchDirection.getX() + 
                                        branchDirection.getY() * branchDirection.getY() + 
                                        branchDirection.getZ() * branchDirection.getZ());
                return normalize(branchDirection, length);
            }
            
            Vector getBranchStep(Vector direction) {
                branchStep.setX(direction.getX() * STEP_SIZE);
                branchStep.setY(direction.getY() * STEP_SIZE);
                branchStep.setZ(direction.getZ() * STEP_SIZE);
                return branchStep;
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 2300;
    }

    @Override
    public String getId() {
        return "bladeofolympus";
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
        return "§3Blade Of Olympus";
    }

    @Override
public List<String> getLore() {
    return List.of(
        "§cHitting enemies summons a dark cloud",
        "§cabove them that strikes them with",
        "§canimated zigzag lightning after a",
        "§cbrief delay.",
        "",
        "§cOn Hit",
        "§dOn Hit Extender",
        "§9§obeangame"
    );
}

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 103;
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

