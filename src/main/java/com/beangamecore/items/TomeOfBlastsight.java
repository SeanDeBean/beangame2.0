package com.beangamecore.items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.BGDamageInvI;
import com.beangamecore.items.type.talisman.BGHPTalismanI;
import com.beangamecore.items.type.talisman.BGInvUnstackable;

public class TomeOfBlastsight extends BeangameItem implements BGHPTalismanI, BGDamageInvI, BGInvUnstackable {

    private static final Map<Integer, TextDisplay> activeDisplays = new HashMap<>(); // TNT entity ID -> TextDisplay
    private static final Set<Integer> tntToShow = new HashSet<>(); // TNT that should have displays this tick
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        // Find TNT near THIS specific player
        int radius = 30;
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        for (Entity entity : world.getNearbyEntities(playerLoc, radius, radius, radius)) {
            if (entity instanceof TNTPrimed tnt) {
                // Mark this TNT to have a display
                tntToShow.add(tnt.getEntityId());
            }
        }
    }
    
    // This should be called from your main plugin EVERY TICK
    public void updateAllDisplays() {
        // First, remove displays for TNT that are no longer marked OR no longer exist
        Iterator<Map.Entry<Integer, TextDisplay>> iterator = activeDisplays.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TextDisplay> entry = iterator.next();
            int tntId = entry.getKey();
            TextDisplay display = entry.getValue();
            
            // Remove display if:
            // 1. TNT is not marked to show this tick, OR
            // 2. TNT no longer exists, OR  
            // 3. Display is dead
            if (!tntToShow.contains(tntId) || !tntExists(tntId) || display == null || display.isDead()) {
                if (display != null && !display.isDead()) {
                    display.remove();
                }
                iterator.remove();
            }
        }
        
        // Create displays for newly marked TNT
        for (int tntId : tntToShow) {
            if (!activeDisplays.containsKey(tntId)) {
                TNTPrimed tnt = findTNTById(tntId);
                if (tnt != null && !tnt.isDead()) {
                    createDisplayForTNT(tnt);
                }
            }
        }
        
        // Update text for all active displays
        for (Map.Entry<Integer, TextDisplay> entry : activeDisplays.entrySet()) {
            int tntId = entry.getKey();
            TextDisplay display = entry.getValue();
            
            TNTPrimed tnt = findTNTById(tntId);
            if (tnt != null && !tnt.isDead() && display != null && !display.isDead()) {
                updateDisplayText(tnt, display);
            }
        }
        
        // Clear the set for next tick
        tntToShow.clear();
    }
    
    private static void createDisplayForTNT(TNTPrimed tnt) {
        int tntId = tnt.getEntityId();
        Location tntLoc = tnt.getLocation();
        Location displayLoc = tntLoc.clone().add(0, 1.2, 0);
        
        // Create the display
        TextDisplay display = (TextDisplay) tnt.getWorld().spawnEntity(
            displayLoc, 
            EntityType.TEXT_DISPLAY
        );
        
        // Configure display
        display.setBillboard(Display.Billboard.CENTER);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        display.setTextOpacity((byte) 200);
        display.setSeeThrough(true);
        display.setShadowed(true);
        display.setPersistent(false);
        display.setInvulnerable(true);
        
        // Store reference
        activeDisplays.put(tntId, display);
    }
    
    private static void updateDisplayText(TNTPrimed tnt, TextDisplay display) {
        int fuseTicks = tnt.getFuseTicks();
        double seconds = fuseTicks / 20.0;
        
        if (fuseTicks <= 0 || tnt.isDead()) {
            display.setText("§cBOOM!");
            return;
        }
        
        String color;
        if (seconds > 2.0) {
            color = "§a";
        } else if (seconds > 1.0) {
            color = "§e";
        } else {
            color = "§c";
        }
        
        String timerText = color + String.format("%.1f", seconds) + "s";
        display.setText(timerText);

        display.teleport(tnt.getLocation().clone().add(0, 1.2, 0));
        
        if (seconds < 0.5) {
            byte opacity = (byte) ((Math.sin(System.currentTimeMillis() / 50.0) * 55) + 200);
            display.setTextOpacity(opacity);
        } else {
            display.setTextOpacity((byte) 200);
        }
    }
    
    private static TNTPrimed findTNTById(int tntId) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getEntityId() == tntId && entity instanceof TNTPrimed tnt) {
                    return tnt;
                }
            }
        }
        return null;
    }
    
    private static boolean tntExists(int tntId) {
        return findTNTById(tntId) != null;
    }
    
    // Cleanup when plugin disables
    public static void cleanupAllDisplays() {
        for (TextDisplay display : activeDisplays.values()) {
            if (display != null && !display.isDead()) {
                display.remove();
            }
        }
        activeDisplays.clear();
        tntToShow.clear();
    }




    public int count(LivingEntity le){
        AtomicInteger force = new AtomicInteger(0);
        if(le instanceof Player p){
            for(ItemStack item : p.getInventory().getContents()){
                if(this.asItem().isSimilar(item)){
                    force.set(force.get() + 1);
                }
            }
        } else {
            return 1;
        }
        return force.get();
    }

    // runs when a player takes damage with this item
    @Override
    public void onDamageInventory(EntityDamageEvent event, ItemStack item) {

        // Check if damage is from explosion
        DamageCause cause = event.getCause();
        boolean isExplosive = cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION;
        
        if (!isExplosive) {
            return; // Only apply reduction to explosive damage
        }

        int count = count((LivingEntity)event.getEntity()) - 1;

        // Calculate damage reduction: base 20% + 10% per additional stack
        double baseReduction = 0.2; // 20%
        double stackReduction = 0.1 * count; // 10% per additional stack
        double totalReduction = baseReduction + stackReduction;
        
        // Cap at 80% reduction (20% base + 60% from 6 stacks = 80% max)
        totalReduction = Math.min(totalReduction, 0.8);
        
        double damage = event.getDamage();
        double newDamage = damage * (1 - totalReduction);
        
        event.setDamage(newDamage);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "tomeofblastsight";
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
        return "§6Tome of Blastsight";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Reduces incoming blast damage by 20%",
            "§3Marks nearby TNT with explosion timers",
            "",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:blast_protection", 7);
    }

    @Override
    public Material getMaterial() {
        // Changed from SUGAR to ENCHANTED_BOOK for "tome" aesthetic
        return Material.ENCHANTED_BOOK;
    }

    @Override
    public int getCustomModelData() {
        return 0; // Changed from 101 to avoid conflict
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
    public EquipmentSlotGroup getSlot() {
        return null; // Talisman items typically don't have equipment slots
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
