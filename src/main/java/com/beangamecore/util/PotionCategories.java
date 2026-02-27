package com.beangamecore.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.potion.PotionEffectType;

public class PotionCategories {

    public static Set<PotionEffectType> getHarmfulPotions(){
        return harmfulPotions;
    }

    private static Set<PotionEffectType> harmfulPotions = new HashSet<>(Arrays.asList(
        PotionEffectType.SLOWNESS, PotionEffectType.MINING_FATIGUE, PotionEffectType.LEVITATION,
        PotionEffectType.NAUSEA, PotionEffectType.BLINDNESS, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, PotionEffectType.POISON, 
        PotionEffectType.WITHER, PotionEffectType.BAD_OMEN, PotionEffectType.GLOWING, PotionEffectType.DARKNESS
    ));

    public static Set<String> getHarmfulCustomPotions(){
        return harmfulCustomPotions;
    }

    private static Set<String> harmfulCustomPotions = new HashSet<>(Arrays.asList(
        "silenced", "schizophrenic", "use_item", "slot_enforced", "immobilized", "jumbling", "redacted"
    ));
        
}

