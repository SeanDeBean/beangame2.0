package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DrunicEdge extends BeangameItem implements BGDDealerHeldI {
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item){
        if(!(event.getEntity() instanceof LivingEntity)){
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
        snakeEffect(event.getDamager().getLocation(), entity.getLocation());
    }

    private void snakeEffect(Location start, Location end) {
        Vector mainDirection = end.toVector().subtract(start.toVector()).normalize();
        
        // Find perpendicular direction (90-degree rotation on Y-axis)
        Vector perpendicular = new Vector(-mainDirection.getZ(), 0, mainDirection.getX()).normalize().multiply(1.5);

        // Compute new start positions
        Location leftStart = start.clone().add(perpendicular);  // Shifted left
        Location rightStart = start.clone().subtract(perpendicular); // Shifted right

        double maxHeight = Math.max(start.getY(), end.getY()) + 1.7; 

        // createSnakeTrail(start, end, maxHeight, dustOptions, random);
        createSnakeTrail(leftStart, end, maxHeight - 0.5);
        createSnakeTrail(rightStart, end, maxHeight - 0.5);
    }

    private void createSnakeTrail(Location start, Location end, double maxHeight) {
        DustOptions dustOptions = new DustOptions(Color.fromRGB(2, 48, 32), 1);
        Random random = new Random();

        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();
    
        for (double d = 0; d < length; d += 0.125) {
            double t = d / length; // Normalized position (0 to 1)
            double parabolicHeight = (4 * maxHeight - 4 * (start.getY() + (end.getY() - start.getY()) * t)) * t * (1 - t);
    
            Vector offset = new Vector(
                (random.nextDouble() - 0.5) * 0.3, // Horizontal randomness
                parabolicHeight + (random.nextDouble() - 0.5) * 0.5, // Arc height + vertical randomness
                (random.nextDouble() - 0.5) * 0.3 // Horizontal randomness
            );
    
            Location current = start.clone().add(direction.clone().multiply(d)).add(offset);
            start.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "drunicedge";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " PP", "SBP", "TS ", r.mCFromMaterial(Material.PUFFERFISH), r.mCFromMaterial(Material.SLIME_BLOCK), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§2§lDrunic Edge";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting enemies applies Poison I for",
            "§c5 seconds on hit.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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

