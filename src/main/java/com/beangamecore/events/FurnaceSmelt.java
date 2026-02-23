package com.beangamecore.events;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGSmeltableI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.util.ItemNBT;

public class FurnaceSmelt implements Listener{
    @EventHandler
    private void onSmelt(org.bukkit.event.inventory.FurnaceSmeltEvent event){
        ItemStack source = event.getSource();
        if(ItemNBT.hasBeanGameTag(source)){
            BeangameItem item = BeangameItemRegistry.getFromItemStackRaw(source);
            if(item instanceof BGSmeltableI s) s.onSmelt(event);
            else event.getBlock().breakNaturally();
        }
    }
}

