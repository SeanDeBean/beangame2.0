package com.beangamecore.items.material;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.recipes.RecipeAPI;
import com.beangamecore.util.Key;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.BeangameSoftItem;
import com.beangamecore.util.GlobalCooldowns;


public class Stopwatch extends BeangameItem implements BGRClickableI, BeangameSoftItem {
    
    @Override
    public long getBaseCooldown() {
        return 90000L;
    }

    @Override
    public void setCooldown(UUID player, long millis){
        GlobalCooldowns.setCooldown("stopwatch", millis);
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        RecipeAPI r = Main.recipeAPI;
        return r.bgShapedRecipe(this, " B ", " C ", " G ", r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.CLOCK), r.mCFromMaterial(Material.GOLD_BLOCK));
    }

    @Override
    public void resetCooldown(UUID player){
        GlobalCooldowns.setCooldown("stopwatch", -1);
    }

    @Override
    public String getId() {
        return "stopwatch";
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        return false;
    }

    @Override
    public boolean isInItemRotation() {
        return false;
    }

    // private static final int SPEED = 7; // ticks per second
    // private static final int DEFAULT = 20; // default ticks per second
    // private static final int DURATION = 7 * 6; // seconds active

    // @Override
    // public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
    //     event.setCancelled(true);
    //     // cooldown system
    //     Player player = event.getPlayer();
    //     if (GlobalCooldowns.onCooldown("stopwatch")){
    //         GlobalCooldowns.sendCooldownMessage("stopwatch", player);
    //         return false;
    //     }
    //     GlobalCooldowns.setCooldown("stopwatch", getBaseCooldown());
    //     Bukkit.getServerTickManager().setTickRate(SPEED);
    //     new BukkitRunnable() {
    //         @Override
    //         public void run() {
    //             Bukkit.getServerTickManager().setTickRate(DEFAULT);
    //         }
    //     }.runTaskLater(Main.getPlugin(), DURATION);
    //     for(Player p : Bukkit.getOnlinePlayers()){  
    //         p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§3Time has been slowed!"));
    //         if(p.getGameMode().equals(GameMode.SURVIVAL)){
    //             p.getWorld().playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_7, 0.7F, 1F);
    //         }
    //     }
    //     return true;
    // }

    @Override
    public String getName() {
        return "§3Stopwatch";
    }

    @Override
    public List<String> getLore() {
        return List.of("§bCustom crafting ingredient", "§9§obeangame");
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.CHAIN;
    }

    @Override
    public int getCustomModelData() {
        return 103;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor(){
        return 0;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}

