package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDReceiverInvI;
import com.beangamecore.items.type.talisman.BGInvUnstackable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class EmotionalSupportAnimal extends BeangameItem implements BGDReceiverInvI, BGRClickableI, BGInvUnstackable {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        event.setCancelled(true);
        return true;
    }

    public int count(LivingEntity le){
        AtomicInteger force = new AtomicInteger(0);
        if(le instanceof Player p){
            for(ItemStack item : p.getInventory().getContents()){
                if(this.asItem().isSimilar(item)){
                    force.set(force.get() + 1);
                }
            }
        } else {
            return 1;
        }
        return force.get();
    }
    
    @Override
    public void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        int count = count((LivingEntity)event.getEntity()) - 1;
        double probabillity = 0.21 * Math.pow(1.04, count);
        if(Math.random() < probabillity){
            event.setDamage(0.0D);

            DustOptions dust = new DustOptions(Color.fromRGB(255, 105, 180), 1.2f); // size 1.2

            for (int j = 0; j < 12; j++) { // spawn 12 particles each tick
                double offsetX = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5
                double offsetY = Math.random() * 1.5;        // random 0 to 1.5 (height)
                double offsetZ = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5

                Location particleLoc = event.getEntity().getLocation().clone().add(offsetX, offsetY, offsetZ);

                event.getEntity().getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dust);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "emotionalsupportanimal";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " G ", "HBH", " A ", r.mCFromMaterial(Material.GOLDEN_APPLE), r.mCFromMaterial(Material.HONEYCOMB), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.AXOLOTL_BUCKET));
        return null;
    }

    @Override
    public String getName() {
        return "§dEmotional Support Animal";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3When hit, has a chance to completely",
            "§3negate damage. Chance increases with",
            "§3additional Emotional Support Animals",
            "§3in inventory. Creates pink particle",
            "§3effects when damage is blocked.",
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
        return Material.AXOLOTL_BUCKET;
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

