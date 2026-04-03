package com.beangamecore.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGConsumableI;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.general.BGResetableI;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Tomatior extends BeangameItem implements BGConsumableI, BGRClickableI, BGResetableI {
    
    @Override
    public void resetItem(){
        tomatiorcount.clear();
    }

    private static final Map<UUID, Integer> tomatiorcount = new HashMap<>();

    @Override
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ItemStack item = event.getItem().clone();
        handleOffHandItem(player, item);
        handleHotbarItems(player, item);

        handleTomatiorCount(player, uuid);
    }

    private void handleOffHandItem(Player player, ItemStack item) {
        if (item.equals(player.getEquipment().getItemInOffHand())) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(),
                    () -> player.getEquipment().setItemInOffHand(item), 1);
        }
    }

    private void handleHotbarItems(Player player, ItemStack item) {
        for (int i = 0; i < 9; i++) {
            if (item.equals(player.getInventory().getItem(i))) {
                final int j = i;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(),
                        () -> player.getInventory().setItem(j, item), 1);
            }
        }
    }

    private void handleTomatiorCount(Player player, UUID uuid) {
        if (tomatiorcount.containsKey(uuid)) {
            int count = tomatiorcount.get(uuid);
            if (count < 9) { // adds a stack
                count++;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacy("§c" + count + " stacks"));
            } else { // explodes
                count = 0;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cBOOM!"));
                player.getWorld().createExplosion(player.getLocation(), 3.5F, false, true, player);
            }
            tomatiorcount.put(uuid, count);
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§c1 stack"));
            tomatiorcount.put(uuid, 1);
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // cooldown system
        if (tomatiorcount.containsKey(uuid) && tomatiorcount.get(uuid) == 9){
            // cooldown system
            if (onCooldown(uuid)){
                return false;
            }
            applyCooldown(uuid);
            World world = player.getWorld();
            Location loc = player.getLocation();
            world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 1F, 0);
            world.spawnParticle(Particle.FLAME, loc, 3);
        }
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 200L;
    }

    @Override
    public String getId() {
        return "tomatior";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public boolean isInFoodItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " M ", "TFT", "TRT", r.mCFromMaterial(Material.MANGROVE_PROPAGULE), r.mCFromMaterial(Material.TNT), r.eCFromBeangame(Key.bg("feast")), r.mCFromMaterial(Material.REDSTONE_BLOCK));
        return null;
    }

    @Override
    public String getName() {
        return "§2Tomatior";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§2Consume to build up explosive stacks.",
            "§2Explodes after 10 bites.",
            "",
            "§2Food",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.APPLE;
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

