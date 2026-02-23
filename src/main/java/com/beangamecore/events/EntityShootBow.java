package com.beangamecore.events;

import com.beangamecore.items.generic.BeangameBow;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGAmmoI;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.beangamecore.Main;

public class EntityShootBow implements Listener{
    @EventHandler
    private void onShoot(EntityShootBowEvent event){
        ItemStack bow = event.getBow();
        BeangameItemRegistry.getFromItemStack(bow).ifPresent(item -> {
            Projectile arrow = (Projectile) event.getProjectile();
            arrow.setMetadata("beangame", new FixedMetadataValue(Main.getPlugin(), item.getKey()));
            item.doIf(BGAmmoI.class, a -> a.onShootBow(event, bow));
            item.doIf(BeangameBow.class, bgbow -> bgbow.addArrow(arrow));
        });
    }
}

