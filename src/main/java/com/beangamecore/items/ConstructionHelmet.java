package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.particles.BeangameParticleManager;
import com.beangamecore.util.Cooldowns;

public class ConstructionHelmet extends BeangameItem implements BGArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        double radius = 3.0;
        UUID uuid = player.getUniqueId();
        Location ploc = player.getLocation().add(0, 0.5, 0);
        World world = player.getWorld();
        BeangameParticleManager particleManager = Main.getPlugin().getParticleManager();
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation().add(0, 1, 0), radius, radius,
                radius)) {
            if (isEligibleTarget(entity, uuid)) {
                UUID vuuid = entity.getUniqueId();
                Cooldowns.setCooldown("use_item", vuuid, 3400L);
                Location eloc = entity.getLocation().add(0, 0.5, 0);
                particleManager.particleTrail(ploc, eloc, 255, 255, 0);
                world.playSound(eloc, Sound.BLOCK_ANVIL_USE, 0.1f, 1);
            }
        }
    }

    private boolean isEligibleTarget(Entity entity, UUID uuid) {
        return entity instanceof Player && entity.getUniqueId() != uuid
                && !((Player) entity).getGameMode().equals(GameMode.SPECTATOR);
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "constructionhelmet";
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
        return "§eConstruction Helmet";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Creates a construction zone around the",
            "§6wearer that prevents nearby players",
            "§6from using items for 3.4 seconds.",
            "",
            "§6Armor",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.CARVED_PUMPKIN;
    }

    @Override
    public int getCustomModelData() {
        return 104;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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
        return EquipmentSlotGroup.HEAD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

