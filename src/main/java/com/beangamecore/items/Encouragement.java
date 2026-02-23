package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.ItemNBT;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class Encouragement extends BeangameItem implements BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        Location loc = player.getLocation();
        encourageNearbyPlayers(loc, player);
        return true;
    }

    private void encourageNearbyPlayers(Location loc, Player sourcePlayer) {
        for (Player encouragementvictim : Bukkit.getOnlinePlayers()) {
            Location vloc = encouragementvictim.getLocation();
            if (isValidEncouragementTarget(loc, vloc, encouragementvictim)) {
                processPlayerItems(encouragementvictim);
            }
        }
    }

    private boolean isValidEncouragementTarget(Location sourceLoc, Location targetLoc, Player targetPlayer) {
        return targetLoc.getWorld().equals(sourceLoc.getWorld())
                && targetLoc.distance(sourceLoc) < 24.0D
                && targetPlayer.getGameMode().equals(GameMode.SURVIVAL);
    }

    private void processPlayerItems(Player encouragementvictim) {
        ItemStack[] itemsInHand = new ItemStack[] {
                encouragementvictim.getEquipment().getItemInMainHand(),
                encouragementvictim.getEquipment().getItemInOffHand()
        };
        for (ItemStack item : itemsInHand) {
            if (ItemNBT.hasBeanGameTag(item)) {
                BeangameItem bgItem = BeangameItemRegistry.getFromItemStackRaw(item);

                if (!(bgItem instanceof Encouragement)) {
                    bgItem.resetCooldown(encouragementvictim.getUniqueId());

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                        PlayerInteractEvent interactForce = new PlayerInteractEvent(
                                encouragementvictim,
                                Action.RIGHT_CLICK_AIR,
                                encouragementvictim.getEquipment().getItemInMainHand(),
                                null,
                                null);
                        Bukkit.getPluginManager().callEvent(interactForce);
                        bgItem.applyCooldown(encouragementvictim.getUniqueId());
                    }, 1L); // 1 tick delay
                }
            }
        }
    }
    
    @Override
    public long getBaseCooldown() {
        return 36000L;
    }

    @Override
    public String getId() {
        return "encouragement";
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
        return "§bEncouragement";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to force all nearby players",
            "§a(including yourself) to activate their ",
            "§aheld beangame items regardless of ",
            "§acooldown. Works within 24 blocks range.",
            "",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND;
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

