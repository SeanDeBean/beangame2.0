package com.beangamecore.util;

import com.beangamecore.entities.renderers.BlockDisplayRenderer;
import com.beangamecore.items.*;
import org.bukkit.NamespacedKey;

import com.beangamecore.registry.BeangameItemRegistry;

public class ResetItems {
    public void resetAllItems(){
        // resets longs
        Longs.register("assassinshitlist_hits");
        Longs.register("spearofares_hits");
        Longs.register("trapperscapital_stacks");

        // resets BlockDisplayRenderer
        BlockDisplayRenderer.clearAll();

        // resets cycling tasks
        BeangameItemRegistry.get(Key.bg("spearofares"), SpearOfAres.class).ifPresent(SpearOfAres::disableStateCycleTask);

        // resets item display based elements
        BeangameItemRegistry.get(Key.bg("fireflower"), FireFlower.class).ifPresent(FireFlower::removeAllFireballs);
        BeangameItemRegistry.get(Key.bg("baleout"), Baleout.class).ifPresent(Baleout::removeAllBales);
        BeangameItemRegistry.get(Key.bg("tennisball"), TennisBallItem.class).ifPresent(TennisBallItem::removeAllTennisBalls);

        // resets specific item trackers
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:bordermanipulator"), BorderManipulator.class).setActive(true);
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:carrioncall"), CarrionCall.class).removeAllBirds();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:catkeyboard"), CatKeyboard.class).clearNotes();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:cookieclicker"), CookieClicker.class).reset();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:disoriedge"), Disoriedge.class).cleanupOldEntries();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:duelistsdance"), DuelistsDance.class).resetVitals();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:equilibriumamulet"), EquilibriumAmulet.class).equalibriumamuletReset();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:portalmaker"), PortalMaker.class).portalmakerResetAll();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:recall"), Recall.class).recallReset();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:runningshoes"), RunningShoes.class).resetAllPlayers();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:tomatior"), Tomatior.class).resetTomatior();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:ultimategamble"), UltimateGamble.class).setActive(false);;
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:vampirefang"), VampireFang.class).reset();
        BeangameItemRegistry.getRaw(NamespacedKey.fromString("beangame:whisperfang"), Whisperfang.class).clearAll();

    }
}

