package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.BGDamageInvI;
import com.beangamecore.items.type.talisman.BGHeldTalismanI;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AngelicShield extends BeangameItem implements BGHeldTalismanI, BGDamageInvI {
    
    private static final int MAX_ABSORPTION = 12; // 10 hearts = 20 absorption points
    private static final Map<UUID, Boolean> wasHoldingLastTick = new HashMap<>();

    @Override
    public void onDamageInventory(EntityDamageEvent event, ItemStack item) {
        applyCooldown(event.getEntity().getUniqueId());
    }

    @Override
    public void applyHeldTalismanEffects(Player player, ItemStack item) {
        UUID playerId = player.getUniqueId();
        
        // Check if player was not holding this item last tick
        boolean wasHolding = wasHoldingLastTick.getOrDefault(playerId, false);

        if (!wasHolding) {
            // Player just started holding the item, apply cooldown
            applyCooldown(playerId);
        }
        
        // Update tracking for next tick
        wasHoldingLastTick.put(playerId, true);
        
        // Check if item is on cooldown
        if (onCooldown(playerId)) {
            return;
        }
        
        // Get current absorption level
        double currentAbsorption = player.getAbsorptionAmount();
        
        // If absorption is less than max and can regenerate, restore it
        if (currentAbsorption < MAX_ABSORPTION && getRemainingCooldown(playerId) % 1000 <= 50) {
            player.setAbsorptionAmount(currentAbsorption + 1);
            DustOptions dust = new DustOptions(Color.fromRGB(255, 234, 0), 1.2f); // size 1.2
            World w = player.getWorld();

            // no particles if player is in spectator mode
            if(!player.getGameMode().equals(GameMode.SPECTATOR)){
                for (int j = 0; j < 12; j++) { // spawn 12 particles each tick
                    double offsetX = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5
                    double offsetY = Math.random() * 1.5;        // random 0 to 1.5 (height)
                    double offsetZ = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5

                    Location particleLoc = player.getLocation().clone().add(offsetX, offsetY, offsetZ);

                    w.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dust);
                }
            }

            w.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.6f, 1f);
        }
        
        // Grant fire resistance if player has absorption
        if (currentAbsorption > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false, false));
        }
    }

    @Override
    public void resetNonHoldingPlayers(Set<UUID> currentlyHoldingPlayers) {
        // Remove players who are no longer holding the item
        wasHoldingLastTick.entrySet().removeIf(entry -> 
            !currentlyHoldingPlayers.contains(entry.getKey())
        );
    }

    @Override
    public long getBaseCooldown() {
        return 8000;
    }

    @Override
    public String getId() {
        return "angelicshield";
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
        return "§eAngelic Shield";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§bGrants a large shield that regenerates",
            "§bwhen out of combat. Provides fire",
            "§bresistance while shield is active.",
            "",
            "§bHeld Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.SHIELD;
    }

    @Override
    public int getCustomModelData() {
        return 8000;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

