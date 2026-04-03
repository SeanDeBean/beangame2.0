package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.general.BGResetableI;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class DuelistsDance extends BeangameItem implements BGDDealerHeldI, BGResetableI {

    private enum Direction {
        NORTH, EAST, SOUTH, WEST;

        public static Direction fromYaw(float yaw) {
            yaw = (yaw % 360 + 360) % 360;

            if (yaw >= 315 || yaw < 45) return SOUTH;
            if (yaw < 135) return WEST;
            if (yaw < 225) return NORTH;
            return EAST;
        }

        public Direction getOpposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case EAST  -> WEST;
                case WEST  -> EAST;
            };
        }
    }

    private final Map<UUID, Set<Direction>> attackerDirections = new HashMap<>();
    private final Map<UUID, UUID> attackerTargets = new HashMap<>();
    private final Map<UUID, BukkitTask> comboResetTasks = new HashMap<>();

    private void resetComboTimer(UUID attackerId) {
        cancelResetTask(attackerId);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            attackerDirections.remove(attackerId);
            attackerTargets.remove(attackerId);
            comboResetTasks.remove(attackerId);
        }, 12 * 20L); // 12 seconds
        comboResetTasks.put(attackerId, task);
    }

    private void cancelResetTask(UUID attackerId) {
        BukkitTask existing = comboResetTasks.remove(attackerId);
        if (existing != null) existing.cancel();
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        UUID attackerId = attacker.getUniqueId();
        UUID victimId = victim.getUniqueId();
        Direction facing = Direction.fromYaw(attacker.getLocation().getYaw());

        // Reset on new target
        if (!victimId.equals(attackerTargets.get(attackerId))) {
            attackerTargets.put(attackerId, victimId);
            attackerDirections.put(attackerId, new HashSet<>());
            cancelResetTask(attackerId);
        }

        Set<Direction> hitDirections = attackerDirections.computeIfAbsent(attackerId, k -> new HashSet<>());
        boolean isNewDirection = hitDirections.add(facing);

        // Show particle feedback from VICTIM's point of view (so flip the direction)
        showDirectionParticles(victim, hitDirections.stream().map(Direction::getOpposite).collect(Collectors.toSet()));

        if (isNewDirection) {
            victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1.2, 0), 10, 0.2, 0.2, 0.2, 0.01);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

            if (hitDirections.size() >= 4) {
                double bonusDamage = 5.0;
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    if(victim instanceof Player v){
                        v.setNoDamageTicks(0);
                    }
                    victim.damage(bonusDamage, attacker);
                }, 1L);
                attacker.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§aAll vitals struck!"));

                victim.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, victim.getLocation().add(0, 1, 0), 40, 0.6, 1.0, 0.6, 0);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.4f, 0.8f);

                attackerDirections.remove(attackerId);
                attackerTargets.remove(attackerId);
                cancelResetTask(attackerId);
                return;
            }

            // Only reset timer if we actually added a new direction
            resetComboTimer(attackerId);


            int duration = 20 * 5; // 5 seconds
            int newLevel = 0;
            if (attacker.hasPotionEffect(PotionEffectType.SPEED)) {
                PotionEffect current = attacker.getPotionEffect(PotionEffectType.SPEED);
                newLevel = Math.min(current.getAmplifier() + 1, 3); // increase level by 1
            }
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, newLevel, true, true));
        }
    }

    private void showDirectionParticles(LivingEntity center, Set<Direction> hit) {
        Location base = center.getLocation().add(0, 1, 0);
        World world = center.getWorld();

        // Draw direction lines
        for (Direction dir : Direction.values()) {
            Vector offset = getDirectionVector(dir).multiply(2.2);
            Location end = base.clone().add(offset);
            Color color = hit.contains(dir) ? Color.LIME : Color.RED;
            drawLine(world, base, end, Particle.DUST, color);
        }

        // Draw light purple circle at the same radius
        drawCircle(world, base, 2.2, Color.FUCHSIA);
    }

    private void drawCircle(World world, Location center, double radius, Color color) {
        int points = 40; // smoother circle
        double y = center.getY();

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);

            world.spawnParticle(
                Particle.DUST,
                new Location(world, x, y, z),
                0,
                new Particle.DustOptions(color, 1f)
            );
        }
    }

    private void drawLine(World world, Location from, Location to, Particle particle, Color color) {
        Vector direction = to.toVector().subtract(from.toVector());
        double length = direction.length();
        Vector step = direction.normalize().multiply(0.2);

        Location current = from.clone();
        for (double i = 0; i < length; i += 0.2) {
            world.spawnParticle(
                Particle.DUST,
                current,
                0,
                new Particle.DustOptions(color, 1f)
            );
            current.add(step);
        }
    }

    private Vector getDirectionVector(Direction dir) {
        return switch (dir) {
            case NORTH -> new Vector(0, 0, -1);
            case EAST  -> new Vector(1, 0, 0);
            case SOUTH -> new Vector(0, 0, 1);
            case WEST  -> new Vector(-1, 0, 0);
        };
    }

    @Override
    public void resetItem(){
        attackerDirections.clear();
        attackerTargets.clear();
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "duelistsdance";
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
        return "§dDuelist's Dance";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cHitting enemies from different directions",
            "§cbuilds a combo. Striking from all four",
            "§cdirections (north, east, south, west)",
            "§cdeals bonus damage and resets the combo.",
            "§cCombo expires after 12 seconds.",
            "",
            "§cOn Hit",
            "§dOn Hit Applier",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:sharpness", 2);
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_SWORD;
    }

    @Override
    public int getCustomModelData() {
        return 104;
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
