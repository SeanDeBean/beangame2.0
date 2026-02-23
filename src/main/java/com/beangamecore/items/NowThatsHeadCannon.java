package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;

public class NowThatsHeadCannon extends BeangameItem implements BGArmorI {
    
    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        for(int i = 0; i < 60; i+=2) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                Player delayedPlayer = Bukkit.getServer().getPlayer(uuid);
                if (delayedPlayer != null) {  // Ensure the player is still online
                    Location delayedLoc = delayedPlayer.getEyeLocation();
                    if(!onCooldown(uuid)){
                        shootProjectiles(delayedLoc, 4, uuid);
                    }
                }
            }, i);
        }
    }
    
    public static EntityType[] projectiles = new EntityType[]{
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW,
        EntityType.SNOWBALL,
        EntityType.EGG,
        EntityType.ENDER_PEARL,
        EntityType.FIREBALL,
        EntityType.SMALL_FIREBALL,
        EntityType.DRAGON_FIREBALL,
        EntityType.WITHER_SKULL,
        EntityType.SHULKER_BULLET,
        EntityType.LLAMA_SPIT,
        EntityType.FISHING_BOBBER,
        EntityType.FIREWORK_ROCKET,
        EntityType.POTION,
        EntityType.EXPERIENCE_BOTTLE,
        EntityType.WIND_CHARGE
    };
    
    public void shootProjectiles(Location loc, double radius, UUID player) {
        Boolean hit = false;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            EntityType atype = entity.getType();
            for (EntityType types : projectiles) {
                if (atype == types) {
                    Main.getPlugin().getParticleManager().particleTrail(loc, entity.getLocation(), 0, 160, 255);
                    entity.remove();
                    hit = true;
                    break;
                }
            }
        }
        if(hit){
            applyCooldown(player);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 2000;
    }

    @Override
    public String getId() {
        return "nowthatsheadcannon";
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
        return "§8Now That's Head Cannon";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Automatically destroys nearby",
            "§6projectiles within 4 blocks.",
            "§6Creates blue particle trails",
            "§6when intercepting projectiles.",
            "§62 second cooldown after each hit.",
            "",
            "§6Armor",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.CARVED_PUMPKIN;
    }

    @Override
    public int getCustomModelData() {
        return 103;
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
