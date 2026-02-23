package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGCastableI;
import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.items.type.damage.BGCancelFallDmgHeldI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Grapple extends BeangameItem implements BGMobilityI, BGCastableI, BGCancelFallDmgHeldI {
    
    @Override
    public void onFish(PlayerFishEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(event.getState().equals(PlayerFishEvent.State.IN_GROUND)){
            // item event
            Location grappledifference = event.getHook().getLocation().subtract(player.getLocation());

            if(!(player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
                player.setVelocity(grappledifference.toVector().multiply(0.32));
                Cooldowns.setCooldown("fall_damage_immunity", uuid, 4000L);
            }   

        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "grapple";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "  D", " SR", "H R", r.eCFromBeangame(Key.bg("dash")), r.mCFromMaterial(Material.STICK), r.mCFromMaterial(Material.STRING), r.eCFromBeangame(Key.bg("luckyhorseshoe")));
        return null;
    }

    @Override
    public String getName() {
        return "§6Grapple";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§fHook the ground to pull",
            "§fyourself toward the location.",
            "§fGrants fall damage immunity",
            "§fduring grapple movement.",
            "",
            "§fMovement",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.FISHING_ROD;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

