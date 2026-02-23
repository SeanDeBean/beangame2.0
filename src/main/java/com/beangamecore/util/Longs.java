package com.beangamecore.util;

import com.beangamecore.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Longs {

    private static final Map<String, Map<UUID, Long>> registry = new HashMap<>();

    public static Map<UUID, Long> get(String name){
        if(registry.get(name) == null) Main.logger().warning("Tried to access unregistered long: "+name+"!");
        return registry.get(name);
    }

    public static long getLong(String id, UUID player){
        Map<UUID, Long> cd = get(id);
        if(!cd.containsKey(player)) return 0;
        return cd.get(player);
    }

    public static void setLong(String id, UUID player, long value){
        get(id).put(player, value);
    }

    public static void register(String name){
        if(registry.containsKey(name)){
            registry.remove(name);
        }
        registry.put(name, new HashMap<>());
        Main.logger().info("Registered Long: ["+name+"]");
    }

    public static void resetLong(String id, UUID player){
        get(id).remove(player);
    }
    
}

