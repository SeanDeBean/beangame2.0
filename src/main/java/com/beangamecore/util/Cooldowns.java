package com.beangamecore.util;

import com.beangamecore.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cooldowns {

    private static final Map<String, Map<UUID, Long>> registry = new HashMap<>();

    public static Map<UUID, Long> get(String name){
        if(registry.get(name) == null) Main.logger().warning("Tried to access unregistered cooldown: "+name+"!");
        return registry.get(name);
    }

    public static boolean onCooldown(String id, UUID player){
        Map<UUID, Long> cd = get(id);
        if(!cd.containsKey(player)) return false;
        return cd.get(player) > System.currentTimeMillis();
    }

    public static void setCooldown(String id, UUID player, long millis){
        get(id).put(player, System.currentTimeMillis() + millis);
    }

    public static long getRemainingCooldown(String id, UUID player) {
        Map<UUID, Long> map = get(id);
        if (map == null) return 0;

        Long expireTime = map.get(player);
        if (expireTime == null) return 0;

        long remaining = expireTime - System.currentTimeMillis();
        return Math.max(remaining, 0); // Never return negative
    }

    public static void register(String name){
        Main.logger().info("Registered Cooldown: ["+name+"]");
        registry.put(name, new HashMap<>());
    }

    public static void sendPVPCooldownMessage(String id, Player player){
        
        UUID uuid = player.getUniqueId();
        if (Cooldowns.onCooldown("redacted", uuid)){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§8Cooldown redacted!"));
            return;
        }

        long attackcooldown = getRemainingCooldown(id, player.getUniqueId()) / 1000L;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cPvP logged for the next " + attackcooldown + " second(s)!"));
    }
    
}

