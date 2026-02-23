package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class KnockupGlove extends BeangameItem implements BGDDealerHeldI {
   
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack stack){
        Entity victim = event.getEntity();
        Location vloc = victim.getLocation();

        if(!(victim instanceof LivingEntity)){
            return;
        }

        boolean hasKBResistance = false;
        if(victim instanceof Player){
            Player pVictim = (Player) victim;
            hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                    pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
        }
        if (hasKBResistance) {
            return;
        }

        if(victim instanceof Player){
            UUID victimUUID = victim.getUniqueId();
            if(onCooldown(victimUUID)){
                return;
            }
            setCooldown(victimUUID, getBaseCooldown());
        }

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Vector newVelocity = victim.getVelocity().add(new Vector(0, 1.45, 0));
            victim.setVelocity(newVelocity);
        }, 1L);

        playKnockupEffects(vloc);
        
    }

    private void playKnockupEffects(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1, 0), 1, 0.1, 0.1, 0.1, 0.1);
        world.spawnParticle(Particle.CLOUD, loc, 8, 0.5, 0.5, 0.5, 0.1);
        world.playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 0.6f, 1.2f);
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "WRW", "SBS", "P P", r.mCFromMaterial(Material.WIND_CHARGE), r.mCFromMaterial(Material.RED_DYE), r.mCFromMaterial(Material.SLIME_BLOCK), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.STICKY_PISTON));
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 1000;
    }

    @Override
    public String getId() {
        return "knockupglove";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§cKnockup Glove";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cMelee hits launch enemies upward",
            "§cwith strong vertical knockback.",
            "§cPlayers can't be launched again",
            "§cfor 1 second after being hit.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.RED_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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

