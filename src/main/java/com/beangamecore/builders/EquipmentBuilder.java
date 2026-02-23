package com.beangamecore.builders;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;

public class EquipmentBuilder {
    
    final EquippableComponent component;

    public EquipmentBuilder(ItemMeta item) {
        component = item.getEquippable();
    }

    public static EquipmentBuilder create(){
        return new EquipmentBuilder(new ItemStack(Material.GRASS_BLOCK).getItemMeta());
    }

    public EquipmentBuilder withSlot(EquipmentSlot slot){
        component.setSlot(slot);
        return this;
    }

    public EquipmentBuilder withEquipSound(Sound sound){
        component.setEquipSound(sound);
        return this;
    }

    public EquipmentBuilder withModel(NamespacedKey model){
        component.setModel(model);
        return this;
    }

    public EquipmentBuilder withDamageOnHurt(boolean hurt){
        component.setDamageOnHurt(hurt);
        return this;
    }

    public EquipmentBuilder withCameraOverlay(NamespacedKey overlay){
        component.setCameraOverlay(overlay);
        return this;
    }

    public EquippableComponent build(){
        return component;
    }

    public EquipmentBuilder withSwappable(boolean swappable) {
        component.setSwappable(swappable);
        return this;
    }
}

