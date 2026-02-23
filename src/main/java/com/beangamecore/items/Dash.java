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

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Dash extends BeangameItem implements BGMobilityI, BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        event.setCancelled(true);
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
        player.setInvisible(true);
        world.playSound(loc, Sound.ENTITY_FOX_TELEPORT, 1.0F, 1.0F);
        world.spawnParticle(Particle.CLOUD, loc, 10);
        if(!(player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7)){
            player.setVelocity(loc.getDirection().multiply(1.6D));
            Cooldowns.setCooldown("fall_damage_immunity", uuid, 4000L);
        }
        // delayed events
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            Location loc2 = player.getLocation();
            player.setInvisible(false);
            world.playSound(loc2, Sound.ENTITY_FOX_TELEPORT, 1.0F, 1.0F);
            world.spawnParticle(Particle.CLOUD, loc2, 10);
        }, 25L);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 3500L;
    }

    @Override
    public String getId() {
        return "dash";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "FEF", "EBE", "FEF", r.mCFromMaterial(Material.FEATHER), r.mCFromMaterial(Material.ENDER_PEARL), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§5Dash";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§fRight-click to dash forward in your",
            "§flooking direction with high velocity.",
            "§fGrants invisibility during the dash and",
            "§ffall damage immunity for 2 seconds.",
            "",
            "§fMovement",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.ENDER_PEARL;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
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

