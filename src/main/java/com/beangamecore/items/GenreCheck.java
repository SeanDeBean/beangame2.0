package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameBow;
import com.beangamecore.items.type.general.BG2tTickingI;

import java.util.List;
import java.util.Map;

import org.bukkit.*;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class GenreCheck extends BeangameBow implements BG2tTickingI {
    
    @Override
    public void tick(){
        for(Projectile projectile : getArrows()){
            if(projectile.isOnGround() || projectile.isDead()){
                removeArrow(projectile);
            } else {
                projectile.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, projectile.getLocation(), 1);
            }
        }
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        Projectile projectile = event.getEntity();
        removeArrow(projectile);
        Location loc = projectile.getLocation();
        World world = loc.getWorld();
        projectile.remove();
        world.strikeLightning(loc);
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "genrecheck";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " SR", "SBR", " SR", r.mCFromMaterial(Material.STICK), r.mCFromMaterial(Material.LIGHTNING_ROD), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§7Genre Check?";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§eShoots arrows that summon lightning",
            "§eon impact.",
            "",
            "§eRanged",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.BOW;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
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

