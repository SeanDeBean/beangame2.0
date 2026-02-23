package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class CreepingDownfall extends BeangameItem implements BGRClickableI {

    private static int number = 9;

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        //cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);
        for(int i = 0; i < number; i++){
            spawnCreeperAround(loc, player);
        }
        return true;
    }

    public void spawnCreeperAround(Location center, Player player) {
        Random random = new Random();
        double x = center.getX() + (random.nextDouble() * 20 - 10);
        double y = center.getY() + 20 + (random.nextDouble() * 4 - 2);
        double z = center.getZ() + (random.nextDouble() * 20 - 10);
        Location spawnLocation = new Location(center.getWorld(), x, y, z);
        Creeper creeper = (Creeper) center.getWorld().spawnEntity(spawnLocation, EntityType.CREEPER);
        creeper.setCustomName(player.getName() + "'s creeper");
        creeper.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 600, 1, false, true));
        creeper.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 600, 0, false, true));
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if(creeper.isValid()){
                creeper.ignite();
            }
        }, 320L);
    }

    @Override
    public long getBaseCooldown() {
        return 36000L;
    }

    @Override
    public String getId() {
        return "creepingdownfall";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "GGG", "GTG", " S ", r.mCFromMaterial(Material.GUNPOWDER), r.eCFromBeangame(Key.bg("tntimer")), r.eCFromBeangame(Key.bg("spawncore")));
        return null;
    }

    @Override
    public String getName() {
        return "§2Creeping Downfall";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon 9 creepers that",
            "§9rain down around you with slow falling.",
            "§9Creepers ignite after 16 seconds and",
            "§9can damage both enemies and the user.",
            "",
            "§9Summon",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.CREEPER_SPAWN_EGG;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of();
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

