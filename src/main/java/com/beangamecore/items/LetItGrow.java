package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;
import com.beangamecore.entities.livingtree.TreeComponentFactory;
import com.beangamecore.entities.livingtree.TreeGenerationConfig;
import com.beangamecore.entities.livingtree.TreeGenerator;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.BGDamageHeldI;


public class LetItGrow extends BeangameItem implements BGRClickableI, BGDamageHeldI {
    
    private static final TreeGenerator treeGenerator = new TreeGenerator(
        TreeGenerationConfig.getDefault(),
        new TreeComponentFactory()
    );

    @Override
    public void onDamageHeldItem(EntityDamageEvent event, ItemStack item) {
        if(event.getCause().equals(DamageCause.SUFFOCATION) && isInsideBorder(event.getEntity())){
            event.setCancelled(true);
        }
    }

    public boolean isInsideBorder(Entity entity) {
        WorldBorder border = entity.getWorld().getWorldBorder();
        Location center = border.getCenter();
        double size = border.getSize() / 2.0; // Size is diameter, we need radius
        
        double x = entity.getLocation().getX() - center.getX();
        double z = entity.getLocation().getZ() - center.getZ();
        
        // Check if within square border (vanilla default)
        return Math.abs(x) < size && Math.abs(z) < size;
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item) {
        Player player = event.getPlayer();
        
        if (isOnCooldown(player)) {
            sendCooldownMessage(player);
            return false;
        }
        
        applyCooldown(player);
        executeLaunchSequence(player);
        return true;
    }

    private void executeLaunchSequence(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 9, false, false));
        scheduleTreeSpawn(player);
    }

    private void scheduleTreeSpawn(Player player) {
        Location spawnLocation = player.getLocation();

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            treeGenerator.spawnTree(player, spawnLocation);
        }, 8L);
    }

    @Override
    public long getBaseCooldown() { return 16000; }

    @Override
    public String getId() { return "letitgrow"; }

    @Override
    public String getName() { return "§2Let It Grow"; }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to spawn a tree at your",
            "§9location, granting Resistance 10 for 2 seconds.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Material getMaterial() { return Material.OAK_SAPLING; }

    @Override
    public int getCustomModelData() { return 101; }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public int getMaxStackSize() { return 1; }

    // Required interface implementations
    @Override
    public boolean isInItemRotation() { return false; }

    @Override
    public CraftingRecipe getCraftingRecipe() { return null; }

    @Override
    public ArmorTrim getArmorTrim() { return null; }

    @Override
    public Color getColor() { return null; }

    @Override
    public int getArmor() { return 0; }

    @Override
    public EquipmentSlotGroup getSlot() { return null; }

    // Private helper methods
    private boolean isOnCooldown(Player player) {
        return onCooldown(player.getUniqueId());
    }

    private void applyCooldown(Player player) {
        applyCooldown(player.getUniqueId());
    }

}
