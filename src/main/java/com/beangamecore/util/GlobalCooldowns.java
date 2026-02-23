package com.beangamecore.util;

import com.beangamecore.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GlobalCooldowns {

    private static final Map<String, Long> cooldowns = new HashMap<>();

    public static long get(String name){
        if(cooldowns.get(name) == null) Main.logger().warning("Tried to access unregistered global cooldown: "+name+"!");
        return cooldowns.get(name);
    }

    public static boolean onCooldown(String id){
        long cd = get(id);
        return cd > System.currentTimeMillis();
    }

    public static void setCooldown(String id, long millis){
        cooldowns.put(id, System.currentTimeMillis() + millis);
    }

    public static long getRemainingCooldown(String id){
        return get(id) - System.currentTimeMillis();
    }

    public static void register(String name){
        Main.logger().info("Registered Global Cooldown: ["+name+"]");
        cooldowns.put(name, System.currentTimeMillis());
    }

    public static void sendCooldownMessage(String id, Player player){
        long cd = getRemainingCooldown(id) / 1000L;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§dWait " + cd + " second(s) before using again!"));
    }
    
}

