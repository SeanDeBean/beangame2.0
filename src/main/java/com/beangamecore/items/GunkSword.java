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

public class GunkSword extends BeangameItem implements BGDDealerHeldI {

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack itemStack){ // attacker, victim, weapon -> gunksword
        LivingEntity victim = (LivingEntity) event.getEntity();
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 25, 0));
        
        if(event.getDamager() instanceof Player damager){
            damager.playEffect(victim.getLocation(), Effect.STEP_SOUND, Material.BLACK_CONCRETE);
            damager.playEffect(victim.getEyeLocation(), Effect.STEP_SOUND, Material.BLACK_CONCRETE);
        }
        if(victim instanceof Player player){
            player.playEffect(victim.getLocation(), Effect.STEP_SOUND, Material.BLACK_CONCRETE);
            player.playEffect(victim.getEyeLocation(), Effect.STEP_SOUND, Material.BLACK_CONCRETE);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "gunksword";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " S ", " S ", " B ", r.mCFromMaterial(Material.INK_SAC), r.eCFromBeangame(Key.bg("coldwetsock")));
        return null;
    }

    @Override
    public String getName() {
        return "§2Gunksword";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cApplies slowness for 4 seconds and",
            "§cblindness for 1.25 seconds to enemies",
            "§con hit with melee attacks.",
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
        return Material.STONE_SWORD;
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

