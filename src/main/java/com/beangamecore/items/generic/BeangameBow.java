package com.beangamecore.items.generic;

import com.beangamecore.items.type.BGProjectileI;
import org.bukkit.entity.Projectile;

import java.util.ArrayList;
import java.util.Collection;

public abstract class BeangameBow extends BeangameItem implements BGProjectileI {

    private Collection<Projectile> arrows = new ArrayList<>();

    public Collection<Projectile> getArrows(){
        return arrows;
    }

    public void addArrow(Projectile arrow){
        arrows.add(arrow);
    }

    public void removeArrow(Projectile arrow){
        arrows.remove(arrow);
    }
    
}

