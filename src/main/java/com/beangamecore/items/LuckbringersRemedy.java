package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import com.beangamecore.Main;
import com.beangamecore.commands.BeangameStart;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

public class LuckbringersRemedy extends BeangameItem implements BGRClickableI {
    
    private final Random random = new Random();

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        applyCooldown(uuid);

        // Roll the dice
        int survivalCount = (int) BeangameStart.alivePlayers.size();
        int cappedSurvivors = Math.min(survivalCount, 10);

        double bias = cappedSurvivors / 10.0;

        double raw = random.nextDouble();
        double biasedRandom = Math.pow(raw, 1.0 - bias);
        
        // Convert to 1-6 range
        int diceRoll = (int) Math.floor(biasedRandom * 6) + 1;
        
        // Ensure it's within bounds (shouldn't be necessary but safety check)
        diceRoll = Math.max(1, Math.min(6, diceRoll));

        // Healing
        int healAmount = diceRoll * 2;
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            player.setHealth(Math.min(player.getHealth() + healAmount, attr.getValue()));
        }

        // Bonus effect on high rolls
        if (diceRoll >= 5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1)); // 3s of Regen II
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 0)); // Absorption I
        }

        // Particles & sound
        player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.2);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.2f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.5f, 1.5f);

        showRollHologram(player, diceRoll);

        return true;
    }

    private void showRollHologram(Player player, int diceRoll) {
        World world = player.getWorld();

        // Spawn armor stand with wool helmet (the "dice")
        ArmorStand stand = (ArmorStand) world.spawnEntity(player.getLocation().add(0, 2.2, 0), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.getEquipment().setHelmet(this.asItem());
        stand.setCustomNameVisible(true);
        stand.setCustomName(ChatColor.GOLD + "You rolled a " + ChatColor.YELLOW + diceRoll);

        // Use static inner class instead of anonymous inner class
        new DiceAnimationTask(player, stand).runTaskTimer(Main.getPlugin(), 0L, 1L);
    }

    // Static inner class to avoid hot-reload issues
    private static class DiceAnimationTask extends BukkitRunnable {
        private final Player player;
        private final ArmorStand stand;
        private int ticks = 0;
        
        public DiceAnimationTask(Player player, ArmorStand stand) {
            this.player = player;
            this.stand = stand;
        }
        
        @Override
        public void run() {
            // Check if we should remove the hologram
            if (!player.isOnline() || stand.isDead() || ticks > 50) {
                stand.remove();
                this.cancel();
                return;
            }

            // Spin head pose (helmet) - rotate around X/Y/Z
            EulerAngle current = stand.getHeadPose();
            double speed = 10 * Math.PI / 180; // 10 degrees per tick

            EulerAngle newPose = new EulerAngle(
                    current.getX() + speed, // pitch
                    current.getY() + speed, // yaw
                    current.getZ() + speed // roll
            );
            stand.setHeadPose(newPose);

            // Keep hologram above player
            stand.teleport(player.getLocation().add(0, 2.2, 0));

            ticks++;
        }
    }

    @Override
    public long getBaseCooldown() {
        return 37000L;
    }

    @Override
    public String getId() {
        return "luckbringersremedy";
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
        return "§aLuckbringer's Remedy";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to roll a 6-sided die.",
            "§9Heals 2 hearts per number rolled.",
            "§9Rolls 5-6 grant regeneration and",
            "§9absorption. Higher rolls are more",
            "§9likely with fewer players alive.",
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
        return Material.WHITE_WOOL;
    }

    @Override
    public int getCustomModelData() {
        return 101;
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
