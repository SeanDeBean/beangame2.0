package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class BiggestSnowballEver extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        event.setCancelled(true);

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        applyCooldown(uuid);

        World world = player.getWorld();
        Location origin = player.getLocation();

        playSnowEffects(world, origin);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            spawnAndAnimateSnowball(world, player);
        }, 20L);

        

        return true;
    }

    private void spawnAndAnimateSnowball(World world, Player player) {
        Location startLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(1));
        ItemDisplay snowball = (ItemDisplay) world.spawnEntity(startLoc, EntityType.ITEM_DISPLAY);
        snowball.setItemStack(new ItemStack(Material.SNOW_BLOCK));
        snowball.setBillboard(Display.Billboard.CENTER);
        snowball.setInterpolationDuration(1);
        snowball.setTeleportDuration(1);

        final float[] scale = { 0.5f };
        final float maxScale = 2.5f;
        final Map<UUID, Long> hitCooldowns = new HashMap<>();
        final float[] rotation = { 0f };

        snowball.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf().rotationX(rotation[0]),
                new Vector3f(scale[0], scale[0], scale[0]),
                new Quaternionf()));

        int[] taskId = new int[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            if (!player.isOnline() || !snowball.isValid()) {
                snowball.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            if (!player.isSprinting()) {
                explodeSnowball(snowball.getLocation(), world, player, snowball, scale[0]);
                snowball.remove();
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }

            Vector forward = player.getLocation().getDirection().normalize();
            double distanceAhead = 1.35 + scale[0]; // scales with size
            Location eye = player.getEyeLocation();

            Vector offset = forward.clone().multiply(distanceAhead);

            // Clamp the vertical offset to stay near eye level
            double clampedY = Math.max(-0.5, Math.min(0.5, offset.getY()));
            offset.setY(clampedY);

            Location newLoc = eye.add(offset);
            snowball.teleport(newLoc);

            // Rotate the snowball like it's rolling forward
            rotation[0] -= 0.07f;
            snowball.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf().rotationX(rotation[0]),
                    new Vector3f(scale[0], scale[0], scale[0]),
                    new Quaternionf()));

            // Grow
            if (scale[0] < maxScale) {
                scale[0] += 0.010;
            }

            // Trail particles
            world.spawnParticle(Particle.SNOWFLAKE, newLoc, 4, 0.2, 0.2, 0.2, 0.02);

            // Scaled AOE damage and knockback
            applyAOEDamageAndKnockback(snowball, scale[0], player, forward, hitCooldowns, world);
        }, 0, 1).getTaskId();
    }

    private void applyAOEDamageAndKnockback(ItemDisplay snowball, float scale, Player player, Vector forward,
            Map<UUID, Long> hitCooldowns, World world) {
        for (Entity e : snowball.getNearbyEntities(scale, scale, scale)) {
            if (isValidTarget(e, player)) {
                applyDamageAndEffects((LivingEntity) e, player, forward, hitCooldowns);
            }
        }
    }

    private boolean isValidTarget(Entity e, Player player) {
        return e instanceof LivingEntity le &&
                !(le instanceof ArmorStand) &&
                !e.equals(player);
    }

    private void applyDamageAndEffects(LivingEntity le, Player player, Vector forward, Map<UUID, Long> hitCooldowns) {
        long now = System.currentTimeMillis();
        UUID id = le.getUniqueId();

        if (!hitCooldowns.containsKey(id) || now - hitCooldowns.get(id) >= 1000) {
            hitCooldowns.put(id, now);
            le.damage(1.0, player);
            boolean hasKBResistance = false;
            if(le instanceof Player){
                Player pVictim = (Player) le;
                hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
            }
            if(!hasKBResistance){
                le.setVelocity(forward.clone().multiply(0.78).setY(0.25));
            }
            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            if (le instanceof Player victim) {
                victim.setFreezeTicks(120);
            }
        }
    }

    private void playSnowEffects(World world, Location origin) {
        world.playSound(origin, Sound.BLOCK_SNOW_PLACE, 1f, 1f);
        world.spawnParticle(Particle.SNOWFLAKE, origin.clone().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0.1);
    }

    private void explodeSnowball(Location loc, World world, Player player, Entity source, float scale) {
        world.spawnParticle(Particle.EXPLOSION, loc, 1);
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

        for (Entity e : source.getNearbyEntities(scale * 1.5, scale * 1.5, scale * 1.5)) {
            if (e instanceof LivingEntity le && !e.equals(player)) {
                le.damage(4.0, player);
                boolean hasKBResistance = false;
                if(le instanceof Player){
                    Player pVictim = (Player) le;
                    hasKBResistance = pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                            pVictim.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;
                }
                if(!hasKBResistance){
                    le.setVelocity(e.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.2).setY(0.6));
                }
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                if(le instanceof Player victim){
                    victim.setFreezeTicks(120);
                }
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 32000L;
    }

    @Override
    public String getId() {
        return "biggestsnowballever";
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
        return "§bBiggest Snowball Ever!";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon a growing snowball",
            "§9that damages and slows enemies while",
            "§9you sprint. Explodes for area damage when",
            "§9you stop sprinting.",
            "",
            "§9Castable",
            "§dOn Hit Extender",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.SNOWBALL;
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

