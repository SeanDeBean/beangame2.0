package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.GlobalCooldowns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EquilibriumAmulet extends BeangameItem implements BGRClickableI {
    
    public static double EquilibriumAmuletCooldown = 27.0;

    public void equalibriumamuletReset(){
        EquilibriumAmuletCooldown = 27.0;
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown
        Player player = event.getPlayer();
        if (GlobalCooldowns.onCooldown("equilibriumamulet")) {
            GlobalCooldowns.sendCooldownMessage("equilibriumamulet", player);
            return false;
        }
        GlobalCooldowns.setCooldown("equilibriumamulet", getBaseCooldown());
        EquilibriumAmuletCooldown = Math.max(0.2, EquilibriumAmuletCooldown - 1);

        Location center = player.getWorld().getWorldBorder().getCenter();
        int y = calculateAverageTopY(player, center, 10);

        // effect on other players
        int floor = y - 16;
        int roof = y + 24;

        notifyAndAffectPlayers(floor, roof);

        return true;
    }

    private int calculateAverageTopY(Player player, Location center, int radius) {
        int ySum = 0;
        int yCount = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Location checkLoc = center.clone().add(x, 0, z);
                int topY = player.getWorld().getHighestBlockYAt(checkLoc); // Gets topmost non-air block at that column
                if (topY > 0) {
                    ySum += topY;
                    yCount++;
                }
            }
        }
        if (yCount > 0) {
            return ySum / yCount;
        } else {
            return center.getBlockY(); // Fallback in case all air (very rare)
        }
    }

    private void notifyAndAffectPlayers(int floor, int roof) {
        for (Player victim : Bukkit.getOnlinePlayers()) {
            victim.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacy("§dIt is safe between y=" + floor + " & y=" + roof));
            if (victim.getGameMode().equals(GameMode.SURVIVAL)) {
                Location vloc = victim.getLocation().add(0, 0.5, 0);
                Location vloc2 = victim.getLocation();
                double vy = victim.getLocation().getY();
                if (vy > roof) {
                    vloc2.setY(roof);
                    victim.damage(3);
                    Main.getPlugin().getParticleManager().particleTrail(vloc, vloc2, 255, 0, 255);
                } else if (vy < floor) {
                    vloc2.setY(floor);
                    victim.damage(3);
                    Main.getPlugin().getParticleManager().particleTrail(vloc, vloc2, 255, 0, 255);
                }
            }
        }
    }

    @Override
    public void setCooldown(UUID player, long millis){
        GlobalCooldowns.setCooldown("equilibriumamulet", millis);
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public long getBaseCooldown() {
        return (long) EquilibriumAmuletCooldown * 1000L;
    }
    @Override
    public void resetCooldown(UUID player){
        GlobalCooldowns.setCooldown("equilibriumamulet", -1);
    }
    @Override
    public String getId() {
        return "equilibriumamulet";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public String getName() {
        return "§dEquilibrium Amulet";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to create a safe zone based",
            "§9on the average terrain height. Players",
            "§9outside the zone take damage. Cooldown lowers ",
            "§9with each use from 27 to 0.2 seconds.",
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
        return Material.PHANTOM_MEMBRANE;
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

