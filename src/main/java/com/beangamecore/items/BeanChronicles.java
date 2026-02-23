package com.beangamecore.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.items.type.BGRClickableI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

public class BeanChronicles extends BeangameItem implements BGRClickableI {

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0F, 1.0F);
        player.sendTitle(null, "§3Items arriving soon!", 20, 100, 20);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"title " + player.getName() + " title {\"text\":\"\u0030\",\"font\":\"customfont:images\"}");
        item.setAmount(item.getAmount() - 1);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), new Runnable(){
            public void run(){
                // creating inventory
                Inventory bginv = Bukkit.createInventory(null, 27, String.valueOf(ChatColor.GOLD) + "Beangame!");
                ItemStack backgroundglass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = backgroundglass.getItemMeta();
                meta.setHideTooltip(true);
                backgroundglass.setItemMeta(meta);
                for (int i : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26 }){
                    bginv.setItem(i, backgroundglass);
                }
                ArrayList<BeangameItem> array = BeangameItemRegistry.getItemsInRotation();
                for (int i : new int[] { 10, 11, 12, 13, 14, 15, 16 }){
                    int rindex = (int)Math.round(Math.random() * (array.size()-1));
                    bginv.setItem(i, array.get(rindex).asItem());
                }
                // opening inventory
                player.openInventory(bginv);
            }
        }, 20L);
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "beanchronicles";
    }

    @Override
    public boolean isInItemRotation(){
        return false;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        return null;
    }

    @Override
    public String getName() {
        return "§6Bean Chronicles";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aRight-click to roll 7 random items",
            "§afrom the current rotation and",
            "§aselect one to add to your inventory.",
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
        return Material.ENCHANTED_BOOK;
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

