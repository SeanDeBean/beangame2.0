package com.beangamecore.items.type;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public interface BGProjectileI {
    // Beangame Projectile Item
    void onProjHit(ProjectileHitEvent event);
    default <T extends Projectile> T launchProjectile(BeangameItem item, Player player, Class<T> clazz){
        T projectile = player.launchProjectile(clazz, player.getLocation().getDirection());
        projectile.setMetadata("beangame", new FixedMetadataValue(Main.getPlugin(), item.getKey().toString()));
        projectile.setShooter(player);
        return projectile;
    }
}

