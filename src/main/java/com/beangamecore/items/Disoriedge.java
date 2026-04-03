package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.general.BGResetableI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class Disoriedge extends BeangameItem implements BGDDealerHeldI, BGResetableI {

    private static final Map<UUID, Boolean> knockLeftNext = new HashMap<>();
    private static final Vector UPWARD_COMPONENT = new Vector(0, 0.25, 0);

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if(victim instanceof Player){
            Player pVictim = (Player) victim;
            if (pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7){ 
                return;
            }
        }

        UUID victimId = victim.getUniqueId();
        if(onCooldown(victimId)) return;
        setCooldown(victimId, getBaseCooldown());

        performPerpendicularShove(attacker, victim, true);
        
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (areEntitiesValid(attacker, victim)) {
                    performPerpendicularShove(attacker, victim, false);
                }
            }, 11L); // 11 ticks = 0.5 seconds after first shove
        }
    
        private boolean areEntitiesValid(LivingEntity attacker, LivingEntity victim) {
            return victim.isValid() && !victim.isDead() 
                && attacker.isValid() && !attacker.isDead();
    }

    private void performPerpendicularShove(LivingEntity attacker, LivingEntity victim, boolean isFirstHit) {
        Location attackerLoc = attacker.getLocation();
        Vector facing = attackerLoc.getDirection().normalize();

        Vector perpendicular = new Vector(-facing.getZ(), 0, facing.getX()).normalize();

        UUID attackerId = attacker.getUniqueId();
        
        boolean knockLeft;
        if (isFirstHit) {
            knockLeft = knockLeftNext.compute(attackerId, (k, v) -> v == null ? true : !v);
        } else {
            knockLeft = !knockLeftNext.getOrDefault(attackerId, true);
        }

        if (!knockLeft) perpendicular.multiply(-1);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                Vector knockback = perpendicular.multiply(0.8).add(UPWARD_COMPONENT);
                victim.setVelocity(knockback);
            }, 1L);

        World world = attacker.getWorld();
        Location victimLoc = victim.getLocation();

        playEffects(world, victimLoc, knockLeft, isFirstHit);
    }

    private void playEffects(World world, Location location, boolean knockLeft, boolean isFirstHit) {
        Particle.DustOptions leftColor = new Particle.DustOptions(Color.fromRGB(255, 105, 180), 1.5f);  // Hot Pink
        Particle.DustOptions rightColor = new Particle.DustOptions(Color.fromRGB(30, 0, 50), 1.5f);     // Very Dark Purple
        
        Particle.DustOptions currentColor = knockLeft ? leftColor : rightColor;
        
        Location center = location.add(0, 1.2, 0);
        
        int particles = isFirstHit ? 12 : 8; // Fewer particles for second hit
        double arcSize = isFirstHit ? 1.5 : 1.2; // Smaller arc for second hit
        
        double arcStart = knockLeft ? 120 : 60; // degrees
        double arcEnd = knockLeft ? 240 : 300;  // degrees
        
        for (int i = 0; i < particles; i++) {
            double progress = (double) i / (particles - 1);
            double angle = Math.toRadians(arcStart + (arcEnd - arcStart) * progress);
            
            double x = Math.cos(angle) * arcSize;
            double z = Math.sin(angle) * arcSize;
            
            world.spawnParticle(Particle.DUST, 
                center.clone().add(x, 0, z), isFirstHit ? 3 : 2, 0.1, 0.1, 0.1, 0, currentColor);
        }
        
        if (isFirstHit) {
            world.playSound(location, Sound.ENTITY_SHULKER_TELEPORT, 0.7f, knockLeft ? 1.2f : 0.8f);
            world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, knockLeft ? 0.7f : 0.5f);
        } else {
            world.playSound(location, Sound.ENTITY_SHULKER_TELEPORT, 0.5f, knockLeft ? 1.1f : 0.9f);
            world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, knockLeft ? 0.8f : 0.6f);
        }
        
        world.spawnParticle(Particle.SWEEP_ATTACK, center, isFirstHit ? 8 : 6, 0.4, 0.4, 0.4, 0.03);
        
        for (int i = 0; i < (isFirstHit ? 5 : 3); i++) {
            double offsetX = (Math.random() - 0.5) * 0.8;
            double offsetZ = (Math.random() - 0.5) * 0.8;
            world.spawnParticle(Particle.DUST, 
                center.clone().add(offsetX, 0.3, offsetZ), 2, 0, 0, 0, 0, currentColor);
        }
    }

    @Override
    public void resetItem() {
        knockLeftNext.entrySet().removeIf(entry -> 
            Bukkit.getPlayer(entry.getKey()) == null
        );
    }
    
    @Override
    public long getBaseCooldown() {
        return 2200;
    }

    @Override
    public String getId() {
        return "disoriedge";
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
        return "§5§lDisoriedge";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting enemies knocks them perpendicular",
            "§cto your facing direction, alternating left",
            "§cand right with each hit. Applies a second",
            "§cknockback 0.5 seconds later in the opposite",
            "§cdirection for disorienting combos.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 109;
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

