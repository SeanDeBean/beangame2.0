package com.beangamecore.events;

import java.util.List;

import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGProjectileI;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;

import com.beangamecore.items.OozingAegis;

public class ProjectileHandler implements Listener {
    @EventHandler
    public void onProjectileLand(ProjectileHitEvent event) {
        Entity projectile = event.getEntity();

        if (event.getHitEntity() != null) {
            if (BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:oozingaegis"), OozingAegis.class).handleSlimeHit(event)) {
                return;
            }
        }

        // Check if projectile has beangame data attached
        List<MetadataValue> values = projectile.getMetadata("beangame");
        if (values.isEmpty()) {
            return;
        }
        BeangameItemRegistry.get(NamespacedKey.fromString(values.get(0).asString())).ifPresent(item -> item.doIf(BGProjectileI.class, p -> p.onProjHit(event)));
    }
}

