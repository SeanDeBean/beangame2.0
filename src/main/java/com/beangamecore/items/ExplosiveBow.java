package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameBow;
import com.beangamecore.items.type.general.BG2tTickingI;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class ExplosiveBow extends BeangameBow implements BG2tTickingI {
    
    @Override
    public void tick(){
        for(Projectile projectile : getArrows()){
            if(projectile.isOnGround() || projectile.isDead()){
                removeArrow(projectile);
            } else {
                projectile.getWorld().spawnParticle(Particle.FLAME, projectile.getLocation(), 1);
            }
        }
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        Projectile projectile = event.getEntity();
        removeArrow(projectile);
        World world = projectile.getWorld();
        final Player player = ((Player)projectile.getShooter());
        Vector direction = projectile.getVelocity().normalize();
        Location loc = projectile.getLocation().add(direction.multiply(0.4));
        projectile.remove();
        
        // Create explosion that breaks blocks but doesn't damage entities
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                world.createExplosion(loc, 1.3F, false, true, player);
            }, 2L); 
        
        // Custom radius-based damage implementation
        applyRadiusDamage(loc, player);
    }

    private void applyRadiusDamage(Location explosionCenter, Player shooter) {
        double radius = 1.3; // Match the explosion radius
        double maxDamage = 6.0; // Adjust this value to balance damage
        double minDamage = 1.0; // Minimum damage at edge of radius
        
        // Get all nearby entities
        Collection<Entity> nearbyEntities = explosionCenter.getWorld().getNearbyEntities(explosionCenter, radius, 2*radius, radius);
        
        for (Entity entity : nearbyEntities) {
            // Don't damage the shooter
            if (entity.equals(shooter)) continue;
            
            // Only damage living entities (players, mobs, etc.)
            if (!(entity instanceof LivingEntity)) continue;

            if(entity instanceof Player p){
                Cooldowns.setCooldown("explosion_immunity", p.getUniqueId(), 100L);
            }
            
            LivingEntity target = (LivingEntity) entity;
            
            // Calculate distance from explosion center
            double distance = entity.getLocation().distance(explosionCenter);
            
            // If within radius, apply scaled damage
            if (distance <= radius) {
                // Calculate damage based on distance (more damage closer to center)
                double damage = maxDamage - ((distance / radius) * (maxDamage - minDamage));
                
                DamageSource.Builder builder = DamageSource.builder(DamageType.PLAYER_EXPLOSION);
                builder.withDirectEntity(shooter);
                EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(shooter, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, builder.build(), damage);
                
                // Call the event so other plugins can modify it
                Bukkit.getPluginManager().callEvent(damageEvent);
                
                // Apply damage if not cancelled
                if (!damageEvent.isCancelled()) {
                    target.damage(damageEvent.getFinalDamage(), shooter);

                    Vector knockback = target.getLocation().toVector().subtract(explosionCenter.toVector()).normalize();

                    boolean hasKBResistance = false;
                    if(target instanceof Player){
                        Player pVictim = (Player) target;
                        hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.EXPLOSION_KNOCKBACK_RESISTANCE) != null && 
                                pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                    }
                    if (!hasKBResistance) {
                        target.setVelocity(knockback.multiply(0.4));
                    }
                }
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "explosivebow";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " TS", "R S", " TS", r.mCFromMaterial(Material.TNT), r.mCFromMaterial(Material.STRING), r.eCFromBeangame(Key.bg("tntimer")));
        return null;
    }

    @Override
    public String getName() {
        return "§cExplosive Bow";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§eFires arrows that explode on impact",
            "§ewith a 1.3 block radius. Creates",
            "§eflame particle trails while flying",
            "§eand breaks blocks in the explosion.",
            "",
            "§eRanged",
            "§dOn Hit Applier",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BOW;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
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

