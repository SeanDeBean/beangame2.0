package com.beangamecore.util;

import org.bukkit.NamespacedKey;

public class Key {
    
    public static NamespacedKey BLOCKS;
    public static NamespacedKey POSITION;
    public static NamespacedKey ID;
    public static NamespacedKey MATERIAL;
    public static NamespacedKey bg(String name){
        return NamespacedKey.fromString("beangame:" + name);
    }

}

