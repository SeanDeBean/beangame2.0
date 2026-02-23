package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;

import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class ButchersKnife extends BeangameItem implements BGDDealerHeldI {
    
    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity victim = (LivingEntity) event.getEntity();
        AttributeInstance attribute = victim.getAttribute(Attribute.MAX_HEALTH);
        if(victim.getHealth() / attribute.getBaseValue() <= 0.25){
            event.setDamage(attribute.getBaseValue());
            Player attacker = null;
            if (event.getDamager() instanceof Player){
                attacker = (Player) event.getDamager();
            }
            if(victim instanceof Player){
                ((Player) victim).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§4Executed."));
            }

            if (attacker != null) {
                createExecuteXEffect(attacker, victim.getLocation());
            }
        }
    }

    private void createExecuteXEffect(Player viewer, Location victimLocation) {
        // Dark red dust particles
        DustOptions darkRed = new DustOptions(Color.fromRGB(139, 0, 0), 1.0f);
        
        // Get victim's eye level for better visibility
        Location center = victimLocation.clone().add(0, 1.5, 0);
        
        // Calculate direction from victim to viewer to orient the X
        Vector toViewer = viewer.getLocation().toVector().subtract(center.toVector()).normalize();
        
        // Create perpendicular vectors for the X orientation
        Vector right = toViewer.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        Vector up = new Vector(0, 1, 0);
        
        // Create X pattern - two diagonal lines (longer and facing the viewer)
        double size = 2.0; // Increased size of the X
        int particlesPerLine = 12; // More particles for longer lines
        
        // First diagonal (top-left to bottom-right relative to viewer)
        for (int i = 0; i < particlesPerLine; i++) {
            double progress = (double) i / (particlesPerLine - 1); // 0.0 to 1.0
            double rightOffset = (progress - 0.5) * size; // -1.0 to +1.0
            double upOffset = (0.5 - progress) * size; // +1.0 to -1.0
            
            Vector offset = right.clone().multiply(rightOffset).add(up.clone().multiply(upOffset));
            Location particleLoc = center.clone().add(offset);
            viewer.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, darkRed);
        }
        
        // Second diagonal (top-right to bottom-left relative to viewer)
        for (int i = 0; i < particlesPerLine; i++) {
            double progress = (double) i / (particlesPerLine - 1); // 0.0 to 1.0
            double rightOffset = (0.5 - progress) * size; // +1.0 to -1.0
            double upOffset = (0.5 - progress) * size; // +1.0 to -1.0
            
            Vector offset = right.clone().multiply(rightOffset).add(up.clone().multiply(upOffset));
            Location particleLoc = center.clone().add(offset);
            viewer.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, darkRed);
        }
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "butchersknife";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "MS ", "PI ", " I ", r.eCFromBeangame(Key.bg("melonaxe")), r.eCFromBeangame(Key.bg("stopsign")), r.eCFromBeangame(Key.bg("plaxe")), r.mCFromMaterial(Material.IRON_SWORD));
        return null;
    }

    @Override
    public String getName() {
        return "§4Butcher's Knife";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cInstantly executes enemies at or below",
            "§c25% health, dealing their maximum",
            "§chealth as damage. Creates a dark red",
            "§cX particle effect on execution.",
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
        return Material.WOODEN_SWORD;
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

