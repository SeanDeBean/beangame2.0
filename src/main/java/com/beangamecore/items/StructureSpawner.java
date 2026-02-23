package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;

import java.util.List;
import java.util.Map;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class StructureSpawner extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location playerLoc = player.getLocation();

        // Get the highest block at the player's X/Z position
        int surfaceY = world.getHighestBlockYAt(playerLoc);

        // Ensure Y is safe (not too high/low)
        if (surfaceY < world.getMinHeight()) {
            surfaceY = world.getMinHeight();
        }

        // Final structure spawn location
        Location spawnLoc = new Location(world, playerLoc.getBlockX(), surfaceY, playerLoc.getBlockZ());

        // Decrease item stack
        stack.setAmount(stack.getAmount() - 1);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§5A structure has been spawned!"));

        // Pick a random structure
        String structure = commands[(int) Math.floor(Math.random() * commands.length)];

        // Spawn structure in the correct world at the surface
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "execute in " + world.getName() + " run place structure minecraft:" + structure + 
            " " + spawnLoc.getBlockX() + " " + spawnLoc.getBlockY() + " " + spawnLoc.getBlockZ()
        );

        // Apply fire resistance if Bastion Remnant is spawned
        if (structure.equals("bastion_remnant")) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (players.getGameMode().equals(GameMode.SURVIVAL)) {
                    players.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0));
                }
            }
        }

        return true;
    }

    public String[] commands = new String[] { 
        "bastion_remnant", "desert_pyramid", "end_city", "jungle_pyramid", "fortress", "mansion", "mineshaft_mesa", "pillager_outpost", "village_plains"
    };

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "structurespawner";
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
        return "§5Structure Spawner";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to spawn a random structure",
            "§9at the highest surface block.",
            "§9Grants Fire Resistance if Bastion spawned.",
            "",
            "§9Castable",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_INGOT;
    }

    @Override
    public int getCustomModelData() {
        return 103;
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

