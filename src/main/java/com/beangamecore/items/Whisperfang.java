package com.beangamecore.items;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.items.type.general.BGResetableI;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Whisperfang extends BeangameItem implements BGDDealerInvI, BGRClickableI, BGResetableI {

    private final Map<UUID, WeakReference<LivingEntity>> lastHitTargets = new HashMap<>();

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Save last hit target
        lastHitTargets.put(attacker.getUniqueId(), new WeakReference<>(victim));
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        WeakReference<LivingEntity> ref = lastHitTargets.get(uuid);
        if (ref == null || ref.get() == null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§cNo valid target to teleport behind."));
            if (lastHitTargets.containsKey(uuid)) {
                lastHitTargets.remove(uuid);
            }
            return false;
        }

        LivingEntity target = ref.get();

        // Validate target: must be alive and not a spectator
        if (!isTargetValid(target)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§cYour last target is no longer valid."));
            lastHitTargets.remove(uuid);
            return false;
        }

        if (target.getLocation().distance(player.getLocation()) > 24) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§cYour last target is out of range."));
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_CHAIN_STEP, 0.7F, 0.8F);
            lastHitTargets.remove(uuid);
            return false;
        }

        // Apply cooldown
        applyCooldown(uuid);

        performTeleportAndEffects(player, target);

        target.damage(1, player);

        return true;
    }

    private boolean isTargetValid(LivingEntity target) {
        return !(target.isDead()
                || (!(target instanceof Player) && !target.isValid())
                || (target instanceof Player tPlayer && tPlayer.getGameMode() == GameMode.SPECTATOR));
    }

    private void performTeleportAndEffects(Player player, LivingEntity target) {
        Location victimLocation = target.getLocation();
        World world = player.getWorld();
        Location oldLoc = player.getLocation();

        // Calculate the teleport location
        double targetAngle = (victimLocation.getYaw() + 90.0F);
        if (targetAngle < 0.0D) {
            targetAngle += 360.0D;
        }
        double nX = Math.cos(Math.toRadians(targetAngle));
        double nZ = Math.sin(Math.toRadians(targetAngle));
        Location newLoc = new Location(world, victimLocation.getX() - nX, victimLocation.getY(),
                victimLocation.getZ() - nZ, victimLocation.getYaw(), victimLocation.getPitch());
        player.teleport(newLoc);

        player.getWorld().spawnParticle(Particle.SONIC_BOOM, oldLoc.add(0, 0.5, 0), 2);
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, newLoc.add(0, 0.5, 0), 2);
        player.getWorld().playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.4f, 1.5f);

        double maxHeight = Math.max(oldLoc.getY(), newLoc.getY()) + 1.5;
        // createSnakeTrail(start, end, maxHeight, dustOptions, random);
        createSnakeTrail(oldLoc, newLoc, maxHeight);
    }

    private void createSnakeTrail(Location start, Location end, double maxHeight) {
        DustOptions dustOptions = new DustOptions(Color.fromRGB(106, 13, 173), 0.7f);
        Random random = new Random();
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();
    
        for (double d = 0; d < length; d += 0.125) {
            double t = d / length; // Normalized position (0 to 1)
            double parabolicHeight = (4 * maxHeight - 4 * (start.getY() + (end.getY() - start.getY()) * t)) * t * (1 - t);
    
            Vector offset = new Vector(
                (random.nextDouble() - 0.5) * 0.3, // Horizontal randomness
                parabolicHeight + (random.nextDouble() - 0.5) * 0.5, // Arc height + vertical randomness
                (random.nextDouble() - 0.5) * 0.3 // Horizontal randomness
            );
    
            Location current = start.clone().add(direction.clone().multiply(d)).add(offset);
            start.getWorld().spawnParticle(Particle.DUST, current, 1, dustOptions);
        }
    }

    @Override
    public void resetItem() {
        lastHitTargets.clear();
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return 3200;
    }

    @Override
    public String getId() {
        return "whisperfang";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§5Whisperfang";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to vanish and reappear behind",
            "§9the last enemy you struck, striking again",
            "§9from the shadows within 24 blocks.",
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
        return Material.IRON_NUGGET;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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
