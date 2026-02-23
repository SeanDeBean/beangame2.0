package com.beangamecore.items;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class Titanbone extends BeangameItem implements BGRClickableI {
    
    private static final TitanboneConfig CONFIG = new TitanboneConfig();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        launchPlayer(player, CONFIG.getInitialLaunch());
        triggerSlam(player, CONFIG.getFullScale());
        return true;
    }

    private void launchPlayer(Player player, LaunchForce launchForce) {
        Vector direction = player.getLocation().getDirection().normalize();
        Vector launch = direction.multiply(launchForce.forward()).setY(launchForce.vertical());
        player.setVelocity(launch);
    }

    private void triggerSlam(Player player, SlamScale scale) {
        World originalWorld = player.getWorld();

        // Lambda version
        int[] taskId = new int[1];
        boolean[] hasLeftGround = {false};
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!isSlamRunnableValid(player, originalWorld)) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            applyFallDamageImmunity(player);
            boolean isOnGround = isPlayerOnGround(player);
            updateHasLeftGround(isOnGround, hasLeftGround);

            if (shouldHandleSlamImpact(isOnGround, hasLeftGround[0])) {
                handleSlamImpact(player, scale);
                Bukkit.getScheduler().cancelTask(taskId[0]);
            }
        }, Ticks.ZERO.value(), Ticks.ONE.value()).getTaskId();
    }

    private boolean isSlamRunnableValid(Player player, World originalWorld) {
        return player.isOnline() && !player.isDead() &&
                player.getGameMode() != GameMode.SPECTATOR &&
                player.getWorld().equals(originalWorld);
    }

    private void applyFallDamageImmunity(Player player) {
        Cooldowns.setCooldown("fall_damage_immunity", player.getUniqueId(), CONFIG.getFallImmunity().toMillis());
    }

    private boolean isPlayerOnGround(Player player) {
        return player.getLocation()
                .getBlock()
                .getRelative(BlockFace.DOWN)
                .getType()
                .isSolid();
    }

    private void updateHasLeftGround(boolean isOnGround, boolean[] hasLeftGround) {
        if (!hasLeftGround[0] && !isOnGround) {
            hasLeftGround[0] = true;
        }
    }

    private boolean shouldHandleSlamImpact(boolean isOnGround, boolean hasLeftGround) {
        return hasLeftGround && isOnGround;
    }


    private void handleSlamImpact(Player player, SlamScale scale) {
        Location slamLoc = player.getLocation();
        World world = player.getWorld();
        Random random = new Random();

        playSlamEffects(world, slamLoc);
        boolean hitAnyPlayers = applyShockwaveToEntities(player, slamLoc, scale);
        dropBones(world, slamLoc, random, scale);
        spawnBoneSpikePillars(player, world, random, scale);

        if (hitAnyPlayers && scale.canRecast()) {
            scheduleRecast(player, scale.reduced());
        }
    }

    private void playSlamEffects(World world, Location slamLoc) {
        world.playSound(slamLoc, Sound.ENTITY_GENERIC_EXPLODE, SoundVolume.NORMAL.value(), SoundPitch.LOW.value());
        world.spawnParticle(Particle.EXPLOSION, slamLoc, ParticleCount.SINGLE.value());
    }

    private boolean applyShockwaveToEntities(Player player, Location slamLoc, SlamScale scale) {
        boolean hitAnyPlayers = false;
        ShockwaveRadius radius = CONFIG.getShockwaveRadius();
        
        for (Entity entity : player.getNearbyEntities(radius.value(), radius.value(), radius.value())) {
            if (!(entity instanceof LivingEntity target) || entity == player) continue;

            Vector away = entity.getLocation().toVector().subtract(slamLoc.toVector()).normalize();
            away.setY(CONFIG.getVerticalKnockback().value() * scale.value());
            away.multiply(CONFIG.getLaunchForce().value() * scale.value());

            target.damage(CONFIG.getDamage().value() * scale.value(), player);


            boolean hasKBResistance = false;
            if(target instanceof Player){
                Player pVictim = (Player) target;
                hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
            }
            if (!hasKBResistance) {
                target.setVelocity(away);
            }

            if (target instanceof Player p && p.getGameMode() != GameMode.SPECTATOR) {
                hitAnyPlayers = true;
            }
        }
        return hitAnyPlayers;
    }

    private void dropBones(World world, Location slamLoc, Random random, SlamScale scale) {
        ItemStack bone = createBoneItem();
        BoneDropCount dropCount = CONFIG.getBoneDropCount(scale);
        
        for (int i = 0; i < dropCount.value(); i++) {
            Item boneItem = world.dropItemNaturally(slamLoc, bone);
            ItemLifetime lifetime = CONFIG.getBoneLifetime(random);
            boneItem.setTicksLived(lifetime.ticksLived());
            boneItem.setPickupDelay(lifetime.pickupDelay());
        }
    }

    private ItemStack createBoneItem() {
        ItemStack bone = new ItemStack(Material.BONE);
        ItemMeta boneMeta = bone.getItemMeta();
        boneMeta.setMaxStackSize(MaxStackSize.SINGLE.value());
        bone.setItemMeta(boneMeta);
        return bone;
    }

    private void scheduleRecast(Player player, SlamScale nextScale) {
        LaunchForce nextLaunch = CONFIG.getScaledLaunch(nextScale);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            launchPlayer(player, nextLaunch);
            triggerSlam(player, nextScale);
        }, CONFIG.getRecastDelay().value());
    }

    private void spawnBoneSpikePillars(Player player, World world, Random random, SlamScale scale) {
        SpikeConfiguration spikeConfig = CONFIG.getSpikeConfiguration(scale, random);
        Location center = player.getLocation();

        for (int i = 0; i < spikeConfig.count(); i++) {
            SpikePosition position = spikeConfig.generatePosition(random, center, world);
            spawnSingleBoneSpike(world, position.location());
        }
    }

    private void spawnSingleBoneSpike(World world, Location base) {
        SpikeHeight height = CONFIG.getSpikeHeight();
        
        for (int y = 0; y < height.value(); y++) {
            BlockPlacementDelay delay = CONFIG.getBlockDelay(y);
            Location blockLoc = base.clone().add(0, y, 0);
            scheduleBoneBlockPlacement(world, blockLoc, delay);
        }
    }

    private void scheduleBoneBlockPlacement(World world, Location blockLoc, BlockPlacementDelay delay) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Block block = blockLoc.getBlock();
            if (block.getType().isAir() || block.isPassable()) {
                block.setType(Material.BONE_BLOCK);
                world.playSound(blockLoc, Sound.BLOCK_BONE_BLOCK_PLACE, SoundVolume.NORMAL.value(), SoundPitch.NORMAL.value());
                world.spawnParticle(Particle.BLOCK, blockLoc.clone().add(0.5, 0.5, 0.5), 
                        ParticleCount.MEDIUM.value(), Material.BONE_BLOCK.createBlockData());

                scheduleBoneBlockRemoval(world, block, blockLoc);
            }
        }, delay.value());
    }

    private void scheduleBoneBlockRemoval(World world, Block block, Location blockLoc) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (block.getType() == Material.BONE_BLOCK) {
                block.setType(Material.AIR);
                world.playSound(blockLoc, Sound.BLOCK_BONE_BLOCK_BREAK, SoundVolume.NORMAL.value(), SoundPitch.LOW.value());
                world.spawnParticle(Particle.BLOCK, blockLoc.clone().add(0.5, 0.5, 0.5), 
                        ParticleCount.HIGH.value(), Material.BONE_BLOCK.createBlockData());
            }
        }, CONFIG.getBlockRemovalDelay().value());
    }

    // Value Objects and Configuration Classes
    
    private static class TitanboneConfig {
        private final ShockwaveRadius shockwaveRadius = new ShockwaveRadius(3.0);
        private final LaunchForceValue launchForce = new LaunchForceValue(1.5);
        private final VerticalKnockback verticalKnockback = new VerticalKnockback(0.8);
        private final Damage damage = new Damage(6.0);
        private final LaunchForce initialLaunch = new LaunchForce(1.4, 0.6);
        private final Duration fallImmunity = Duration.ofSeconds(1);
        private final Ticks recastDelay = Ticks.of(4);
        private final Ticks blockRemovalDelay = Ticks.of(60);
        private final SpikeHeight spikeHeight = new SpikeHeight(3);
        
        public ShockwaveRadius getShockwaveRadius() { return shockwaveRadius; }
        public LaunchForceValue getLaunchForce() { return launchForce; }
        public VerticalKnockback getVerticalKnockback() { return verticalKnockback; }
        public Damage getDamage() { return damage; }
        public LaunchForce getInitialLaunch() { return initialLaunch; }
        public Duration getFallImmunity() { return fallImmunity; }
        public SlamScale getFullScale() { return SlamScale.full(); }
        public Ticks getRecastDelay() { return recastDelay; }
        public Ticks getBlockRemovalDelay() { return blockRemovalDelay; }
        public SpikeHeight getSpikeHeight() { return spikeHeight; }
        
        public BoneDropCount getBoneDropCount(SlamScale scale) {
            return new BoneDropCount((int) Math.floor(10 * scale.value()));
        }
        
        public LaunchForce getScaledLaunch(SlamScale scale) {
            return new LaunchForce(
                initialLaunch.vertical() * scale.value(),
                initialLaunch.forward() * scale.value()
            );
        }
        
        public ItemLifetime getBoneLifetime(Random random) {
            int offset = random.nextInt(41) - 20;
            return new ItemLifetime(5920 + offset, 100000);
        }
        
        public SpikeConfiguration getSpikeConfiguration(SlamScale scale, Random random) {
            int maxSpikes = (int) Math.round(4 * scale.value());
            int count = random.nextInt(maxSpikes + 1);
            return new SpikeConfiguration(count);
        }
        
        public BlockPlacementDelay getBlockDelay(int level) {
            return new BlockPlacementDelay(level * 4);
        }
    }
    
    // Value Objects
    
    public record LaunchForce(double vertical, double forward) {}
    public record SlamScale(double value) {
        public static SlamScale full() { return new SlamScale(1.0); }
        public boolean canRecast() { return value > 0.4; }
        public SlamScale reduced() { return new SlamScale(value * 0.8); }
    }
    public record ShockwaveRadius(double value) {}
    public record LaunchForceValue(double value) {}
    public record VerticalKnockback(double value) {}
    public record Damage(double value) {}
    public record BoneDropCount(int value) {}
    public record ItemLifetime(int ticksLived, int pickupDelay) {}
    public record SpikeHeight(int value) {}
    public record BlockPlacementDelay(long value) {}
    
    public record Ticks(long value) {
        public static final Ticks ZERO = new Ticks(0L);
        public static final Ticks ONE = new Ticks(1L);
        
        public static Ticks of(long value) {
            return new Ticks(value);
        }
    }
    
    public enum SoundVolume {
        NORMAL(1.0f);
        private final float value;
        SoundVolume(float value) { this.value = value; }
        public float value() { return value; }
    }
    
    public enum SoundPitch {
        LOW(0.7f), NORMAL(1.0f);
        private final float value;
        SoundPitch(float value) { this.value = value; }
        public float value() { return value; }
    }
    
    public enum ParticleCount {
        SINGLE(1), MEDIUM(10), HIGH(12);
        private final int value;
        ParticleCount(int value) { this.value = value; }
        public int value() { return value; }
    }
    
    public enum MaxStackSize {
        SINGLE(1);
        private final int value;
        MaxStackSize(int value) { this.value = value; }
        public int value() { return value; }
    }
    
    public static class SpikeConfiguration {
        private final int count;
        
        public SpikeConfiguration(int count) { this.count = count; }
        public int count() { return count; }
        
        public SpikePosition generatePosition(Random random, Location center, World world) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 1.5 + random.nextDouble() * 1.5;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;
            
            Location base = center.clone().add(xOffset, 0, zOffset);
            base = world.getHighestBlockAt(base).getLocation();
            
            return new SpikePosition(base);
        }
    }
    
    public record SpikePosition(Location location) {}
            
    @Override
    public long getBaseCooldown() { return 20000L; }
    @Override
    public String getId() { return "titanbone"; }
    @Override
    public boolean isInItemRotation() { return true; }
    @Override
    public CraftingRecipe getCraftingRecipe() { return null; }
    @Override
    public String getName() { return "§4Titanbone"; }
    
    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to launch forward and slam",
            "§9down, creating a shockwave that damages",
            "§9enemies and spawns bone spikes.",
            "§9Chaining hits allows for repeated slams.",
            "",
            "§9Castable",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() { return Map.of("minecraft:knockback", 2); }
    @Override
    public Material getMaterial() { return Material.BONE; }
    @Override
    public int getCustomModelData() { return 101; }
    @Override
    public List<ItemFlag> getItemFlags() { return List.of(); }
    @Override
    public ArmorTrim getArmorTrim() { return null; }
    @Override
    public Color getColor() { return null; }
    @Override
    public int getArmor() { return 0; }
    @Override
    public EquipmentSlotGroup getSlot() { return null; }
    @Override
    public int getMaxStackSize() { return 1; }
}
