package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDReceiverInvI;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.beangamecore.Main;

public class Treat extends BeangameItem implements BGRClickableI, BGDReceiverInvI {
    
    private void treatSlowness(Player player){
        World world = player.getWorld();
        List<LivingEntity> livingentities = world.getLivingEntities();
        Location aloc = player.getLocation();
        for(LivingEntity livingentity : livingentities){
            if(player.getLocation().distance(livingentity.getLocation()) <= 6 && !livingentity.getUniqueId().equals(player.getUniqueId())){
                if(livingentity instanceof Player && !((Player)livingentity).getGameMode().equals(GameMode.SPECTATOR)){

                }
                Boolean hasKBResistance = false;
                hasKBResistance = ((Player)livingentity).getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        ((Player)livingentity).getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                if(!hasKBResistance){
                    livingentity.setVelocity(aloc.toVector().subtract(livingentity.getLocation().toVector()).multiply(0.2));
                }
                livingentity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 10, 1));
                livingentity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 10, 1));
            }
        }
    }

    private void biomeEffectBoost(Player player) {
        String biome = player.getLocation().getBlock().getBiome().toString();
        if (biome.contains("SNOW") || biome.contains("ICE")) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 0.5F);
            player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 50, 2, 2, 2);
        } else if (biome.contains("DESERT")) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_BULLET_HIT, 1F, 1.5F);
            player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation(), 50, 2, 2, 2);
        }
    }

    private void treatSpawnFangs(Player player){
        double[] distances = {2, 3, 4, 5};
        for(int i = 0; i < distances.length; i++){
            double distance = distances[i];
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                treatRing(player, distance);
            }, i * 5L);
        }
    }

    private void treatRing(Player player, double radius){
        World world = player.getWorld();
        for(int angle = 0; angle < 360; angle += 20){
            double radians = Math.toRadians(angle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);
            Vector position = player.getLocation().toVector().add(new Vector(x, 0, z));
            EvokerFangs fangs = (EvokerFangs) world.spawnEntity(position.toLocation(world), EntityType.EVOKER_FANGS);
            fangs.setSilent(true);
            world.playSound(fangs.getLocation(), Sound.ENTITY_EVOKER_FANGS_ATTACK, 0.2F, 1F);
            fangs.setOwner(player);
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        treatSpawnFangs(player);
        treatSlowness(player);
        biomeEffectBoost(player);
        return true;
    }

    @Override
    public void victimInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if(event.getDamager() instanceof EvokerFangs ef){
            if(ef.getOwner().equals(event.getEntity())){
                event.setCancelled(true);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 20000L;
    }

    @Override
    public String getId() {
        return "treat";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " A ", "AIC", " C ", r.mCFromMaterial(Material.ARMADILLO_SCUTE), r.eCFromBeangame(Key.bg("illagerwannabe")), r.mCFromMaterial(Material.CLAY_BALL));
        return null;
    }

    @Override
    public String getName() {
        return "§3Treat";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon evoker fangs in rings",
            "§9around you and apply Slowness and Mining",
            "§9Fatigue to nearby enemies. Mining Fatigue",
            "§9increases ability cooldowns for affected players.",
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
        return Material.CLAY_BALL;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

