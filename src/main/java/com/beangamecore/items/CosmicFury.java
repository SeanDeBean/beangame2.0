package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGLClickableI;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class CosmicFury extends BeangameItem implements BGLClickableI, BGDDealerHeldI {

    static Random CosmicFuryRandomizer = new Random();

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity damager = (LivingEntity) event.getDamager();
        UUID uuid = damager.getUniqueId();
        if (onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);
        LivingEntity entity = (LivingEntity) event.getEntity();
        Location eloc = entity.getLocation();
        entity.getWorld().playSound(eloc, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1F, 1);
        // item effect
        for(int i = 0; i < CosmicFuryRandomizer.nextInt(10) + 5; i++){
            Location end = eloc.clone();
            Location start = eloc.clone();
            start.add(CosmicFuryRandomizer.nextInt(30) - 15, CosmicFuryRandomizer.nextInt(15) + 35, CosmicFuryRandomizer.nextInt(30) - 15);
            CosmicFallingStar.summon(start, end, damager);
        }
    }
    
    @Override
    public void onLeftClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1F, 1);
        // item effect
        for(int i = 0; i < CosmicFuryRandomizer.nextInt(10) + 5; i++){
            Location end = player.getLocation().clone();
            end.add(end.getDirection().normalize().multiply(5.5));
            Location start = player.getLocation().clone();
            start.add(CosmicFuryRandomizer.nextInt(30) - 15, CosmicFuryRandomizer.nextInt(15) + 35, CosmicFuryRandomizer.nextInt(30) - 15);
            CosmicFallingStar.summon(start, end, player);
        }
    }

    public void cosmicFuryUpdateStars() {
        List<CosmicFallingStar> toRemove = new ArrayList<>();

        for (CosmicFallingStar star : CosmicFallingStar.fallingStars) {
            updateStar(star, toRemove);
        }

        // Remove all expired or collided stars after iteration
        if (!toRemove.isEmpty()) {
            removeStars(toRemove);
        }
    }

    private void updateStar(CosmicFallingStar star, List<CosmicFallingStar> toRemove) {
        star.starParticles();
        star.tickFallingStars();

        if (shouldRemoveStar(star)) {
            toRemove.add(star);
            return;
        }

        damageNearbyEntities(star);
    }

    private boolean shouldRemoveStar(CosmicFallingStar star) {
        // Ensure item display is valid before proceeding
        if (star.getItemDisplay() == null) {
            return true;
        }

        Location loc = star.getItemDisplay().getLocation();
        Material blockType = loc.getBlock().getType();

        // Remove star if it has been alive too long or has hit a solid block
        return star.getTicksAlive() > 100 || (blockType != Material.AIR && blockType != Material.WATER);
    }

    private void damageNearbyEntities(CosmicFallingStar star) {
        Location loc = star.getItemDisplay().getLocation();
        List<LivingEntity> nearby = loc.getWorld().getLivingEntities();
        for (LivingEntity entity : nearby) {
            if (shouldDamageEntity(star, loc, entity)) {
                Bukkit.getScheduler().runTask(Main.getPlugin(), () -> {
                    if (canApplyDamage(star, entity)) {
                        entity.damage(5, star.getOwner());
                    }
                });
            }
        }
    }

    private boolean canApplyDamage(CosmicFallingStar star, LivingEntity entity) {
        return star.getOwner() != null && entity.isValid() && !entity.isDead();
    }

    private boolean shouldDamageEntity(CosmicFallingStar star, Location loc, LivingEntity entity) {
        return !entity.getUniqueId().equals(star.getOwner().getUniqueId())
                && entity.getLocation().distance(loc) <= 1.5;
    }

    private void removeStars(List<CosmicFallingStar> toRemove) {
        for (CosmicFallingStar star : toRemove) {
            star.remove(); // Ensure proper cleanup
        }
        CosmicFallingStar.fallingStars.removeAll(toRemove);
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " C ", " B ", " S ", r.eCFromBeangame(Key.bg("cosmicingot")), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STICK));
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 2000L;
    }

    @Override
    public String getId() {
        return "cosmicfury";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§5Cosmic Fury";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cLeft-click to summon 5-15 falling stars",
            "§cin front of you. Hitting enemies summons",
            "§c5-15 stars that rain down around them.",
            "§cStars deal 2.5 hearts of damage on impact.",
            "",
            "§cOn Hit",
            "§dOn Hit Extenders",
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
        return 102;
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
