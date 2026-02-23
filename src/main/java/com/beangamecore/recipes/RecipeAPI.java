package com.beangamecore.recipes;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;


public class RecipeAPI {

    final Plugin p;

    public RecipeAPI(Plugin plugin){
        p = plugin;
    }

    public ShapelessRecipe shapelessRecipe(String recipeName, Material result, RecipeChoice... items){
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        for(RecipeChoice choice : items){
            shapelessRecipe.addIngredient(choice);
        }
        return shapelessRecipe;
    }

    public ShapelessRecipe shapelessRecipe(String recipeName, ItemStack result, RecipeChoice... items){
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        for(RecipeChoice choice : items){
            shapelessRecipe.addIngredient(choice);
        }
        return shapelessRecipe;
    }

    public ShapedRecipe shapedRecipe(String recipeName, Material result, String row1, String row2, String row3, RecipeChoice.MaterialChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.MaterialChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);
        return shapedRecipe;
    }

    public ShapedRecipe shapedRecipe(String recipeName, Material result, String row1, String row2, String row3, RecipeChoice.ExactChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.ExactChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);
        return shapedRecipe;
    }

    public ShapedRecipe shapedRecipe(String recipeName, ItemStack result, String row1, String row2, String row3, RecipeChoice.MaterialChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.MaterialChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);
        return shapedRecipe;
    }

    public RecipeChoice.MaterialChoice mCFromMaterial(Material material){
        return new RecipeChoice.MaterialChoice(material);
    }

    public RecipeChoice.ExactChoice eCFromBeangame(NamespacedKey key) {
        BeangameItem item = BeangameItemRegistry.getRaw(key);
        ItemStack stack = item.asItem();
        
        // Ensure the item has proper metadata for comparison
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            // Set a display name if not already set
            if (!meta.hasDisplayName()) {
                meta.setDisplayName(item.getName());
            }
            stack.setItemMeta(meta);
        }
        
        return new RecipeChoice.ExactChoice(stack);
    }

    public ShapedRecipe bgShapedRecipe(BeangameItem item, String row1, String row2, String row3, Object... items) {
        String recipeName = item.getNamespace() + "_" + item.getId();
        ItemStack result = item.asItem();
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), result);
        
        String combined = row1 + row2 + row3;
        HashMap<Character, RecipeChoice> charChoices = new HashMap<>();
        
        int itemIndex = 0;
        for (char c : combined.toCharArray()) {
            if (c != ' ' && !charChoices.containsKey(c)) {
                if (itemIndex < items.length) {
                    Object ingredient = items[itemIndex];
                    if (ingredient instanceof RecipeChoice) {
                        charChoices.put(c, (RecipeChoice) ingredient);
                        itemIndex++;
                    } else {
                        throw new IllegalArgumentException("Ingredient must be RecipeChoice");
                    }
                }
            }
        }
        
        shapedRecipe.shape(row1, row2, row3);
        charChoices.forEach(shapedRecipe::setIngredient);
        return shapedRecipe;
    }

    public ShapedRecipe shapedRecipe(String recipeName, ItemStack result, String row1, String row2, String row3, RecipeChoice.ExactChoice... items){
        ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(p, recipeName), new ItemStack(result));
        String combined = row1+row2+row3;
        HashMap<Character, RecipeChoice.ExactChoice> charMats = new HashMap<>();
        int i = 0;
        for(char c : combined.toCharArray()){
            if(!charMats.containsKey(c) && c != ' '){
                charMats.put(c, items[i]);
                i++;
            }
        }
        shapedRecipe.shape(row1, row2, row3);
        charMats.forEach(shapedRecipe::setIngredient);
        return shapedRecipe;
    }
    
}

