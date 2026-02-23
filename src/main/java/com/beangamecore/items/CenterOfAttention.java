package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGLPTalismanI;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CenterOfAttention extends BeangameItem implements BGLPTalismanI, BGRClickableI {
    
    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        if (item.getItemMeta().getEnchants().isEmpty()) {
            return;
        }
        
        int i = 1;
        for (Player centerofattentionvictim : Bukkit.getOnlinePlayers()) {
            i++;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), 
                new PullTask(player, centerofattentionvictim, uuid), i);
        }
    }

    private static class PullTask implements Runnable {
        private final Player player;
        private final Player victim;
        private final UUID uuid;
        
        public PullTask(Player player, Player victim, UUID uuid) {
            this.player = player;
            this.victim = victim;
            this.uuid = uuid;
        }
        
        @Override
        public void run() {
            Location aloc = player.getLocation();
            Location vloc = victim.getLocation();
            if (isPlayerAffected(victim, uuid, aloc, vloc)) {
                victim.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacy("§9Pulled!"));
                victim.setVelocity(aloc.toVector().subtract(vloc.toVector()).multiply(0.2));
            }
        }

        private boolean isPlayerAffected(Player target, UUID sourceUUID, Location sourceLoc, Location targetLoc) {
            boolean hasKBResistance = target.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE) != null && 
                        target.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE).getValue() >= 0.7;

            return !hasKBResistance &&
                    targetLoc.getWorld().equals(sourceLoc.getWorld()) &&
                    targetLoc.distance(sourceLoc) < 12.0D &&
                    !target.getUniqueId().equals(sourceUUID) &&
                    target.getGameMode().equals(GameMode.SURVIVAL);
        }
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack item){
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // cooldown system
        if (onCooldown(uuid)){
            return false;
        }
        applyCooldown(uuid);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25F, 0);
        if(item.containsEnchantment(Enchantment.LUCK_OF_THE_SEA)){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9Toggled off!"));
            item.removeEnchantment(Enchantment.LUCK_OF_THE_SEA);
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§9Toggled on!"));
            item.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
        }
        return true;
    }

    @Override
    public long getBaseCooldown() {
        return 250L;
    }

    @Override
    public String getId() {
        return "centerofattention";
    }

    @Override
    public boolean isInItemRotation() {
        return true;
    }

    @Override
    public CraftingRecipe getCraftingRecipe() {
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, " P ", "OBF", " A ", r.mCFromMaterial(Material.MUSIC_DISC_PIGSTEP), r.mCFromMaterial(Material.MUSIC_DISC_OTHERSIDE), r.eCFromBeangame(Key.bg("bean")), r.mCFromMaterial(Material.MUSIC_DISC_5), r.mCFromMaterial(Material.AXOLOTL_BUCKET));
        return null;
    }

    @Override
    public String getName() {
        return "§9Center of Attention";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Right-click to toggle an aura that",
            "§3pulls nearby players toward you.",
            "§3When active, creates a gravitational",
            "§3pull within 12 blocks.",
            "",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of("minecraft:luck_of_the_sea", 1);
    }

    @Override
    public Material getMaterial() {
        return Material.RECOVERY_COMPASS;
    }

    @Override
    public int getCustomModelData() {
        return 0;
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

