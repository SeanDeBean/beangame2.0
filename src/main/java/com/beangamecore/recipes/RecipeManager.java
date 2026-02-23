package com.beangamecore.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.beangamecore.Main;

public class RecipeManager {
    public void init(Main plugin){
        
        NamespacedKey slimebeangame = new NamespacedKey(plugin, "slimeball_beangame");
        ItemStack slimeball = new ItemStack(Material.SLIME_BALL, 8);
        ShapelessRecipe slimeballRecipe = new ShapelessRecipe(slimebeangame, slimeball);
        slimeballRecipe.addIngredient(1, Material.WATER_BUCKET);
        slimeballRecipe.addIngredient(8, Material.DIRT);
        Bukkit.addRecipe(slimeballRecipe);

        // arrow recepie
        NamespacedKey arrowbeangame = new NamespacedKey(plugin, "arrow_beangame");
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        ShapelessRecipe arrowRecipe = new ShapelessRecipe(arrowbeangame, arrow);
        arrowRecipe.addIngredient(1, Material.STICK);
        arrowRecipe.addIngredient(1, Material.FLINT);
        Bukkit.addRecipe(arrowRecipe);

        // copper ingot recepie
        NamespacedKey copperbeangame = new NamespacedKey(plugin, "copper_beangame");
        ItemStack copper6 = new ItemStack(Material.COPPER_INGOT, 6);
        ShapelessRecipe copper6Recipe = new ShapelessRecipe(copperbeangame, copper6);
        copper6Recipe.addIngredient(1, Material.COAL);
        copper6Recipe.addIngredient(8, Material.RAW_COPPER);
        Bukkit.addRecipe(copper6Recipe);

        // flint recepie
        NamespacedKey flintbeangame = new NamespacedKey(plugin, "flint_beangame");
        ItemStack flint = new ItemStack(Material.FLINT, 1);
        ShapelessRecipe flintRecipe = new ShapelessRecipe(flintbeangame, flint);
        flintRecipe.addIngredient(1, Material.GRAVEL);
        Bukkit.addRecipe(flintRecipe);

        // gold ingot recepie
        NamespacedKey goldbeangame = new NamespacedKey(plugin, "gold_beangame");
        ItemStack gold6 = new ItemStack(Material.GOLD_INGOT, 6);
        ShapelessRecipe gold6Recipe = new ShapelessRecipe(goldbeangame, gold6);
        gold6Recipe.addIngredient(1, Material.COAL);
        gold6Recipe.addIngredient(8, Material.RAW_GOLD);
        Bukkit.addRecipe(gold6Recipe);

        // iron ingot recepie
        NamespacedKey ironbeangame = new NamespacedKey(plugin, "iron_beangame");
        ItemStack iron6 = new ItemStack(Material.IRON_INGOT, 6);
        ShapelessRecipe iron6Recipe = new ShapelessRecipe(ironbeangame, iron6);
        iron6Recipe.addIngredient(1, Material.COAL);
        iron6Recipe.addIngredient(8, Material.RAW_IRON);
        Bukkit.addRecipe(iron6Recipe);

        // gilded blackstone recepie
        NamespacedKey gildedblackstone = new NamespacedKey(plugin, "gildedblackstone_beangame");
        ShapedRecipe gildedblackstoneRecipe =  new ShapedRecipe(gildedblackstone, new ItemStack(Material.GILDED_BLACKSTONE, 1));
        gildedblackstoneRecipe.shape("BBB",
                                              "BGB",
                                              "BBB"); 
        gildedblackstoneRecipe.setIngredient('B', Material.BLACKSTONE);
        gildedblackstoneRecipe.setIngredient('G', Material.RAW_GOLD_BLOCK);
        Bukkit.addRecipe(gildedblackstoneRecipe);

        NamespacedKey woolstring = new NamespacedKey(plugin, "woolstring_beangame");
        ShapelessRecipe woolstringRecipe =  new ShapelessRecipe(woolstring, new ItemStack(Material.STRING, 4));
        RecipeChoice.MaterialChoice woolChoice = new RecipeChoice.MaterialChoice(
            Material.WHITE_WOOL,
            Material.ORANGE_WOOL,
            Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.PINK_WOOL,
            Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL,
            Material.PURPLE_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.GREEN_WOOL,
            Material.RED_WOOL,
            Material.BLACK_WOOL
        );
        woolstringRecipe.addIngredient(woolChoice);
        Bukkit.addRecipe(woolstringRecipe);

    }
}


