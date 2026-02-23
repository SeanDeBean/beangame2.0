package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.BGDamageHeldI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.projectiles.ProjectileSource;

import com.beangamecore.Main;

public class Telemole extends BeangameItem implements BGRClickableI, BGProjectileI, BGDamageHeldI {

    @Override
    public void onDamageHeldItem(EntityDamageEvent event, ItemStack item) {
        if(event.getCause().equals(DamageCause.SUFFOCATION) && isInsideBorder(event.getEntity())){
            event.setCancelled(true);
        }
    }

    public boolean isInsideBorder(Entity entity) {
        WorldBorder border = entity.getWorld().getWorldBorder();
        Location center = border.getCenter();
        double size = border.getSize() / 2.0; // Size is diameter, we need radius
        
        double x = entity.getLocation().getX() - center.getX();
        double z = entity.getLocation().getZ() - center.getZ();
        
        // Check if within square border (vanilla default)
        return Math.abs(x) < size && Math.abs(z) < size;
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {
        ProjectileSource source = event.getEntity().getShooter();
        if(source instanceof Player){
            Player user = (Player) source;
            Location start = user.getLocation();
            Location end = event.getEntity().getLocation().subtract(0, 3, 0);
            end.setPitch(start.getPitch());
            end.setYaw(start.getYaw());
            event.getEntity().remove();
            World world = end.getWorld();
            for(int i = 0; i < 3; i++){
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    start.subtract(0, 1, 0);
                    user.teleport(start);
                    world.playSound(start, Sound.BLOCK_ROOTED_DIRT_BREAK, 1, 1);
                }, i * 4L);
            }
            for(int i = 0; i < 3; i++){

                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    end.add(0, 1, 0);
                    user.teleport(end);
                    world.playSound(end, Sound.BLOCK_ROOTED_DIRT_BREAK, 1, 1);
                }, 12L + i * 4L);
            }
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        launchProjectile(this, player, Snowball.class);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 22000L;
    }

    @Override
    public String getId() {
        return "telemole";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "E S", "SDL", " C ", r.mCFromMaterial(Material.ENDER_PEARL), r.mCFromMaterial(Material.IRON_SHOVEL), r.eCFromBeangame(Key.bg("drit")), r.mCFromMaterial(Material.LADDER), r.mCFromMaterial(Material.TRAPPED_CHEST));
        return null;
    }

    @Override
    public String getName() {
        return "§5telemole";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to shoot a projectile that",
            "§9creates a tunnel to teleport through.",
            "§9Grants immunity to suffocation damage.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.POPPED_CHORUS_FRUIT;
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

