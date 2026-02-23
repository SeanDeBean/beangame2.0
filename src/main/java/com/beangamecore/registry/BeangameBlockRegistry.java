package com.beangamecore.registry;

import com.beangamecore.Main;
import com.beangamecore.blocks.generic.BeangameBlock;
import org.bukkit.NamespacedKey;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class BeangameBlockRegistry {

    public static final Map<String, BeangameBlock> registry = new TreeMap<>();

    public static void register(NamespacedKey id, BeangameBlock block){
        registry.put(id.toString(), block);
        Main.logger().info("Registered Beangame Block: ["+id+"]");
    }

    public static Optional<BeangameBlock> get(String id){
        return Optional.ofNullable(getRaw(id));
    }

    public static Optional<BeangameBlock> get(NamespacedKey id){
        return Optional.ofNullable(registry.get(id.toString()));
    }

    public static BeangameBlock getRaw(String id){
        return registry.get("beangame:"+id);
    }

    public static BeangameBlock getRaw(NamespacedKey key){
        return registry.get(key.toString());
    }

}

