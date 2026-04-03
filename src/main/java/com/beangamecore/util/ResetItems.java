package com.beangamecore.util;

import com.beangamecore.entities.renderers.BlockDisplayRenderer;
import com.beangamecore.items.*;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.general.BGResetableI;

import org.bukkit.NamespacedKey;

import com.beangamecore.registry.BeangameItemRegistry;

public class ResetItems {
    public void resetAllItems(){
        for(BeangameItem i : BeangameItemRegistry.collection()){
            i.doIf(BGResetableI.class, BGResetableI::resetItem);
        }

        // resets BlockDisplayRenderer
        BlockDisplayRenderer.clearAll();

        // resets specific item trackers
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:bordermanipulator"), BorderManipulator.class).setActive(true);
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:ultimategamble"), UltimateGamble.class).setActive(false);;

    }
}

