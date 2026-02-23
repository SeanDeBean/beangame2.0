package com.beangamecore.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGArmorI;
import com.beangamecore.items.type.target.BGTargetArmor;
import com.beangamecore.util.ItemNBT;

public class Drownmet extends BeangameItem implements BGArmorI, BGTargetArmor {
    
    static HashMap<UUID, Drowned> drownedList = new HashMap<>();
    static List<UUID> noList = new ArrayList<>();

    public static void onDrownedDeath(EntityDeathEvent event, Drowned drowned){
        for(UUID uuid : drownedList.keySet()){
            if(drownedList.get(uuid).equals(drowned)){
                noList.add(uuid);
                drownedList.remove(uuid);
                // Fixed: Converted to lambda
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    noList.remove(uuid);
                }, 600L);
                event.getDrops().clear();
                return;
            }
        }
    }

    @Override
    public void onTargetArmor(EntityTargetLivingEntityEvent event, ItemStack armor){
        UUID uuid = event.getTarget().getUniqueId();
        if(drownedList.containsKey(uuid)){
            if(event.getEntity() instanceof Drowned && drownedList.get(uuid).equals((Drowned) event.getEntity())){
                event.setCancelled(true);
            }
        }
    }

    @Override // this activates every 3 seconds when a player is wearing the helmet
    public void applyArmorEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();

        // removes drowns on log off and on helemet take off
        // Fixed: Converted to lambda
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (shouldSkipRemoveDrowned(uuid)) {
                return;
            }
            removeDrowned(player);
        }, 59L);

        if (noList.contains(uuid)) {
            removeDrowned(player);
            return;
        }

        // spawning mechanics and teleporting when out of range
        if (drownedList.containsKey(uuid)) {
            if (drownedList.get(uuid).isDead()) {
                spawnDrowned(player);
                return;
            }
            Drowned drowned = drownedList.get(uuid);
            if (drowned.getLocation().distance(player.getLocation()) > 20) {
                removeDrowned(player);
                spawnDrowned(player);
            }
        } else {
            spawnDrowned(player);
        }

        if (Math.random() > 0.74) {
            retarget(player, drownedList.get(uuid));
        }
    }

    private boolean shouldSkipRemoveDrowned(UUID uuid) {
        Player bukkitPlayer = Bukkit.getPlayer(uuid);
        return bukkitPlayer.isOnline() &&
                !bukkitPlayer.getGameMode().equals(GameMode.SPECTATOR) &&
                ItemNBT.hasBeanGameTag(bukkitPlayer.getEquipment().getHelmet()) &&
                ItemNBT.isBeanGame(bukkitPlayer.getEquipment().getHelmet(), getKey());
    }


    // gets a location nearby the player
    private Location getRandomLocation(Location location, int radius){
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double xOffset = radius * Math.cos(angle);
        double zOffset = radius * Math.sin(angle);
        Location loc = location.clone().add(xOffset, 0, zOffset);
        loc.setY(loc.getY() + 1);
        return loc;
    }

    private void retarget(Player owner, Drowned drowned) {
        Double close = 100D;
        Player target = null;
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (isSuitableTarget(owner, drowned, players) && players.getLocation().distance(drowned.getLocation()) < close) {
                close = players.getLocation().distance(drowned.getLocation());
                target = players;
            }
        }
        if(close < 100){
            drowned.setTarget(target);
            return;
        }
        drowned.setTarget(null);
    }

    private boolean isSuitableTarget(Player owner, Drowned drowned, Player players) {
        return players.getGameMode().equals(GameMode.SURVIVAL) && !players.equals(owner)
                && players.getLocation().distance(drowned.getLocation()) < 100 && !players.isInvisible();
    }

    // spawn structure
    private void spawnDrowned(Player owner){
        Drowned drowned = (Drowned) owner.getWorld().spawnEntity(getRandomLocation(owner.getLocation(), 6), EntityType.DROWNED);
        drownedList.put(owner.getUniqueId(), drowned);
        drowned.setAdult();
        drowned.setCustomName(owner.getName());
        drowned.setCustomNameVisible(false);
        drowned.getEquipment().setHelmet(new ItemStack(Material.TURTLE_HELMET));
        drowned.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT));
        retarget(owner, drowned);
    }

    // removes the drown from the world and the player from the list
    private static void removeDrowned(Player owner){
        UUID uuid = owner.getUniqueId();
        if(drownedList.containsKey(uuid)){
            if(!drownedList.get(uuid).isDead()){
                drownedList.get(uuid).remove();
            }
            drownedList.remove(uuid);
        } 
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "drownmet";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§3Drownmet";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9While worn, summons a loyal drowned",
            "§9companion that wields a trident and",
            "§9wears a turtle helmet. The drowned",
            "§9automatically targets nearby enemies",
            "§9and teleports if too far from you.",
            "",
            "§6Armor",
            "§9Summon",
            "§9§obeangame",
            "§9", "§7When on Head:", "§9+3 Armor"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:protection", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.TURTLE_HELMET;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public ArmorTrim getArmorTrim(){
        return new ArmorTrim(TrimMaterial.DIAMOND, TrimPattern.SILENCE);
    }

    @Override
    public int getArmor(){
        return 3;
    }

    @Override
    public EquipmentSlotGroup getSlot(){
        return EquipmentSlotGroup.HEAD;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
