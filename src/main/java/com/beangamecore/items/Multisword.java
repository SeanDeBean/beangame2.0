package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;

public class Multisword extends BeangameItem implements BGDDealerHeldI, BGLPTalismanI {
    
    // All enchantments available in Minecraft 1.21.4
    private static final List<Enchantment> ALL_ENCHANTMENTS = Arrays.asList(
        // Damage Enchantments
        Enchantment.SHARPNESS,
        Enchantment.SMITE,
        Enchantment.BANE_OF_ARTHROPODS,
        
        // Utility Enchantments
        Enchantment.KNOCKBACK,
        Enchantment.FIRE_ASPECT,
        Enchantment.LOOTING,
        Enchantment.SWEEPING_EDGE,
        Enchantment.UNBREAKING,
        Enchantment.MENDING,
        
        // Tool Enchantments (can go on swords too)
        Enchantment.FORTUNE,
        Enchantment.SILK_TOUCH,
        Enchantment.EFFICIENCY,
        
        // Armor Enchantments (some can go on swords too)
        Enchantment.PROTECTION,
        Enchantment.FIRE_PROTECTION,
        Enchantment.BLAST_PROTECTION,
        Enchantment.PROJECTILE_PROTECTION,
        Enchantment.FEATHER_FALLING,
        Enchantment.RESPIRATION,
        Enchantment.AQUA_AFFINITY,
        Enchantment.THORNS,
        Enchantment.DEPTH_STRIDER,
        Enchantment.FROST_WALKER,
        Enchantment.SOUL_SPEED,
        Enchantment.SWIFT_SNEAK,
        
        // Fishing Rod Enchantments
        Enchantment.LUCK_OF_THE_SEA,
        Enchantment.LURE
    );
    
    // Maximum levels for enchantments
    private static final Map<Enchantment, Integer> MAX_LEVELS = new HashMap<>();
    static {
        // Damage
        MAX_LEVELS.put(Enchantment.SHARPNESS, 5);
        MAX_LEVELS.put(Enchantment.SMITE, 5);
        MAX_LEVELS.put(Enchantment.BANE_OF_ARTHROPODS, 5);
        
        // Utility
        MAX_LEVELS.put(Enchantment.KNOCKBACK, 2);
        MAX_LEVELS.put(Enchantment.FIRE_ASPECT, 2);
        MAX_LEVELS.put(Enchantment.LOOTING, 3);
        MAX_LEVELS.put(Enchantment.SWEEPING_EDGE, 3);
        MAX_LEVELS.put(Enchantment.UNBREAKING, 3);
        MAX_LEVELS.put(Enchantment.MENDING, 1);
        
        // Tools
        MAX_LEVELS.put(Enchantment.FORTUNE, 3);
        MAX_LEVELS.put(Enchantment.SILK_TOUCH, 1);
        MAX_LEVELS.put(Enchantment.EFFICIENCY, 5);
        
        // Armor
        MAX_LEVELS.put(Enchantment.PROTECTION, 4);
        MAX_LEVELS.put(Enchantment.FIRE_PROTECTION, 4);
        MAX_LEVELS.put(Enchantment.BLAST_PROTECTION, 4);
        MAX_LEVELS.put(Enchantment.PROJECTILE_PROTECTION, 4);
        MAX_LEVELS.put(Enchantment.FEATHER_FALLING, 4);
        MAX_LEVELS.put(Enchantment.RESPIRATION, 3);
        MAX_LEVELS.put(Enchantment.AQUA_AFFINITY, 1);
        MAX_LEVELS.put(Enchantment.THORNS, 3);
        MAX_LEVELS.put(Enchantment.DEPTH_STRIDER, 3);
        MAX_LEVELS.put(Enchantment.FROST_WALKER, 2);
        MAX_LEVELS.put(Enchantment.SOUL_SPEED, 3);
        MAX_LEVELS.put(Enchantment.SWIFT_SNEAK, 3);
        
        // Fishing
        MAX_LEVELS.put(Enchantment.LUCK_OF_THE_SEA, 3);
        MAX_LEVELS.put(Enchantment.LURE, 3);
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Map<Enchantment, Integer> enchantments = meta.getEnchants();
        if (enchantments.isEmpty()) return;

        List<Enchantment> weightedEnchantments = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchant = entry.getKey();
            int weight = (enchant == Enchantment.SHARPNESS) ? 5 : 1;
            
            // Add the enchantment multiple times based on weight
            for (int i = 0; i < weight; i++) {
                weightedEnchantments.add(enchant);
            }
        }

