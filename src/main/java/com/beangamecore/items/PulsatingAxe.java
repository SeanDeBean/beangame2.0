package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class PulsatingAxe extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        World world = loc.getWorld();
        for (LivingEntity pulsatingaxevictim : player.getWorld().getLivingEntities()) {
            if (shouldStrikeLightning(pulsatingaxevictim, world, loc, uuid)) {
                world.strikeLightning(pulsatingaxevictim.getLocation());
            }
        }
        return true;
    }

    private boolean shouldStrikeLightning(LivingEntity entity, World world, Location origin, UUID playerUuid) {
        Location entityLoc = entity.getLocation();
        if (!entityLoc.getWorld().equals(world))
            return false;
        if (entityLoc.distance(origin) >= 12.0D)
            return false;
        if (entity.getUniqueId().equals(playerUuid))
            return false;
        if (entity instanceof Player pv && pv.getGameMode() == GameMode.SPECTATOR)
            return false;
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 25000L;
    }

    @Override
    public String getId() {
        return "pulsatingaxe";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "EWE", " AG", "R  ", r.mCFromMaterial(Material.EXPOSED_CUT_COPPER_SLAB), r.mCFromMaterial(Material.WAXED_WEATHERED_CUT_COPPER_SLAB), r.mCFromMaterial(Material.DIAMOND_AXE), r.eCFromBeangame(Key.bg("genrecheck")), r.mCFromMaterial(Material.LIGHTNING_ROD));
        return null;
    }

    @Override
    public String getName() {
        return "§bPulsating Axe";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon lightning",
            "§9on all nearby living entities within",
            "§912 blocks. Does not affect you or",
            "§9spectators.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_AXE;
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

