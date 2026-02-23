package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StaffOfHunger extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        double rad = 16;
        for (Player staffofhungervictim : Bukkit.getOnlinePlayers()) {
            if (isEligibleForHunger(staffofhungervictim, loc, uuid, rad)) {
                staffofhungervictim.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 45, 250));
            }
        }

        DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 0, 0), 2);
        Main.getPlugin().getParticleManager().spawnParticleSphere(player.getLocation(), rad, Particle.DUST, dustOptions, 500);

        return true;
    }

    private boolean isEligibleForHunger(Player staffofhungervictim, Location loc, UUID uuid, double rad) {
        Location vloc = staffofhungervictim.getLocation();
        return vloc.getWorld().equals(loc.getWorld()) && vloc.distance(loc) < rad
                && !staffofhungervictim.getUniqueId().equals(uuid)
                && staffofhungervictim.getGameMode().equals(GameMode.SURVIVAL);
    }

    @Override
    public long getBaseCooldown() {
        return 52000L;
    }

    @Override
    public String getId() {
        return "staffofhunger";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "  E", " F ", "K  ", r.mCFromMaterial(Material.FERMENTED_SPIDER_EYE), r.eCFromBeangame(Key.bg("feast")), r.mCFromMaterial(Material.DRIED_KELP));
        return null;
    }

    @Override
    public String getName() {
        return "§6Staff of Hunger";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to inflict extreme hunger",
            "§9on all nearby players within 16 blocks.",
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
        return Material.WOODEN_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
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

