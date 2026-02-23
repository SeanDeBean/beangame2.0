package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ItemMagnet extends BeangameItem implements BGLPTalismanI, BGRClickableI {
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack){
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)){
            return false;
        }
        applyCooldown(uuid);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25F, 0);
        if(stack.containsEnchantment(Enchantment.LUCK_OF_THE_SEA)){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9Toggled off!"));
            stack.removeEnchantment(Enchantment.LUCK_OF_THE_SEA);
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§cToggled on!"));
            stack.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
        }
        return true;
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        Location loc = player.getLocation();
        if(item.getItemMeta().getEnchants().isEmpty()){
            return;
        }
        for (Item i : player.getWorld().getEntitiesByClass(Item.class)) {
            if(i.getLocation().distance(loc) < 12.0D){
                i.teleport(loc);
            }
        }
    }

    @Override
    public long getBaseCooldown() {
        return 250L;
    }

    @Override
    public String getId() {
        return "itemmagnet";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "I I", "L R", " B ", r.mCFromMaterial(Material.IRON_INGOT), r.mCFromMaterial(Material.LAPIS_BLOCK), r.mCFromMaterial(Material.REDSTONE_BLOCK), r.eCFromBeangame(Key.bg("bean")));
        return null;
    }

    @Override
    public String getName() {
        return "§9Item Magnet";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Right-click to toggle magnet on/off.",
            "§3When active, pulls all items within",
            "§312 blocks toward you automatically.",
            "",
            "§3Talisman",
            "§5Tool",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
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

