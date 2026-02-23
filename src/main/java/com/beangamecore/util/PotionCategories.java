package com.beangamecore.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.potion.PotionEffectType;

public class PotionCategories {

    public static Set<PotionEffectType> harmfulPotions = new HashSet<>(Arrays.asList(
        PotionEffectType.SLOWNESS, PotionEffectType.MINING_FATIGUE, 
        PotionEffectType.NAUSEA, PotionEffectType.BLINDNESS, PotionEffectType.HUNGER, PotionEffectType.WEAKNESS, PotionEffectType.POISON, 
        PotionEffectType.WITHER, PotionEffectType.BAD_OMEN, PotionEffectType.GLOWING, PotionEffectType.DARKNESS));
        
}

