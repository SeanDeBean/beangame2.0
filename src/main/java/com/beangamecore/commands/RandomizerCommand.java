package com.beangamecore.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;

public class RandomizerCommand implements CommandExecutor {
    
    private static boolean randomizer = false;
    private static List<ItemStack> allItems = new ArrayList<>();
    private static Random random = new Random();

    public static boolean getRandomizer(){
        return randomizer;
    }

    public static void setRandomizer(Boolean bool){
        randomizer = bool;
    }

    public static void randomDrops(BlockBreakEvent event){
        if (!randomizer || allItems.isEmpty()) {
            return; // Don't modify drops if randomizer is off or list is empty
        }
        event.setDropItems(false);
        Block block = event.getBlock();

        ItemStack randomItem = allItems.get(random.nextInt(allItems.size()));
        Item item = block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), randomItem);
        item.setTicksLived(5800);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bg.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        toggleRandomizer();
        return true;
    }

    private void toggleRandomizer() {
        randomizer = !randomizer;

        if (randomizer) {
            initializeItemList();
        } else {
            // Clear the list when disabled to save memory
            allItems.clear();
        }

        String message = randomizer ? createRainbowText("RANDOMIZER ENABLED!") : createRainbowText("RANDOMIZER DISABLED!");

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(null, message, 20, 100, 20);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.4F);
        }

    }

    private void initializeItemList() {
        allItems.clear(); // Clear existing items

        for (Material material : Material.values()) {
            // Filter out non-items (like AIR, VOID_AIR, etc.) and legacy materials
            if (isSuitableMaterial(material)) {
                allItems.add(new ItemStack(material, 1));
            }
        }

        for (BeangameItem bgitem : BeangameItemRegistry.getItemsInRotation()) {
            allItems.add(bgitem.asItem());
        }

        // Optional: Log how many items were loaded
        Bukkit.getLogger().info("Loaded " + allItems.size() + " items for randomizer!");
    }

    @SuppressWarnings("deprecation")
    private boolean isSuitableMaterial(Material material) {
        return material.isItem() && !material.isAir() && !material.isLegacy();
    }

    private String createRainbowText(String text) {
        ChatColor[] colors = {ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, 
                            ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE};
        StringBuilder rainbow = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            rainbow.append(colors[i % colors.length]).append(text.charAt(i));
        }
        return rainbow.toString();
    }
}

