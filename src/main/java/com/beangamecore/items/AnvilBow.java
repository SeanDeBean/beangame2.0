package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameBow;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;

public class AnvilBow extends BeangameBow {
    
    @Override
    public void onProjHit(ProjectileHitEvent event) {
        event.setCancelled(true);
        
        Projectile projectile = event.getEntity();
        projectile.remove();
        
        Location loc = projectile.getLocation();
        World world = loc.getWorld();
        int roundedX = Math.round((float) loc.getX());
        int roundedY = Math.round((float) loc.getY()) + 9; // Move blocks 9 blocks above impact
        int roundedZ = Math.round((float) loc.getZ());
        
        for (int x = roundedX - 1; x <= roundedX + 1; x++) {
            for (int z = roundedZ - 1; z <= roundedZ + 1; z++) {
                Location blockLoc = new Location(world, x, roundedY, z);
                Block block = blockLoc.getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.ANVIL);
                }
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "anvilbow";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "AAA", "AWA", "ABA", r.mCFromMaterial(Material.ANVIL), r.mCFromMaterial(Material.BOW), BeangameItemRegistry.getRaw(Key.bg("bean")).asExactRecipeChoice());
        return null;
    }

    @Override
    public String getName() {
        return "§8Anvil Bow";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§eFires arrows that summon a cluster",
            "§eof anvils above the impact location,",
            "§ecrushing anything below.",
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
        return 103;
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

