package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameBow;
import com.beangamecore.items.type.general.BG2tTickingI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class IcicleBow extends BeangameBow implements BG2tTickingI {
    
    @Override
    public void tick(){
        for(Projectile projectile : getArrows()){
            projectile.getWorld().spawnParticle(Particle.SNOWFLAKE, projectile.getLocation(), 3);
        }
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        removeArrow(projectile);

        // Get the block where the arrow hit
        Location hitLocation = projectile.getLocation();
        Block baseBlock = hitLocation.getBlock();

        placeIceCube(baseBlock);

        // Apply effects to entities
        Entity evictim = event.getHitEntity();
        applyEntityEffects(evictim);
    }

    private void placeIceCube(Block baseBlock) {
        // Place a 2x2x2 cube of ICE blocks
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    placeIceIfAir(baseBlock, dx, dy, dz);
                }
            }
        }
    }

    private void placeIceIfAir(Block baseBlock, int dx, int dy, int dz) {
        Block block = baseBlock.getRelative(dx, dy, dz);
        if (block.getType() == Material.AIR) {
            block.setType(Material.PACKED_ICE);
        }
    }

    private void applyEntityEffects(Entity evictim) {
        // Apply effects to entities
        if (evictim instanceof LivingEntity lvictim) {
            lvictim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
            lvictim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 1));
            lvictim.setVelocity(new Vector(0, 0, 0));
        }
        if (evictim instanceof Player victim) {
            victim.setFreezeTicks(120);
            victim.playEffect(victim.getLocation(), Effect.STEP_SOUND, Material.ICE);
            victim.playEffect(victim.getEyeLocation(), Effect.STEP_SOUND, Material.ICE);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "iciclebow";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " PS", "IBS", " PS", r.mCFromMaterial(Material.PACKED_ICE), r.mCFromMaterial(Material.STRING), r.mCFromMaterial(Material.ICE), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§bIcicle Bow";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§eShoots arrows that freeze players",
            "§eand create packed ice cubes on impact.",
            "§eApplies slowness, mining fatigue,",
            "§eand slows target movement.",
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
        return 101;
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