        Enchantment enchantToRemove = weightedEnchantments.get(
            ThreadLocalRandom.current().nextInt(weightedEnchantments.size())
        );

        int currentLevel = meta.getEnchantLevel(enchantToRemove);

        // Remove or decrease level
        if (currentLevel <= 1) {
            // Remove completely if level 1 or less
            meta.removeEnchant(enchantToRemove);
        } else {
            // Decrease by 1 level
            meta.removeEnchant(enchantToRemove);
            meta.addEnchant(enchantToRemove, currentLevel - 1, true);
        }

        item.setItemMeta(meta);
    }
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        LivingEntity damager = (LivingEntity) event.getDamager();
        
        // Apply cooldown only for players
        if (damager instanceof Player p) {
            UUID uuid = p.getUniqueId();
            if (onCooldown(uuid)) {
                return;
            }
            applyCooldown(uuid);
        }
        
        // Cycle sword material
        Material next = getNextSwordMaterial(item.getType());
        if (next != null) {
            item.setType(next);
        }
        
        // Add random enchantment
        addRandomEnchantment(item, damager);
    }
    
    private void addRandomEnchantment(ItemStack item, LivingEntity damager) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        // Get all possible enchantments that can be applied to this item
        List<Enchantment> applicableEnchantments = new ArrayList<>();
        for (Enchantment enchant : ALL_ENCHANTMENTS) {
            if (enchant.canEnchantItem(item) || ThreadLocalRandom.current().nextBoolean()) {
                applicableEnchantments.add(enchant);
            }
        }
        
        if (applicableEnchantments.isEmpty()) return;
        
        // Pick random enchantment
        Enchantment randomEnchant = applicableEnchantments.get(
            ThreadLocalRandom.current().nextInt(applicableEnchantments.size())
        );
        
        // Get current level and max level
        int currentLevel = meta.getEnchantLevel(randomEnchant);
        int maxLevel = MAX_LEVELS.getOrDefault(randomEnchant, 1);
        
        // Increase level (cap at max)
        int newLevel = Math.min(currentLevel + 1, maxLevel);
        
        // Add or update enchantment
        meta.removeEnchant(randomEnchant);
        if (newLevel > 0) {
            meta.addEnchant(randomEnchant, newLevel, true);
        }
        
        item.setItemMeta(meta);
        
        // Visual feedback
        if (damager instanceof Player player) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.0f);
            player.getWorld().spawnParticle(Particle.ENCHANT, 
                player.getEyeLocation(), 10, 0.3, 0.3, 0.3, 0.1);
        }
    }
    
    private Material getNextSwordMaterial(Material current) {
        List<Material> swordCycle = Arrays.asList(
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD
        );

        int index = swordCycle.indexOf(current);
        if (index == -1 || index == swordCycle.size() - 1) {
            return swordCycle.get(0); // wrap around to first
        }
        return swordCycle.get(index + 1);
    }
    
    @Override
    public long getBaseCooldown() {
        return 333L;
    }

    @Override
    public String getId() {
        return "multisword";
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
        return "§bMultisword";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cCycles through sword materials",
            "§con each hit: wood → stone → gold",
            "§c→ iron → diamond → netherite.",
            "§cAlso gains +1 level of a random",
            "§cenchantment on every successful hit.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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
    public EquipmentSlotGroup getSlot() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
