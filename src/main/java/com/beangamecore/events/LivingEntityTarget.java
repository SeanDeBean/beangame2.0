package com.beangamecore.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.type.target.BGTargetArmor;
import com.beangamecore.items.type.target.BGTargetTalisman;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;

public class LivingEntityTarget implements Listener{
    @EventHandler
    private void onLivingEntityTarget(org.bukkit.event.entity.EntityTargetLivingEntityEvent event){
        LivingEntity target = event.getTarget();
        if(target instanceof Player ptarget){

            if (Cooldowns.onCooldown("untargetable", ptarget.getUniqueId())){
                event.setCancelled(true);
                return;
            }

            for(ItemStack armor : ptarget.getEquipment().getArmorContents()){
                BeangameItemRegistry.getFromItemStack(armor).ifPresent(i -> i.doIf(BGTargetArmor.class, s -> s.onTargetArmor(event, armor)));
            }
            for(ItemStack item : ptarget.getInventory().getContents()){
                if(item != null){
                    BeangameItemRegistry.getFromItemStack(item).ifPresent(i -> i.doIf(BGTargetTalisman.class, s -> s.onTargetTalisman(event, item)));
                }
            }
        }
    }
}

