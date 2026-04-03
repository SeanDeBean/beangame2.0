package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameBow;
import com.beangamecore.items.type.general.BG2tTickingI;

import java.util.List;
import java.util.Map;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class Blowbow extends BeangameBow implements BG2tTickingI {
    
    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        Projectile projectile = event.getEntity();
        removeArrow(projectile);
        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();
        for (Entity blowbowvictim : world.getEntities()) {
            if(blowbowvictim.getLocation().distance(loc) < 6){
                Location vloc = blowbowvictim.getLocation();
                vloc.setY(vloc.getY() + 0.01);
                blowbowvictim.teleport(vloc);
                boolean hasKBResistance = false;
                if(blowbowvictim instanceof Player){
                    Player pVictim = (Player) blowbowvictim;
                    hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                            pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                }
                if(!hasKBResistance){
                    blowbowvictim.setVelocity(blowbowvictim.getVelocity().add(new Vector(0, 1.5, 0)));
                }
            }
        }
        projectile.remove();
    }
    
    @Override
    public void tick(){
        for(Projectile projectile : getArrows()){
            if(projectile.isOnGround() || projectile.isDead()){
                removeArrow(projectile);
            } else {
                projectile.getWorld().spawnParticle(Particle.SNOWFLAKE, projectile.getLocation(), 1);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "blowbow";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " RC", "RBC", " RC", r.mCFromMaterial(Material.BREEZE_ROD), r.mCFromMaterial(Material.WIND_CHARGE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§fBlowbow";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§eFires arrows that create a powerful",
            "§eupward blast when they land, launching",
            "§enearby entities high into the air.",
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

