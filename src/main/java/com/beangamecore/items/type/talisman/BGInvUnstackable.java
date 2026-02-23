package com.beangamecore.items.type.talisman;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.util.Booleans;

import java.util.UUID;

public interface BGInvUnstackable {
    // Beangame Inventory Unstackable
    default boolean alreadyActivated(UUID player, BeangameItem item){
        return Booleans.getBoolean(item.getKey()+"_invstackcheck", player);
    }
    default void activate(UUID player, BeangameItem item){
        Booleans.get(item.getKey()+"_invstackcheck").put(player, true);
    }
    default void reset(UUID player, BeangameItem item){
        Booleans.get(item.getKey()+"_invstackcheck").put(player, false);
    }
}

