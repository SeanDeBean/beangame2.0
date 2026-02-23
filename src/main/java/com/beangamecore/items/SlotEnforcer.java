package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.util.Cooldowns;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public class SlotEnforcer extends BeangameItem implements BGRClickableI {
    
    public void slotenforcerSlotChange(PlayerItemHeldEvent event){
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long slotenforcervictimcdr = Cooldowns.getRemainingCooldown("slot_enforced", uuid) / 1000L;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§5Slot enforced for another " + slotenforcervictimcdr + " second(s)!"));
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        event.setCancelled(true);
        // cooldown
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);
        // item event
        World world = player.getWorld();
        Location loc = player.getLocation();
        world.playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.0F);
        for (Player slotenforcervictim : Bukkit.getOnlinePlayers()) {
            UUID vuuid = slotenforcervictim.getUniqueId();
            Location vloc = slotenforcervictim.getLocation();
            if (isSlotEnforcerTarget(loc, uuid, slotenforcervictim, vloc)) {
                world.playSound(vloc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0F, 1.0F);
                Cooldowns.setCooldown("slot_enforced", vuuid, 9000L);
            }
        }
        return true;
    }

    private boolean isSlotEnforcerTarget(Location loc, UUID uuid, Player slotenforcervictim, Location vloc) {
        UUID vuuid = slotenforcervictim.getUniqueId();
        return vloc.getWorld().equals(loc.getWorld()) && vloc.distance(loc) < 24.0D && !vuuid.equals(uuid)
                && slotenforcervictim.getGameMode().equals(GameMode.SURVIVAL);
    }

    @Override
    public long getBaseCooldown() {
        return 55000L;
    }

    @Override
    public String getId() {
        return "slotenforcer";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "ETE", "PSP", "HIH", r.mCFromMaterial(Material.ICE), r.mCFromMaterial(Material.IRON_TRAPDOOR), r.mCFromMaterial(Material.PACKED_ICE), r.mCFromMaterial(Material.CALIBRATED_SCULK_SENSOR), r.mCFromMaterial(Material.HONEY_BLOCK), r.eCFromBeangame(Key.bg("immobilizer")));
        return null;
    }

    @Override
    public String getName() {
        return "§5Slot Enforcer";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to lock all nearby",
            "§9players' hotbar slots for 9",
            "§9seconds. Prevents them from",
            "§9switching held items.",
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
        return Material.NETHERITE_INGOT;
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

