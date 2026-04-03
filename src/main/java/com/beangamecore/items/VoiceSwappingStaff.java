package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceSwappingStaff extends BeangameItem implements BGVoicechat, BGVCMicPacket, BGDDealerHeldI {

    private static ConcurrentHashMap<UUID, UUID> toSwap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<UUID, ActiveSwap> activeSwaps = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<UUID, Integer> effectTasks = new ConcurrentHashMap<>();
    
    private static final Color COLOR_PROXY = Color.fromRGB(100, 255, 100);
    private static final Color COLOR_LINK = Color.fromRGB(200, 100, 255);

    private static class ActiveSwap {
        final UUID player1;
        final UUID player2;
        final long expiryTime;
        
        ActiveSwap(UUID p1, UUID p2, long durationTicks) {
            this.player1 = p1;
            this.player2 = p2;
            this.expiryTime = System.currentTimeMillis() + (durationTicks * 50);
        }
        
        boolean involves(UUID player) {
            return player1.equals(player) || player2.equals(player);
        }
        
        UUID getOther(UUID player) {
            return player1.equals(player) ? player2 : player1;
        }
    }

    @Override
    public void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatServerApi api = event.getVoicechat();
        VoicechatConnection sender = event.getSenderConnection();

        if (sender != null) {
            handleSenderConnection(event, api, sender);
        }
    }

    private void handleSenderConnection(MicrophonePacketEvent event, VoicechatServerApi api, VoicechatConnection sender) {
        UUID pSender = sender.getPlayer().getUuid();
        UUID pOther = toSwap.get(pSender);
        
        if (pOther != null) {
            event.cancel();
            VoicechatConnection other = api.getConnectionOf(pOther);
            
            // FIX: Check if other connection exists
            if (other == null || other.getPlayer() == null) {
                // Target disconnected, clean up this swap
                cleanupPair(pSender, pOther);
                return;
            }
            
            showSpeakingEffect(pOther);
            sendLocationalSoundToAll(api, event, other, sender);
        }
    }

    private void showSpeakingEffect(UUID playerUuid) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null || !player.isOnline()) return;
        
        Location loc = player.getLocation().add(0, 2.5, 0);
        World world = player.getWorld();
        
        // Single note particle above head
        world.spawnParticle(Particle.DUST, loc, 1, 0.1, 0.1, 0.1, 
            new Particle.DustOptions(COLOR_PROXY, 0.8f));
    }

    private void sendLocationalSoundToAll(VoicechatServerApi api, MicrophonePacketEvent event, VoicechatConnection other, VoicechatConnection sender) {
        // Extra safety check
        if (other == null || other.getPlayer() == null) return;
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(sender.getPlayer().getUuid())) {
                continue;
            }

            VoicechatConnection connection = api.getConnectionOf(p.getUniqueId());
            if (connection == null) continue;

            api.sendLocationalSoundPacketTo(
                connection,
                event.getPacket()
                    .locationalSoundPacketBuilder()
                    .position(other.getPlayer().getPosition())
                    .build()
            );
        }
    }

    public void createPair(UUID p1, UUID p2) {
        disruptExistingSwaps(p1);
        disruptExistingSwaps(p2);
        
        ActiveSwap swap = new ActiveSwap(p1, p2, 240);
        activeSwaps.put(p1, swap);
        activeSwaps.put(p2, swap);
        
        addPair(p1, p2);
        addPair(p2, p1);
        
        startLinkEffect(p1, p2);
    }

    private void disruptExistingSwaps(UUID player) {
        ActiveSwap existing = activeSwaps.remove(player);
        if (existing != null) {
            UUID other = existing.getOther(player);
            
            toSwap.remove(player);
            toSwap.remove(other);
            activeSwaps.remove(other);
            
            cancelLinkEffect(player);
            cancelLinkEffect(other);
        }
    }

    void addPair(UUID p1, UUID p2) {
        toSwap.put(p1, p2);
        
        // Store task for cleanup
        int taskId = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            cleanupPair(p1, p2);
        }, 240).getTaskId();
        
        // Track expiry task
        effectTasks.put(p1, taskId);
    }

    private void cleanupPair(UUID p1, UUID p2) {
        // Only cleanup if this specific pair is still active
        ActiveSwap swap = activeSwaps.get(p1);
        if (swap != null && swap.involves(p2)) {
            toSwap.remove(p1);
            toSwap.remove(p2);
            activeSwaps.remove(p1);
            activeSwaps.remove(p2);
            
            cancelLinkEffect(p1);
            cancelLinkEffect(p2);
            
            Player player = Bukkit.getPlayer(p1);
            if (player != null) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    TextComponent.fromLegacy("§7♪ Voice link expired."));
            }
        }
    }

    public static UUID getSwappedPartner(UUID player) {
        return toSwap.get(player);
    }

    public static boolean isInActiveSwap(UUID player) {
        return activeSwaps.containsKey(player);
    }

    private void startLinkEffect(UUID p1, UUID p2) {
        int taskId = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), () -> {
            Player player1 = Bukkit.getPlayer(p1);
            Player player2 = Bukkit.getPlayer(p2);
            
            if (player1 == null || player2 == null || !player1.isOnline() || !player2.isOnline() || player1.getGameMode().equals(GameMode.SPECTATOR) || player2.getGameMode().equals(GameMode.SPECTATOR)) {
                cleanupPair(p1, p2);
                return;
            }
            
            // Check if swap should still be active
            ActiveSwap swap = activeSwaps.get(p1);
            if (swap == null || System.currentTimeMillis() > swap.expiryTime) {
                cleanupPair(p1, p2);
                return;
            }
            
            drawLinkLine(player1.getLocation(), player2.getLocation());
            
        }, 0L, 20L).getTaskId(); // Every 1 second (slower)
        
        effectTasks.put(p1, taskId);
        effectTasks.put(p2, taskId);
    }

    private void drawLinkLine(Location loc1, Location loc2) {
        Vector direction = loc2.toVector().subtract(loc1.toVector());
        double distance = direction.length();
        direction.normalize();
        
        World world = loc1.getWorld();
        int points = (int) (distance * 2); // Fewer points
        
        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            Location point = loc1.clone().add(direction.clone().multiply(distance * t)).add(0, 1, 0);
            
            world.spawnParticle(Particle.DUST, point, 1, 
                new Particle.DustOptions(COLOR_LINK, 0.6f));
        }
    }

    private void cancelLinkEffect(UUID player) {
        Integer taskId = effectTasks.remove(player);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        // Also remove expiry task if exists
        Integer expiryTask = effectTasks.remove(player);
        if (expiryTask != null) {
            Bukkit.getScheduler().cancelTask(expiryTask);
        }
    }

    @Override
    public void onInitialize(VoicechatServerApi api) {
    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        
        UUID attackerUuid = attacker.getUniqueId();
        
        if (onCooldown(attackerUuid)) {
            sendCooldownMessage(attacker);
            return;
        }
        
        applyCooldown(attackerUuid);
        createPair(attackerUuid, victim.getUniqueId());
    }

    @Override
    public long getBaseCooldown() {
        return 16000;
    }

    @Override
    public String getId() {
        return "voiceswappingstaff";
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
        return ChatColor.LIGHT_PURPLE + "Voice Swapping Staff";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§cSwaps voices between you and the",
            "§ctarget player for 12 seconds on hit.",
            "§cHitting a linked player disrupts their swap.",
            "",
            "§cOn Hit",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.WOODEN_HOE;
    }

    @Override
    public int getCustomModelData() {
        return 101;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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