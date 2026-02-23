package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.beangamecore.Main;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WitherScepter extends BeangameItem implements BGRClickableI {

    private static final List<WitherSkull> skulls = new ArrayList<>();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);

        // Cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // Item event
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (world == null) return false; // Prevent NullPointerException

        world.playSound(loc, Sound.ENTITY_WITHER_SHOOT, 1.0F, 1.0F);

        WitherSkull witherSkull = player.launchProjectile(WitherSkull.class);
        configureWitherSkull(witherSkull, player);

        skulls.add(witherSkull);

        // Schedule cleanup task for the wither skull
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (witherSkull != null && !witherSkull.isDead()) {
                witherSkull.remove();
                skulls.remove(witherSkull);
            }
        }, 200L);

        return true;
    }

    private void configureWitherSkull(WitherSkull witherSkull, Player player) {
        witherSkull.setCustomName(player.getName());
        witherSkull.setCharged(true);
        witherSkull.setIsIncendiary(true);
        witherSkull.setVelocity(player.getEyeLocation().getDirection().multiply(3));
    }

    public static void resetEffect(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof WitherSkull)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        WitherSkull skull = (WitherSkull) event.getDamager();
        LivingEntity entity = (LivingEntity) event.getEntity();

        if (skulls.contains(skull)) {
            // Apply the wither effect after a slight delay
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                if (entity != null && !entity.isDead()) {
                    entity.removePotionEffect(PotionEffectType.WITHER);
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1));
                }
            }, 1L);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 2500L;
    }

    @Override
    public String getId() {
        return "witherscepter";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " S ", "G  ", "   ", r.mCFromMaterial(Material.WITHER_SKELETON_SKULL), r.eCFromBeangame(Key.bg("ghastlystaff")));
        return null;
    }

    @Override
    public String getName() {
        return "§8Wither Scepter";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Launches charged wither skulls",
            "§9Skulls apply Wither II for 9 seconds on hit",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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

