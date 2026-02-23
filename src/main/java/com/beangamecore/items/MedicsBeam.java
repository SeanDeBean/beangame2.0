package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.beangamecore.Main;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.util.Vector;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;

public class MedicsBeam extends BeangameItem implements BGArmorI {

    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        if (!player.getGameMode().equals(GameMode.SPECTATOR) && player.isSneaking()) {
            performHealingEffect(player);
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                if (canPerformHealingEffect(player)) {
                    performHealingEffect(player);
                }
            }, 30L);
        }
    }

    private boolean canPerformHealingEffect(Player player) {
        return player.isOnline() 
            && !player.isDead() 
            && !player.getGameMode().equals(GameMode.SPECTATOR) 
            && player.isSneaking();
    }

    private void performHealingEffect(Player player) {
        Collection<Entity> nearbyEntities = getEntitiesInLine(player);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId())) {
                
                LivingEntity livingEntity = (LivingEntity) entity;
                double maxHealth = livingEntity.getAttribute(Attribute.MAX_HEALTH).getValue();
                double prevhealth = livingEntity.getHealth();
                double newHealth = Math.min(prevhealth + 3, maxHealth);
                livingEntity.setHealth(newHealth);

                // self heal if heal is effective
                if(prevhealth != newHealth){
                    maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                    newHealth = Math.min(player.getHealth() + 1, maxHealth);
                    player.setHealth(newHealth);
                }
            }
        }
    }

    private Collection<Entity> getEntitiesInLine(Player player){
        Collection<Entity> entities = new ArrayList<>();
        Vector direction = player.getEyeLocation().getDirection().normalize(); // Get direction player is looking at
        Location start = player.getEyeLocation().clone().subtract(0, 0.3, 0);
        World world = player.getWorld();
        
        Location end = player.getEyeLocation().clone().add(direction.clone().multiply(10)).subtract(0, 0.3, 0);
        Main.getPlugin().getParticleManager().particleTrail(start.clone(), end.clone(), 0, 255, 0);

        // Traverse a line in the direction the player is looking
        for (int i = 1; i <= 10; i++) {
            Location loc = start.clone().add(direction.clone().multiply(i)); // Adjust the location along the direction vector
        
            Collection<Entity> nearbyEntities = world.getNearbyEntities(loc, 0.8, 0.8, 0.8);
        
            for (Entity entity : nearbyEntities) {
                if (!entities.contains(entity)) {
                    entities.add(entity);
                }
            }
        }

        return entities;
    }

    @Override
    public long getBaseCooldown() {
        return 0L;
    }

    @Override
    public String getId() {
        return "medicsbeam";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " G ", "LC ", " B ", r.mCFromMaterial(Material.GOLDEN_APPLE), r.mCFromMaterial(Material.LIGHTNING_ROD), r.mCFromMaterial(Material.IRON_CHESTPLATE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§aMedic's Beam";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§6Hold sneak to fire a healing beam",
            "§6that heals entities in its path for",
            "§61.5 hearts and heals you for 0.5",
            "§6hearts per successful heal.",
            "",
            "§6Armor",
            "§aSupport",
            "§9§obeangame",
            "§9", "§7When on Body:", "§9+3 Armor"

        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:mending", 1, "minecraft:protection", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.LEATHER_CHESTPLATE;
    }

    @Override
    public int getCustomModelData() {
        return 104;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DYE);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.EMERALD, TrimPattern.FLOW);
    }

    @Override
    public Color getColor() {
        return Color.fromRGB(0, 255, 0);
    }

    @Override
    public int getArmor(){
        return 3;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.CHEST;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

