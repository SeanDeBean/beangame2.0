package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGProjectileI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.recipes.RecipeAPI;
import com.beangamecore.util.Key;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;

public class GhostBridge extends BeangameItem implements BGRClickableI, BGProjectileI, BeangameSoftItem {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        if (onCooldown(event.getPlayer().getUniqueId())) {
            return false;
        }
        applyCooldown(event.getPlayer().getUniqueId());
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1, 1);
        stack.setAmount(stack.getAmount() - 1);
        WindCharge w = launchProjectile(this, event.getPlayer(), WindCharge.class);
        w.setGravity(false);
        startBlockChangeTask(event, w);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            final WindCharge wc = w;
            if (wc != null && wc.isValid())
                wc.explode();
        }, 80);

        return true;
    }

    private void startBlockChangeTask(PlayerInteractEvent event, WindCharge w) {
        final Player player = event.getPlayer();
        final WindCharge wc = w;
        
        // Use array for task ID tracking
        int[] taskId = new int[1];
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (shouldApplyBlockChange(player, wc)) {
                Location loc = wc.getLocation();
                // Store the original block data
                player.sendBlockChange(loc, Material.GLASS.createBlockData());

                // Revert the block back to its original state after a delay (e.g., 5 seconds)
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    player.sendBlockChange(loc, loc.getBlock().getBlockData());
                }, 200);
            } else if (wc == null || !wc.isValid()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
            }
        }, 1, 1).getTaskId();
    }

    private boolean shouldApplyBlockChange(Player player, WindCharge wc) {
        return wc != null && player.getLocation().distance(wc.getLocation()) > 2 && wc.isValid();
    }
    
    @Override
    public long getBaseCooldown() {
        return 750L;
    }

    @Override
    public String getId() {
        return "ghostbridge";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public int getCraftingAmount(){
        return 4;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, " G ", "GSG", " G ", r.mCFromMaterial(Material.GLASS), r.eCFromBeangame(Key.bg("soul")));
    }

    @Override
    public String getName() {
        return ChatColor.AQUA+"Ghost Bridge";
    }

    @Override
    public List<String> getLore() {
        return List.of(ChatColor.BLUE+"beangame");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.WIND_CHARGE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 8;
    }

    @Override
    public void onProjHit(ProjectileHitEvent event) {

    }

}

