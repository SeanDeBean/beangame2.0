package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.BGRClickableI;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class WardensRage extends BeangameItem implements BGRClickableI, BGVoicechat, BGVCMicPacket {

    private ConcurrentHashMap<UUID, Warden> activeWardens = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID, Integer> wardenTimers = new ConcurrentHashMap<>();
    
    @Override
    public boolean onRightClick(PlayerInteractEvent event, ItemStack stack) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player is on cooldown
        if (onCooldown(uuid)) {
            sendCooldownMessage(player);
            return false;
        }

        // Apply cooldown and spawn Warden
        applyCooldown(uuid);
        Warden warden = player.getWorld().spawn(player.getLocation(), Warden.class);
        warden.setCustomName("enraged " + player.getName());
        warden.setHealth(player.getHealth() * 3);

        AttributeInstance attribute = warden.getAttribute(Attribute.SCALE);
        attribute.setBaseValue(0.6);

        player.setGameMode(GameMode.SPECTATOR);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 600, 1, false, false));
        warden.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 600, 1, false, false));
        AtomicBoolean killedEarly = new AtomicBoolean(false);
        if(!Revive.noRevive.contains(uuid)) Revive.noRevive.add(uuid);

        activeWardens.put(uuid, warden);

        // Handle Warden behavior
        startWardenBehaviorTask(player, warden, killedEarly, uuid);

        // Schedule task to despawn the Warden after 30 seconds
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (killedEarly.get())
                return;
            if (shouldDespawnWarden(warden)) {
                Revive.noRevive.remove(uuid);
                activeWardens.remove(player.getUniqueId());
                return;
            }
            player.setGameMode(GameMode.SURVIVAL);
            player.setNoDamageTicks(20);
            player.teleport(warden.getLocation());
            activeWardens.remove(player.getUniqueId());
            Revive.noRevive.remove(uuid);
            wardenTimers.remove(uuid);
            warden.remove();
        }, 600);

        return false;
    }

    private boolean shouldDespawnWarden(Warden warden) {
        return warden == null || warden.getWorld() == null || !Bukkit.getWorlds().contains(warden.getWorld());
    }

    private void startWardenBehaviorTask(Player player, Warden warden, AtomicBoolean killedEarly, UUID uuid) {
        // Lambda version
        int[] taskId = new int[1];
        
        // Initialize timer (30 seconds = 600 ticks)
        wardenTimers.put(uuid, 600);
        
        taskId[0] = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            // Update and display timer
            int timeLeft = wardenTimers.getOrDefault(uuid, 0);
            if (timeLeft > 0) {
                timeLeft--;
                wardenTimers.put(uuid, timeLeft);
                
                // Show action bar timer to the player
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    float percentLeft = (float) timeLeft / 600 * 100;
                    
                    // Create a progress bar
                    int bars = 20;
                    int filledBars = (int) (percentLeft / 100 * bars);
                    StringBuilder progressBar = new StringBuilder();
                    progressBar.append("§a");
                    for (int i = 0; i < filledBars; i++) {
                        progressBar.append("|");
                    }
                    progressBar.append("§c");
                    for (int i = filledBars; i < bars; i++) {
                        progressBar.append("|");
                    }
                    
                    // Send action bar message
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.GRAY + "[" + progressBar.toString() + ChatColor.GRAY + "]"));
                }
            }
            
            // Cancel task if world is unloaded or deleted
            if (shouldDespawnWarden(warden)) {
                Bukkit.getScheduler().cancelTask(taskId[0]); // World is gone, just stop the task
                wardenTimers.remove(uuid);
                return;
            }

            // Kill player if Warden is null or dead
            if (warden == null || !warden.isValid()) {
                killedEarly.set(true);
                Revive.noRevive.remove(uuid);
                player.setGameMode(GameMode.SURVIVAL);
                if (activeWardens.containsKey(player.getUniqueId())) {
                    player.damage(999); // Kill the player
                    activeWardens.remove(player.getUniqueId());
                }
                Bukkit.getScheduler().cancelTask(taskId[0]);
                wardenTimers.remove(uuid);
                return;
            }

            // Normal Warden behavior
            if (!killedEarly.get()) {
                executeNormalWardenBehavior(player, warden);
            } else {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                wardenTimers.remove(uuid);
            }
        }, 2, 1).getTaskId();
    }

    private void executeNormalWardenBehavior(Player player, Warden warden) {
        Player result = null;
        double lastDistance = Double.MAX_VALUE;
        for (Player p : player.getWorld().getPlayers()) {
            if (shouldContinue(player, p))
                continue;
            double distance = player.getLocation().distance(p.getLocation());
            if (distance < lastDistance) {
                lastDistance = distance;
                result = p;
            }
        }
        if (result != null) {
            warden.setTarget(result);
            warden.setAnger(result, 999999);
        }
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            player.setSpectatorTarget(warden);
        }
        if (Math.random() > 0.75) {
            warden.getWorld().spawnParticle(Particle.SCULK_SOUL,
                    warden.getEyeLocation().subtract(0, 0.5, 0), 1, 0, 0, 0, 0.25);
        }
    }

    private boolean shouldContinue(Player player, Player p) {
        return player.equals(p) || p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public void onMicrophonePacket(MicrophonePacketEvent event) {
        if(!activeWardens.containsKey(event.getSenderConnection().getPlayer().getUuid())) return;
        VoicechatServerApi api = event.getVoicechat();

        Player player = (Player)event.getSenderConnection().getPlayer().getPlayer();

        if(player == null){
            return;
        }

        event.cancel();

        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.equals(player)){
                continue;
            }
            if(!p.getWorld().equals(player.getWorld())){
                continue;
            }
            VoicechatConnection connection = api.getConnectionOf(p.getUniqueId());

            if(connection != null){
                api.sendLocationalSoundPacketTo(
                        connection,
                        event.getPacket().locationalSoundPacketBuilder()
                                .position(event.getSenderConnection().getPlayer().getPosition())
                                .build());
            }
        }
    }

    @Override
    public void onInitialize(VoicechatServerApi api) {

    }

    @Override
    public long getBaseCooldown() {
        return 90000L;
    }

    @Override
    public String getId() {
        return "wardensrage";
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
        return ChatColor.DARK_AQUA+"Warden's Rage";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§9Right-click to channel your inner fury,",
            "§9transforming into a warden for 30 seconds.",
            "§9Your warden form has 3x your health and",
            "§9seeks out the nearest enemies.",
            "§9If the warden dies, you die instantly.",
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
        return Material.ECHO_SHARD;
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
    public ArmorTrim getArmorTrim() {
        return null;
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getArmor() {
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

