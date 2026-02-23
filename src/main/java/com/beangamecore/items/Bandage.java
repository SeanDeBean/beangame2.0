package com.beangamecore.items;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;

public class Bandage extends BeangameItem implements BGRClickableI, BeangameSoftItem {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (onCooldown(player.getUniqueId())){
            sendCooldownMessage(player);
            return false;
        }
        boolean used = consumeBandage((LivingEntity) player, stack);
        if(used){
            applyCooldown(player.getUniqueId());
        }
        return true;
    }

    public boolean consumeBandage(LivingEntity entity, ItemStack stack){
        boolean used = heal(entity);
        if(stack == null){
            return false;
        }
        if(used){
            stack.setAmount(stack.getAmount() - 1);
        }
        return used;
    }

    private boolean heal(LivingEntity entity){
        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        double health = attribute.getValue();
        if(entity.getHealth() == health){
            return false;
        }
        if(entity.getHealth() + 4 >= health){
            entity.setHealth(health);
        } else {
            entity.setHealth(entity.getHealth() + 4);
        }
        entity.getWorld().spawnParticle(Particle.GUST, entity.getLocation(), 3);
        entity.getWorld().playSound(entity, Sound.BLOCK_NOTE_BLOCK_FLUTE, 1, 1);
        return true;
    }
    
    @Override
    public long getBaseCooldown() {
        return 6000L;
    }

    @Override
    public String getId() {
        return "bandage";
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    @Override
    public int getCraftingAmount(){
        return 12;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Bandage";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to instantly heal 2 hearts",
            "§awhen not at full health.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.PAPER;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
    }

    @Override
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 12;
    }

}

