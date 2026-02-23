package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDReceiverInvI;
import com.beangamecore.items.type.talisman.BGHPTalismanI;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffectType;

public class CosmicEye extends BeangameItem implements BGDReceiverInvI, BGRClickableI, BGHPTalismanI {
    
    private static final Random random = new Random();

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        if(player.hasPotionEffect(PotionEffectType.BLINDNESS)){
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if(player.hasPotionEffect(PotionEffectType.DARKNESS)){
            player.removePotionEffect(PotionEffectType.DARKNESS);
        }
    }

    @Override
    public void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        UUID uuid = livingEntity.getUniqueId();
        if (onCooldown(uuid)){
            return;
        }
        applyCooldown(uuid);
        for(int i = 0; i < Math.min(28, Math.pow(Math.max(1, event.getFinalDamage()/2), 2)); i++){
            Location start = cosmiceyeGetRandomLocation(livingEntity, 5).add(0, random.nextInt(15) + 35, 0);
            Location end = cosmiceyeGetRandomLocation(livingEntity, 6);
            CosmicFallingStar.summon(start, end, livingEntity);
        }
    }

    private Location cosmiceyeGetRandomLocation(LivingEntity le, double radius){
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * radius;

        double x = le.getLocation().getX() + distance * Math.cos(angle);
        double z = le.getLocation().getZ() + distance * Math.sin(angle);
        double y = le.getLocation().getY();

        return new Location(le.getWorld(), x, y, z);
    }

    @Override
    public long getBaseCooldown() {
        return 500L;
    }

    @Override
    public String getId() {
        return "cosmiceye";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "   ", " E ", " C ", r.eCFromBeangame(Key.bg("externalsight")), r.eCFromBeangame(Key.bg("cosmicingot")));
        return null;
    }

    @Override
    public String getName() {
        return "§eCosmic Eye";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Grants immunity to blindness and darkness.",
            "§3When hit, summons falling stars based on",
            "§3damage taken. More damage creates more",
            "§3stars that rain down around you.",
            "",
            "§3Talisman",
            "§9§obeangame"
        );
    }
    
    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.ENDER_EYE;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        return true;
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

