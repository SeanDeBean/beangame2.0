package com.beangamecore.util;

import com.beangamecore.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Booleans {

    private static final Map<String, Map<UUID, Boolean>> registry = new HashMap<>();

    public static Map<UUID, Boolean> get(String name){
        if(registry.get(name) == null) Main.logger().warning("Tried to access unregistered boolean: "+name+"!");
        return registry.get(name);
    }

    public static boolean getBoolean(String id, UUID player){
        Map<UUID, Boolean> cd = get(id);
        if(!cd.containsKey(player)) return false;
        return cd.get(player);
    }

    public static void setBoolean(String id, UUID player, boolean bool){
        get(id).put(player, bool);
    }

    public static void register(String name){
        registry.put(name, new HashMap<>());
    }

}

