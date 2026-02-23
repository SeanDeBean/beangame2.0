package com.beangamecore.entities.seacreatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GuardianPopsicle extends BeangameSeaCreature{
    private int tickCounter = 0;
    
    public GuardianPopsicle() {
        setWeight(25);
        setMinWaterDepth(6);
        setMinPlayerLevel(0);
        setPreferredTime(TimeOfDay.NIGHT);
        addAllowedBiome(Biome.DEEP_OCEAN);
        addAllowedBiome(Biome.DEEP_COLD_OCEAN);
        addAllowedBiome(Biome.DEEP_LUKEWARM_OCEAN);
        addAllowedBiome(Biome.OCEAN);
    }

    @Override
    public void spawn(Location location, Player player) {
        // Spawn the bottom guardian (base of the popsicle)
        Guardian baseGuardian = (Guardian) location.getWorld().spawnEntity(location, EntityType.GUARDIAN);
        baseGuardian.setCustomName(getDisplayName());
        baseGuardian.setCustomNameVisible(false);
        baseGuardian.setAI(true);
        baseGuardian.setPersistent(true);
        setMainEntity(baseGuardian);

        // Spawn and stack 3 more guardians on top (4 total)
        Entity previousGuardian = baseGuardian;
        for (int i = 1; i < 4; i++) {
            Location guardianLoc = location.clone().add(0, i * 0.5, 0);
            Guardian guardian = (Guardian) guardianLoc.getWorld().spawnEntity(guardianLoc, EntityType.GUARDIAN);
            
            // Make this guardian ride the previous one
            previousGuardian.addPassenger(guardian);
            
            // Set guardian properties
            guardian.setCustomName("§bGuardian Popsicle Layer " + (i + 1));
            guardian.setCustomNameVisible(false);
            guardian.setAI(true); // Only base guardian needs AI
            guardian.setPersistent(true);
            guardian.setSilent(true);
            
            // Add to entity list
            addEntity(guardian);
            previousGuardian = guardian;
        }

        // Add special effects
        location.getWorld().playSound(location, Sound.ENTITY_GUARDIAN_AMBIENT, 1.0f, 0.8f);
        location.getWorld().spawnParticle(Particle.BUBBLE, location, 30, 2, 3, 2);

        onSpawn(player);
    }

    @Override
    public void tick() {
        tickCounter++;
        
        // Check if main entity died - if so, kill all entities
        if (isMainEntityDead()) {
            removeAllEntities();
            setActive(false);
            return;
        }
        
        // Check if any entity died - if main entity is still alive, continue
        if (isAnyEntityDead() && !isMainEntityDead()) {
            // Remove dead entities from the list
            getAllEntities().removeIf(Entity::isDead);
        }

        // Every 5 seconds (100 ticks), make all guardians target nearest player
        if (tickCounter % 100 == 0 && isActive()) {
            Player target = findNearestPlayer();
            if (target != null) {
                // Make all guardians target the player
                for (Entity entity : getAllEntities()) {
                    if (entity instanceof Guardian guardian && !guardian.isDead()) {
                        guardian.setTarget(target);
                    }
                }
                
                // Play targeting sound
                getMainEntity().getWorld().playSound(
                    getMainEntity().getLocation(), 
                    Sound.ENTITY_GUARDIAN_AMBIENT, 
                    1.5f, 
                    1.0f
                );
            }
        }
        
        // Slowly rotate the popsicle for visual effect
        if (tickCounter % 20 == 0 && isActive() && getMainEntity() != null && !getMainEntity().isDead()) {
            Entity main = getMainEntity();
            Location loc = main.getLocation();
            loc.setYaw(loc.getYaw() + 10f); // Rotate 15 degrees every second
            main.teleport(loc);
        }
    }

    @Override
    public void onDefeated(Player player) {
        int exp = calculateExperience();
        player.giveExp(exp);
        
        // Clean up
        setActive(false);
    }

    @Override
    public void onDespawn() {
        // Remove all entities from the world
        removeAllEntities();
        super.onDespawn();
    }

    @Override
    public List<ItemStack> getLootDrops() {
        List<ItemStack> loot = new ArrayList<>();
        Random random = new Random();
        
        // Guaranteed drops
        loot.add(new ItemStack(Material.PRISMARINE_SHARD, 6 + random.nextInt(6)));
        loot.add(new ItemStack(Material.PRISMARINE_CRYSTALS, 2 + random.nextInt(3)));
        
        // Chance for extra drops
        if (random.nextDouble() < 0.4) { // 40% chance
            loot.add(new ItemStack(Material.WET_SPONGE, 1));
        }
        
        return loot;
    }

    @Override
    public void onSpawn(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_GUARDIAN_FLOP, 1.0f, 1.0f);
        Vector direction = player.getLocation().toVector().subtract(getMainEntity().getLocation().toVector()).normalize();
        getMainEntity().setVelocity(direction.multiply(0.7).setY(0.5));
    }

    private Player findNearestPlayer() {
        Entity main = getMainEntity();
        if (main == null || main.isDead()) return null;
        
        return main.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distance(main.getLocation()) < 20)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getDisplayName() {
        return "§bGuardian Popsicle";
    }

    @Override
    protected int calculateExperience() {
        // Override for more appropriate XP
        SeaCreatureRarity rarity = getRarity();
        int baseXP = 40 + (100 - getWeight()); // Good XP reward
        return (int) (baseXP * rarity.getXpMultiplier());
    }
}
