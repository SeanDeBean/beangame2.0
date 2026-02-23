package com.beangamecore.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBT;

public class ItemNBT {

    // checks if item has a beangame.itemkey NBT datatag at all
    public static boolean hasBeanGameTag(ItemStack item){
        Boolean bool = false;
        if(item != null && !item.getType().equals(Material.AIR)){
            bool = NBT.get(item, (nbt) -> {
                return nbt.hasTag("beangame.itemkey");
            });
        } 
        return bool;
    }

    // checks the beangame.itemkey NBT and compares value
    public static boolean isBeanGame(ItemStack item, NamespacedKey s) {
        String key = NBT.get(item, (nbt) -> {
            return nbt.getString("beangame.itemkey");
        });
        return key.equals(s.toString());
    }

    public static NamespacedKey getBeanGame(ItemStack item){
        if(item == null) return null;
        if(item.getType() == Material.AIR) return null;
        String s = NBT.get(item, (nbt) -> {
            return nbt.getString("beangame.itemkey");
        });
        if(s == null) return null;
        else if(!s.contains(":")) return null;
        else return NamespacedKey.fromString(s);
    }
    
}

