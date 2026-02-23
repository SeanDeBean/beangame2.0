package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.items.type.BGFlightArmorI;
import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.items.type.damage.BGCancelFallDmgArmorI;
import com.beangamecore.util.Booleans;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class GracefulWaders extends BeangameItem implements BGMobilityI, BGArmorI, BGFlightArmorI, BGCancelFallDmgArmorI {
    
    @Override
    public void onToggleFlightArmor(PlayerToggleFlightEvent event, ItemStack stack){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // item event
        Booleans.setBoolean("gracefulwaders_active", uuid, false);
        Location loc = player.getLocation();
        World world = player.getWorld();

        if(!(player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
            player.setVelocity(loc.getDirection().multiply(1.35).setY(1));
        }
        world.spawnParticle(Particle.CLOUD, loc, 5);
        world.playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 1.0F, -1.0F);
    }

    @Override
    public void applyArmorEffects(Player player, ItemStack item) {
        if(!player.isFlying()){
            player.setAllowFlight(true);
            Booleans.setBoolean("gracefulwaders_active", player.getUniqueId(), true);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "gracefulwaders";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "   ", "H D", "C C", r.eCFromBeangame(Key.bg("luckyhorseshoe")),  r.eCFromBeangame(Key.bg("dash")), r.mCFromMaterial(Material.WIND_CHARGE));
        return null;
    }

    @Override
    public String getName() {
        return "§fGraceful Waders";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§fGrants double jump ability. Jump",
            "§fwhile in air to launch forward and",
            "§fupward. Prevents fall damage.",
            "",
            "§6Armor",
            "§fMovement",
            "§9§obeangame",
            "§9", "§7When on Feet:", "§9+1 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.CHAINMAIL_BOOTS;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.WILD);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 1;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.FEET;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

