package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.beangamecore.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

public class Chronobreak extends BeangameItem implements BGMPTalismanI, BGRClickableI {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation();
        Double health = player.getHealth();
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> chronobreakTracking.put(uuid, new ChronobreakState(health, loc)), 160L);
    }

    public static class ChronobreakState{
        boolean shortened;
        Location location;
        double health;
        int hunger;
        float saturation;
        ItemStack[] inventory;
        public ChronobreakState(double health, Location location){
            this.health = health;
            this.location = location;
            shortened = true;
        }
        public ChronobreakState(Location location, double health, int hunger, float saturation, ItemStack[] inventory){
            this.location = location;
            this.health = health;
            this.hunger = hunger;
            this.saturation = saturation;
            this.inventory = inventory;
            shortened = false;
        }
    }

    Map<UUID, Chronobreak.ChronobreakState> chronobreakTracking = new HashMap<>();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        if(!chronobreakTracking.containsKey(uuid)){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§aNo location as of 8 seconds ago!"));
            return false;
        }
        applyCooldown(uuid);
        Chronobreak.ChronobreakState state = chronobreakTracking.get(uuid);
        Location loc = player.getEyeLocation();
        Location loc2 = player.getLocation();
        particleCloud(loc);
        particleCloud(loc2);
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0);
        player.setNoDamageTicks(7);
        loc2 = state.location;
        player.teleport(loc2, TeleportCause.SPECTATE);
        loc2.add(0, 1.5, 0);
        double health = Math.min(state.health, attribute.getValue());
        player.setHealth(health);
        if(!state.shortened){
            player.setSaturation(state.saturation);
            player.setFoodLevel(state.hunger);
            player.getInventory().setContents(state.inventory);
        }
        Main.getPlugin().getParticleManager().particleTrail(loc, loc2, 255, 255, 0);
        return true;
    }

    private void particleCloud(Location loc){
        DustOptions dust = new DustOptions(Color.fromRGB(255, 255, 0), 0.9f); // size 0.9
        World w = loc.getWorld();
        for (int j = 0; j < 12; j++) { // spawn 12 particles each tick
            double offsetX = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5
            double offsetY = Math.random() * 1.5;        // random 0 to 1.5 (height)
            double offsetZ = (Math.random() - 0.5) * 1.0; // random -0.5 to 0.5

            Location particleLoc = loc.clone().add(offsetX, offsetY, offsetZ);

            w.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dust);
        }
    }

    public void chronobreakReset(Player player){
        chronobreakTracking.remove(player.getUniqueId());
    }

    @Override
    public long getBaseCooldown() {
        return 24000L;
    }

    @Override
    public String getId() {
        return "chronobreak";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " L ", "C R", " S ", r.mCFromMaterial(Material.LAPIS_BLOCK), r.mCFromMaterial(Material.CLOCK), r.mCFromMaterial(Material.RECOVERY_COMPASS), r.eCFromBeangame(Key.bg("stopwatch")));
        return null;
    }

    @Override
    public String getName() {
        return "§aChronobreak";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to rewind yourself 8 seconds",
            "§9back in time, restoring your health",
            "§9and position from that moment.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    public int getCustomModelData() {
        return 102;
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

