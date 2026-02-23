package com.beangamecore.util;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BGUtil {

    public static boolean hasBeangameArmor(Player player, BeangameItem item){
        for(ItemStack i : player.getInventory().getArmorContents()){
            if(i == null) continue;
            if(i.getType() != Material.AIR){
                if(ItemNBT.isBeanGame(i, item.getKey())) return true;
            }
        }
        return false;
    }

    public static boolean hasBeangameInterface(Player player, Class<?> clazz){
        for(ItemStack i : player.getInventory().getArmorContents()){
            if(i == null) continue;
            if(i.getType() != Material.AIR){
                BeangameItem item = BeangameItemRegistry.getFromItemStackRaw(i);
                if(clazz.isInstance(item)) return true;
            }
        }
        return false;
    }
    
}

