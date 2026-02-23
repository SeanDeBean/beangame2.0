package com.beangamecore.commands;

import com.beangamecore.registry.BeangameItemRegistry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class GiveTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            List<String> keys = BeangameItemRegistry.collection().stream()
                .map(item -> item.getKey().toString())
                .toList();

            List<String> returnValue = new ArrayList<>();
            StringUtil.copyPartialMatches(strings[0], keys, returnValue);

            // Retry with "beangame:" prefix if no match was found
            if (returnValue.isEmpty()) {
                StringUtil.copyPartialMatches("beangame:" + strings[0], keys, returnValue);
            }
            return returnValue;
        } else if (strings.length == 2) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();

            List<String> returnValue = new ArrayList<>();
            StringUtil.copyPartialMatches(strings[1], List.of("@a", "all"), returnValue);
            StringUtil.copyPartialMatches(strings[1], playerNames, returnValue);
            return returnValue;
        }
        return List.of();
    }
}

