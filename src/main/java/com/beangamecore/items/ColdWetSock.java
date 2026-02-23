package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ColdWetSock extends BeangameItem implements BGDDealerHeldI {

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity entity = (LivingEntity) event.getEntity();
        entity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 3));
        entity.setFreezeTicks(90);

        if(event.getDamager() instanceof Player damager){
            damager.playEffect(entity.getLocation(), Effect.STEP_SOUND, Material.SNOW);
            damager.playEffect(entity.getEyeLocation(), Effect.STEP_SOUND, Material.SNOW);
        }
        if(entity instanceof Player player){
            player.playEffect(entity.getLocation(), Effect.STEP_SOUND, Material.SNOW);
            player.playEffect(entity.getEyeLocation(), Effect.STEP_SOUND, Material.SNOW);
        }
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " PI", "FBP", "SF ", r.mCFromMaterial(Material.PACKED_ICE), r.mCFromMaterial(Material.ICE), r.mCFromMaterial(Material.PUFFERFISH_BUCKET), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.IRON_SWORD));
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "coldwetsock";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§bCold Wet Sock";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting enemies applies Mining Fatigue II,",
            "§cNausea IV, and freezing for 4.5 seconds.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
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

