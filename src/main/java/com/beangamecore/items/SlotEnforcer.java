package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class SlotEnforcer extends BeangameItem implements BGRClickableI {
    
    // Effect colors - mystical purple/blue theme
    private static final Color COLOR_WAVE = Color.fromRGB(150, 0, 255);      // Deep purple
    private static final Color COLOR_WAVE_OUTER = Color.fromRGB(100, 0, 200); // Darker purple
    private static final Color COLOR_LOCK = Color.fromRGB(255, 215, 0);      // Gold lock
    private static final Color COLOR_SPARK = Color.fromRGB(200, 150, 255);   // Light purple spark
    
    public void slotenforcerSlotChange(PlayerItemHeldEvent event){
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long slotenforcervictimcdr = Cooldowns.getRemainingCooldown("slot_enforced", uuid) / 1000L;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§eSlot enforced for another " + slotenforcervictimcdr + " second(s)!"));
        
        // Visual feedback when trying to switch slots
        spawnLockDenialEffect(player);
    }

    private void spawnLockDenialEffect(Player player) {
        World world = player.getWorld();
        Location loc = player.getEyeLocation();
        
        // X shape indicating locked
        Vector right = loc.getDirection().crossProduct(new Vector(0, 1, 0)).normalize();
        Vector up = right.clone().crossProduct(loc.getDirection()).normalize();
        
        for (int i = 0; i < 5; i++) {
            double t = i / 4.0;
            double offset = 0.3 + t * 0.4;
            
            // X shape
            Location x1 = loc.clone().add(right.clone().multiply(offset)).add(up.clone().multiply(offset));
            Location x2 = loc.clone().add(right.clone().multiply(-offset)).add(up.clone().multiply(offset));
            Location x3 = loc.clone().add(right.clone().multiply(offset)).add(up.clone().multiply(-offset));
            Location x4 = loc.clone().add(right.clone().multiply(-offset)).add(up.clone().multiply(-offset));
            
            world.spawnParticle(Particle.DUST, x1, 1, new Particle.DustOptions(COLOR_LOCK, 1.0f));
            world.spawnParticle(Particle.DUST, x2, 1, new Particle.DustOptions(COLOR_LOCK, 1.0f));
            world.spawnParticle(Particle.DUST, x3, 1, new Particle.DustOptions(COLOR_LOCK, 1.0f));
            world.spawnParticle(Particle.DUST, x4, 1, new Particle.DustOptions(COLOR_LOCK, 1.0f));
        }
        
        // Small shake sound
        world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.8f);
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        World world = player.getWorld();
        Location loc = player.getLocation();
        
        // Caster activation effect
        spawnCasterActivationEffect(player, loc, world);
        
        for (Player slotenforcervictim : Bukkit.getOnlinePlayers()) {
            UUID vuuid = slotenforcervictim.getUniqueId();
            Location vloc = slotenforcervictim.getLocation();
            if (isSlotEnforcerTarget(loc, uuid, slotenforcervictim, vloc)) {
                spawnVictimLockEffect(slotenforcervictim, vloc, world);
                Cooldowns.setCooldown("slot_enforced", vuuid, 9000L);
            }
        }
        return true;
    }

    private void spawnCasterActivationEffect(Player player, Location loc, World world) {
        // Expanding wave effect from caster
        world.playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.0F);
        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 0.6F, 1.5F);
        
        // Ground ring that expands
        for (int ring = 0; ring < 3; ring++) {
            final int currentRing = ring;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                double radius = 4 + currentRing * 8; // 4, 12, 20
                int particles = (int) (radius * 8);
                
                for (int i = 0; i < particles; i++) {
                    double angle = 2 * Math.PI * i / particles;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    
                    Location ringLoc = loc.clone().add(x, 0.1, z);
                    Color ringColor = (currentRing == 0) ? COLOR_WAVE : COLOR_WAVE_OUTER;
                    float size = 1.5f - (currentRing * 0.3f);
                    
                    world.spawnParticle(Particle.DUST, ringLoc, 1, 
                        new Particle.DustOptions(ringColor, size));
                    
                    // Rising sparks
                    if (i % 4 == 0) {
                        Location sparkLoc = ringLoc.clone().add(0, 1 + Math.random(), 0);
                        world.spawnParticle(Particle.DUST, sparkLoc, 1, 0.1, 0.3, 0.1, 
                            new Particle.DustOptions(COLOR_SPARK, 0.8f));
                    }
                }
            }, ring * 5L);
        }
        
        // Caster aura burst
        for (int i = 0; i < 30; i++) {
            double angle = 2 * Math.PI * i / 30;
            double radius = 0.8;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            for (int y = 0; y < 3; y++) {
                Location auraLoc = loc.clone().add(x, y * 0.8, z);
                world.spawnParticle(Particle.DUST, auraLoc, 1, 
                    new Particle.DustOptions(COLOR_WAVE, 1.2f));
            }
        }
        
        // Enchantment table letters effect above caster
        world.spawnParticle(Particle.ENCHANT, loc.clone().add(0, 2.5, 0), 50, 0.5, 0.5, 0.5, 0.5);
    }

    private void spawnVictimLockEffect(Player victim, Location vloc, World world) {
        // Sound
        world.playSound(vloc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.0F);
        world.playSound(vloc, Sound.BLOCK_IRON_DOOR_CLOSE, 0.8F, 1.2F);
        
        // Chains wrapping around
        for (int i = 0; i < 16; i++) {
            double angle = 2 * Math.PI * i / 16;
            double radius = 0.6;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            // Two chain rings at different heights
            world.spawnParticle(Particle.DUST, vloc.clone().add(x, 0.8, z), 1, 
                new Particle.DustOptions(COLOR_WAVE_OUTER, 0.9f));
            world.spawnParticle(Particle.DUST, vloc.clone().add(x, 1.4, z), 1, 
                new Particle.DustOptions(COLOR_WAVE_OUTER, 0.9f));
        }
        
        // Pulse effect that repeats during the lock duration
        for (int pulse = 0; pulse < 9; pulse++) {
            final int currentPulse = pulse;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (!victim.isOnline()) return;
                
                Location currentLoc = victim.getLocation();
                float intensity = 1.0f - (currentPulse / 9.0f);
                
                // Ring pulse
                for (int i = 0; i < 12; i++) {
                    double angle = 2 * Math.PI * i / 12;
                    double x = Math.cos(angle) * 0.8;
                    double z = Math.sin(angle) * 0.8;
                    
                    world.spawnParticle(Particle.DUST, currentLoc.clone().add(x, 0.1, z), 1, 
                        new Particle.DustOptions(COLOR_WAVE, 0.8f * intensity));
                }
            }, pulse * 20L); // Every second for 9 seconds
        }
    }

    private boolean isSlotEnforcerTarget(Location loc, UUID uuid, Player slotenforcervictim, Location vloc) {
        UUID vuuid = slotenforcervictim.getUniqueId();
        return vloc.getWorld().equals(loc.getWorld()) && vloc.distance(loc) < 24.0D && !vuuid.equals(uuid)
                && slotenforcervictim.getGameMode().equals(GameMode.SURVIVAL);
    }

    @Override
    public long getBaseCooldown() {
        return 55000L;
    }

    @Override
    public String getId() {
        return "slotenforcer";
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
        return "§5Slot Enforcer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to lock all nearby",
            "§9players' hotbar slots for 9",
            "§9seconds. Prevents them from",
            "§9switching held items.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_INGOT;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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