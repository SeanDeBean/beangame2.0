package com.beangamecore.blocks.generic;

import com.beangamecore.items.generic.BeangameItem;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;

public interface BeangameBlock extends Keyed {
    // Don't use this interface directly unless your extending it for more functionality

    default NamespacedKey getKey(){
        if(this instanceof BeangameItem i){
            return i.getKey();
        } else throw new UnsupportedOperationException();
    }

    Material getMaterial();
    void onDestroy(Block block);

    default void destroy(Block block){
        if(shouldDropOnDestroy(block) && this instanceof BeangameItem i) block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), i.asItem());
    }
    
    boolean shouldDropOnDestroy(Block block);
    default Material getBlockType(){
        return getMaterial();
    }
}

