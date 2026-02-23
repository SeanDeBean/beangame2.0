package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Key;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class WaWaWoodArmor extends BeangameItem implements BGRClickableI {
    
    private static final double MAX_RADIUS = 12.0;
    private static final long EXPANSION_DURATION = 20L;
    private static final long ARMOR_DURATION = 160L;

    // Store original armor sets to restore later
    private static final Map<UUID, List<ItemStack>> originalArmorSets = new HashMap<>();
    // Track which players have been transformed
    private static final Map<UUID, Long> transformedPlayers = new HashMap<>();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
       
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Cooldown check
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // Item event action
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GOAT_SCREAMING_HURT, 0.8f, 0f);

        double[] radius = {0};
        int[] taskId = new int[1];
        List<Player> affectedPlayers = new ArrayList<>();

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (radius[0] >= MAX_RADIUS) {
                // Transformation complete, start restoration timer
                startRestorationTimer(affectedPlayers);
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            radius[0] += MAX_RADIUS / EXPANSION_DURATION;

            DustOptions dustOptions = new DustOptions(Color.fromRGB(76, 43, 32), 1);
            Main.getPlugin().getParticleManager().spawnParticleSphere(player.getLocation(), radius[0], Particle.DUST, dustOptions, 100);

            // Find and transform players in the expanding radius
            for (Entity entity : player.getNearbyEntities(radius[0], radius[0], radius[0])) {
                if(entity instanceof Player target && !target.equals(player) && !affectedPlayers.contains(target) && transformedPlayers.containsKey(target.getUniqueId()) == false) {
                    transformPlayerArmor(target);
                    affectedPlayers.add(target);
                }
            }
        }, 0, 1).getTaskId();

        return true;
    }

    private void transformPlayerArmor(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Store original armor
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        List<ItemStack> originalArmor = new ArrayList<>();
        
        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorPiece = armorContents[i];
            if (armorPiece != null && !armorPiece.getType().equals(Material.AIR)) {
                originalArmor.add(armorPiece.clone());
                
                // Transform based on armor slot
                ItemStack woodArmor = createWoodArmor(i);
                armorContents[i] = woodArmor;
            } else {
                originalArmor.add(null);
            }
        }
        
        originalArmorSets.put(uuid, originalArmor);
        transformedPlayers.put(uuid, System.currentTimeMillis() + (ARMOR_DURATION * 50)); // Convert ticks to milliseconds
        player.getInventory().setArmorContents(armorContents);
        
        // Play transformation sound
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1, 1);
    }

    private ItemStack createWoodArmor(int armorSlotIndex) {
        
        String name;
        
        switch (armorSlotIndex) {
            case 0: // Boots
                name = "spruceboots";
                break;
            case 1: // Leggings
                name = "spruceleggings";
                break;
            case 2: // Chestplate
                name = "sprucechestplate";
                break;
            default: // Helmet
                name = "sprucehelmet";
                break;
        }

        // Get the BeangameItem from registry
        Optional<BeangameItem> bgItemOpt = BeangameItemRegistry.get(Key.bg(name));
        
        if (bgItemOpt.isPresent()) {
            BeangameItem w = bgItemOpt.get();
            ItemStack woodArmor = w.asItem();
            ItemMeta woodArmorMeta = woodArmor.getItemMeta();
            
            if (woodArmorMeta != null) {
                woodArmorMeta.setDisplayName("§fWa-Wa " + w.getName());
                woodArmorMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
                woodArmorMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                woodArmorMeta.setUnbreakable(true);
                woodArmor.setItemMeta(woodArmorMeta);
            }
            
            return woodArmor;
        } else {
            // Fallback if the item doesn't exist in registry
            return createFallbackWoodArmor(armorSlotIndex);
        }
    }

    private ItemStack createFallbackWoodArmor(int armorSlotIndex) {
        Material material;
        
        switch (armorSlotIndex) {
            case 0: // Boots
                material = Material.LEATHER_BOOTS;
                break;
            case 1: // Leggings
                material = Material.LEATHER_LEGGINGS;
                break;
            case 2: // Chestplate
                material = Material.LEATHER_CHESTPLATE;
                break;
            default: // Helmet
                material = Material.LEATHER_HELMET;
                break;
        }
        
        ItemStack fallbackArmor = new ItemStack(material);
        ItemMeta meta = fallbackArmor.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§fWa-Wa Wood Armor");
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            

            fallbackArmor.setItemMeta(meta);
        }
        
        return fallbackArmor;
    }



    private void startRestorationTimer(List<Player> affectedPlayers) {
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (Player player : affectedPlayers) {
                restorePlayerArmor(player);
            }
        }, ARMOR_DURATION);
    }

    private void restorePlayerArmor(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (originalArmorSets.containsKey(uuid)) {
            List<ItemStack> originalArmor = originalArmorSets.get(uuid);
            ItemStack[] currentArmor = player.getInventory().getArmorContents();
            ItemStack[] newArmor = new ItemStack[4];
            
            // Only restore non-null, non-air items
            for (int i = 0; i < 4; i++) {
                ItemStack original = (i < originalArmor.size()) ? originalArmor.get(i) : null;
                if (original != null && original.getType() != Material.AIR) {
                    newArmor[i] = original;
                } else {
                    newArmor[i] = currentArmor[i]; // Keep what they currently have
                }
            }
            
            player.getInventory().setArmorContents(newArmor);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1, 1);
            
            originalArmorSets.remove(uuid);
            transformedPlayers.remove(uuid);
        }
    }

    public void forceRestoreArmor(Player player) {
        restorePlayerArmor(player);
    }

    public void handlePlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        
        if (originalArmorSets.containsKey(uuid)) {
            // Get the original armor that was stored
            List<ItemStack> originalArmor = originalArmorSets.get(uuid);
            
            // Remove ALL wood armor from player's inventory (not just drops)
            ItemStack[] currentArmor = player.getInventory().getArmorContents();
            for (int i = 0; i < currentArmor.length; i++) {
                ItemStack piece = currentArmor[i];
                if (piece != null && piece.hasItemMeta() && piece.getItemMeta().hasDisplayName()) {
                    if (piece.getItemMeta().getDisplayName().startsWith("§fWa-Wa")) {
                        currentArmor[i] = null; // Remove wood armor
                    }
                }
            }
            player.getInventory().setArmorContents(currentArmor);
            
            // Remove wood armor from death drops
            List<ItemStack> dropsToRemove = new ArrayList<>();
            for (ItemStack drop : event.getDrops()) {
                if (drop != null && drop.hasItemMeta() && drop.getItemMeta().hasDisplayName()) {
                    if (drop.getItemMeta().getDisplayName().startsWith("§fWa-Wa")) {
                        dropsToRemove.add(drop);
                    }
                }
            }
            event.getDrops().removeAll(dropsToRemove);
            
            // Add original armor to death drops
            for (ItemStack originalPiece : originalArmor) {
                if (originalPiece != null) {
                    // Clone to avoid reference issues
                    event.getDrops().add(originalPiece.clone());
                }
            }
            
            // Clear the data
            originalArmorSets.remove(uuid);
            transformedPlayers.remove(uuid);
        }
    }


    @Override
    public long getBaseCooldown() {
        return 32000L;
    }

    @Override
    public String getId() {
        return "wawawoodarmor";
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
        return "§2Wa-Wa Wood Armor";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to create an expanding zone",
            "§9that transforms all player's armor into",
            "§9spruce wood armor for " + ARMOR_DURATION/20 + " seconds.",
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
        return Material.BROWN_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 103;
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

