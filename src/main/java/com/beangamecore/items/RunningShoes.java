package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.items.type.move.BGMoveArmorI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.ItemNBT;

public class RunningShoes extends BeangameItem implements BGMoveArmorI, BGArmorI {

    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        UUID id = player.getUniqueId();

        Integer protectionLevel = playerProtectionLevels.getOrDefault(id, 0);
        int level = item.getEnchantmentLevel(Enchantment.PROTECTION);
        if(level != protectionLevel){
            updateArmorProtection(player, item, protectionLevel);
        }
    }
    
    private static final int BLOCKS_PER_LEVEL = 250;
    private static final int MAX_PROTECTION_LEVEL = 10;
    
    private final Map<UUID, Double> playerDistances = new HashMap<>();
    private final Map<UUID, Integer> playerProtectionLevels = new HashMap<>();

    @Override
    public void onMoveArmor(PlayerMoveEvent event, ItemStack armor) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        
        // Check if player actually moved a block (not just rotated)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        // Initialize if first time
        if (!playerDistances.containsKey(id)) {
            playerDistances.put(id, 0.0);
            playerProtectionLevels.put(id, 0);
        }
        
        // Get current distance and protection level
        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double distanceTraveled = Math.sqrt(dx * dx + dz * dz);

        if (distanceTraveled < 0.001) {
            return;
        }

        double currentDistance = playerDistances.get(id) + 4 * distanceTraveled;
        int currentProtection = playerProtectionLevels.get(id);
        
        // Update distance
        playerDistances.put(id, currentDistance);
        
        // Check if we've reached another 1000 blocks
        if (currentDistance >= BLOCKS_PER_LEVEL) {
            // Reset distance counter
            playerDistances.put(id, 0.0);
            
            // Increase protection if not at max
            if (currentProtection < MAX_PROTECTION_LEVEL) {
                currentProtection++;
                playerProtectionLevels.put(id, currentProtection);
            }
            updateArmorProtection(player, armor, currentProtection);
        }
    }

    private void updateArmorProtection(Player player, ItemStack armor, int protectionLevel) {
        ItemMeta meta = armor.getItemMeta();
        BeangameItem bgitem = BeangameItemRegistry.getRaw(ItemNBT.getBeanGame(armor));
        if(!bgitem.getId().equals("runningshoes") || meta == null){
            return;
        }

        // Remove old protection enchantment
        meta.removeEnchant(Enchantment.PROTECTION);
        
        // Add new protection level if above 0
        if (protectionLevel > 0) {
            meta.addEnchant(Enchantment.PROTECTION, protectionLevel, true);
        }
        
        armor.setItemMeta(meta);
    }

    public void resetAllPlayers() {
        playerDistances.clear();
        playerProtectionLevels.clear();
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "runningshoes";
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
        return "§9Running Shoes";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Gains protection levels as you run.",
            "§6Every " + BLOCKS_PER_LEVEL + " blocks = +1 Protection",
            "§6Max: Protection " + MAX_PROTECTION_LEVEL,

            "",
            "§6Armor",
            "§9§obeangame",
            "§9", "§7When on Feet:", "§9+2 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of(); // Start with no enchantments
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_BOOTS;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, 
                      ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DYE);
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.WAYFINDER);
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(0, 102, 204); // Blue color for running shoes
    }

    @Override
    public int getArmor() {
        return 2;
    }

    @Override
    public EquipmentSlotGroup getSlot() {
        return EquipmentSlotGroup.FEET;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}