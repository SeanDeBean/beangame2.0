package com.beangamecore.items.generic;

import org.bukkit.Color;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public abstract class BeangameMaterial extends BeangameItem {
    
    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public boolean isInItemRotation() {
        return false;
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
    
}

