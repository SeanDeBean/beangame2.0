package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;

public class BloodSniffer extends BeangameItem implements BGDDealerHeldI, BGMPTalismanI {

    private static final int TRACKING_RANGE = 24;
    private static final double LOW_HEALTH_THRESHOLD = 0.3; // 30% health or less


    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        UUID attackerUUID = attacker.getUniqueId();
        
        if(onCooldown(attackerUUID) || !isLowHealth(victim)){
            return;
        }
        applyCooldown(attackerUUID);
        
        // blood hit effects
        createBloodHitEffect(victim.getLocation());
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        int range = TRACKING_RANGE;
        
        // Track nearby players with low health
        for (Player target : player.getWorld().getPlayers()) {
            if (target != player && 
                target.getGameMode() != GameMode.SPECTATOR &&
                player.getLocation().distance(target.getLocation()) <= range &&
                isLowHealth(target)) {
                
                createBloodTrail(player.getLocation(), target.getLocation());
                
                // Grant speed boost when moving toward low health targets
                Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector());
                Vector playerVelocity = player.getVelocity();
                
                if (toTarget.normalize().dot(playerVelocity.normalize()) > 0.7) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
                }
            }
        }
    }

    private void createBloodTrail(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        
        for (double d = 0; d < Math.min(distance, 10); d += 0.5) {
            Location trailLoc = from.clone().add(direction.clone().multiply(d));
            trailLoc.add(0, 0.1, 0);
            
            from.getWorld().spawnParticle(Particle.DUST, trailLoc, 1, 
                new DustOptions(Color.fromRGB(170, 0, 0), 0.8f));
        }
    }

    private void createBloodHitEffect(Location location) {
        location.getWorld().spawnParticle(Particle.BLOCK, location.add(0, 1, 0), 
            10, 0.3, 0.3, 0.3, 0.1, Material.REDSTONE_BLOCK.createBlockData());
    }

    private boolean isLowHealth(LivingEntity player) {
        double currentHealth = player.getHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        return (currentHealth / maxHealth) <= LOW_HEALTH_THRESHOLD;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "bloodsniffer";
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
        return "§4Blood Sniffer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Reveals blood trails to nearby players",
            "§3with low health (30% or less). Grants",
            "§3speed when moving toward wounded targets",
            "§3and creates blood effects on hitting them.",
            "",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.SNIFFER_EGG;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

