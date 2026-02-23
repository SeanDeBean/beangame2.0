package com.beangamecore.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.beangamecore.commands.BeangameStart;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.registry.BeangameItemRegistry;
import com.beangamecore.util.Cooldowns;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class WallBreakers extends BeangameItem implements BGRClickableI, BGMPTalismanI {

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        int currentModel = meta.getCustomModelData();
        int newModel = determineNextModel(player);
        
        if(currentModel == newModel) {
            return;
        }

        meta.setCustomModelData(newModel);
        item.setItemMeta(meta);
    }

    private int determineNextModel(Player player) {
        if(onCooldown(player.getUniqueId()) || countRevivablePlayers() < 2) {
            return 104; // default model
        }
        return 105; // active model
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        // cooldown system
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        int count = countRevivablePlayers();
        if(count < 2){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy("§5There needs to be at least 2 dead players to use this item!"));
            return false;
        }

        if (onCooldown(uuid)){
            sendCooldownMessage(player);
            return false;
        }
        applyCooldown(uuid);

        // Item event - play sound and action bar message
        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_FOX_SCREECH, 3.0F, 1.0F);

        Player[] summonedPlayers = getTwoRandomSpectatorPlayers();
        for (Player summonedPlayer : summonedPlayers) {

            summonedPlayer.teleport(player);
            summonedPlayer.getInventory().clear();
            summonedPlayer.setGameMode(GameMode.SURVIVAL);
            if(!BeangameStart.alivePlayers.contains(summonedPlayer.getUniqueId())){
                BeangameStart.alivePlayers.add(summonedPlayer.getUniqueId());
            }

            Cooldowns.setCooldown("fall_damage_immunity", summonedPlayer.getUniqueId(), 3500L);

            // Apply wither effect and equip suicide vest here
            BeangameItem suicidevestItem = BeangameItemRegistry.getRaw("suicidevest");
            if (suicidevestItem != null) {
                ItemStack suicidevest = suicidevestItem.asItem();
                suicidevest.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
                summonedPlayer.getEquipment().setChestplate(suicidevest);
            }

            summonedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, -1, 9));
            if(count > 10) {
                summonedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, -1, 4));
            }
            summonedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 0));

            summonedPlayer.getWorld().playSound(summonedPlayer.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0);

            int total = Bukkit.getOnlinePlayers().size();
            
            if (total > 0) {
                double deathPercentage = (double) count / total;
                double healthReduction = 20 * deathPercentage;
                summonedPlayer.setHealth(Math.max(20 - healthReduction, 2));
            }


            summonedPlayer.setVelocity(loc.getDirection().multiply(1.1));
        }
        
        return true;
    }

    public Player[] getTwoRandomSpectatorPlayers() {
        List<Player> eligiblePlayers = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerRevivable(player, player.getUniqueId())) {
                eligiblePlayers.add(player);
            }
        }
        
        if (eligiblePlayers.size() < 2) {
            return new Player[0]; // or return null, depending on your needs
        }
        
        Collections.shuffle(eligiblePlayers);
        return new Player[]{eligiblePlayers.get(0), eligiblePlayers.get(1)};
    }

    private boolean isPlayerRevivable(Player player, UUID uuid) {
        return player.getGameMode() == GameMode.SPECTATOR &&  !Revive.noRevive.contains(uuid);
    }

    private int countRevivablePlayers() {
        int count = 0;
        for (Player players : Bukkit.getOnlinePlayers()) {
            UUID uuid = players.getUniqueId();
            if (isPlayerRevivable(players, uuid)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public long getBaseCooldown() {
        return 80000L;
    }

    @Override
    public String getId() {
        return "wallbreakers";
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
        return "§5Wall Breakers";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to summon 2 players from ",
            "§9spectator mode with permanent wither",
            "§9and wearing suicide vests.",
            "",
            "§9Summon",
            "§aSupport",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.BLACK_DYE;
    }

    @Override
    public int getCustomModelData() {
        return 104;
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

