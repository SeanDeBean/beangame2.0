package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGMobilityI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class LeapingSword extends BeangameItem implements BGMobilityI, BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // cooldown system
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // item event
        Location loc = player.getLocation();
        World world = player.getWorld();
        world.playSound(loc, Sound.BLOCK_ANVIL_LAND, 1.0F, 2.0F);
        world.spawnParticle(Particle.FLASH, loc, 10);
        if(!(player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
            player.setVelocity(loc.getDirection().multiply(1.6D).setY(1.2D));
            Cooldowns.setCooldown("fall_damage_immunity", uuid, 6500L);
        }
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 5500L;
    }

    @Override
    public String getId() {
        return "leapingsword";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "  D", " H ", "S  ", r.eCFromBeangame(Key.bg("dash")), r.eCFromBeangame(Key.bg("luckyhorseshoe")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public String getName() {
        return "§4Leaping Sword";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§fRight-click to leap forward and",
            "§fupward in the direction you're facing.",
            "§fGrants fall damage immunity for",
            "§f6.5 seconds after leaping.",
            "",
            "§fMovement",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.GOLDEN_SWORD;
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

