package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
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
import com.beangamecore.items.type.BGTeleportInvI;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.util.Cooldowns;

public class SculkstepCloak extends BeangameItem implements BGDDealerInvI, BGTeleportInvI {

    private final Set<UUID> shockwaveDamagedEntities = ConcurrentHashMap.newKeySet();
    private static final double ECHO_DAMAGE_MULTIPLIER = 0.25;

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item){
        if (!(event.getDamager() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        LivingEntity target = (LivingEntity) event.getEntity();

        if (shockwaveDamagedEntities.contains(target.getUniqueId())) {
            shockwaveDamagedEntities.remove(target.getUniqueId());
            return;
        }

        if (isBackstab(player, target) && !onCooldown(uuid)) {

            applyCooldown(uuid);
            double echoDamage = event.getDamage() * ECHO_DAMAGE_MULTIPLIER;
            
            // Tag the target BEFORE applying shockwave damage
            shockwaveDamagedEntities.add(target.getUniqueId());

            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    // Apply shockwave damage to the target (and others)
                    createSculkShockwave(target, player, echoDamage);
                }, 10L);

            player.getWorld().playSound(target.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 1.0f, 1.5f);
        }
    }

    private void createSculkShockwave(LivingEntity originalTarget, Player source, double echoDamage) {
        Location center = originalTarget.getLocation();
        World world = center.getWorld();
        double radius = 3.0;
        
        // Sound effect
        world.playSound(center, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.7f, 1.2f);
        
        // Particle shockwave
        for (int i = 0; i < 3; i++) {
            final int ring = i;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                double currentRadius = radius * (ring + 1) / 3.0;
                
                // Create ring particles
                for (int j = 0; j < 20; j++) {
                    double angle = 2 * Math.PI * j / 20;
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;
                    
                    Location particleLoc = center.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.DUST, particleLoc, 1,
                        new Particle.DustOptions(Color.fromRGB(10, 60, 40), 1.5f));
                    
                    // Additional smaller particles
                    world.spawnParticle(Particle.SCULK_SOUL, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                }
                
                if (ring == 2) {
                    for (Entity entity : world.getNearbyEntities(center, currentRadius, 2, currentRadius)) {
                        if (entity instanceof LivingEntity living && 
                            !entity.equals(source)) {
                            
                            // Tag other entities too, just in case
                            shockwaveDamagedEntities.add(entity.getUniqueId());
                            living.damage(echoDamage, source);
                        }
                    }
                }
            }, ring * 5L);
        }
        
        // Clear the tag after a short delay
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            shockwaveDamagedEntities.remove(originalTarget.getUniqueId());
        }, 5L); // Clear after shockwave completes
    }

    private boolean isBackstab(Player attacker, LivingEntity target) {
        
        // Get vector from target to attacker (direction from target's perspective to attacker)
        Vector targetToAttacker = attacker.getLocation().toVector()
            .subtract(target.getLocation().toVector())
            .normalize();
        
        // Get target's facing direction (if it's a mob/player)
        Vector targetFacing;
        if (target instanceof Player) {
            targetFacing = ((Player) target).getLocation().getDirection().normalize();
        } else if (target instanceof Mob) {
            targetFacing = target.getLocation().getDirection().normalize();
        } else {
            // For other living entities, use their velocity or default to forward
            targetFacing = target.getVelocity().length() > 0 ? 
                target.getVelocity().normalize() : 
                new Vector(1, 0, 0); // Default forward
        }
        
        // Calculate angle between target's facing direction and vector to attacker
        double angle = targetFacing.angle(targetToAttacker);
        
        // If angle < 90 degrees, attacker is in front of target
        // If angle > 90 degrees, attacker is behind target
        // We'll use > 100 degrees to be more forgiving
        return angle > Math.toRadians(100);
    }

    @Override
    public void onTeleport(PlayerTeleportEvent event, BeangameItem item) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false, true));
        Cooldowns.setCooldown("untargetable", uuid, 6800L);
    
        spawnSculkParticles(player.getLocation(), 3, Color.fromRGB(10, 60, 40));

    }

    private void spawnSculkParticles(Location location, int count, Color color) {
        location.getWorld().spawnParticle(Particle.DUST, location, count, 0.3, 0.3, 0.3,
            new Particle.DustOptions(color, 1.0f));
    }

    @Override
    public long getBaseCooldown() {
        return 333;
    }

    @Override
    public String getId() {
        return "sculkstepcloak";
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
        return "§bSculkstep Cloak";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3On teleport, gain Speed II and",
            "§3become untargetable for 6.8 seconds.",
            "§3On backstab, create a sculk shockwave",
            "§3that deals 25% of the original damage.",
            "",
            "§cOn Hit",
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
        return Material.BLACK_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}
