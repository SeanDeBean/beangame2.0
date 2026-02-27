package com.beangamecore.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerInvI;
import com.beangamecore.items.type.talisman.BGInvUnstackable;
import com.beangamecore.items.type.talisman.BGMPTalismanI;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import com.beangamecore.util.Cooldowns;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.md_5.bungee.api.ChatColor;

public class SealOfTheSchizophrenic extends BeangameItem implements BGVoicechat, BGVCMicPacket, BGMPTalismanI, BGInvUnstackable, BGDDealerInvI {

    @Override
    public void attackerInventoryOnHit(EntityDamageByEntityEvent event, ItemStack item){
        LivingEntity entity = (LivingEntity) event.getEntity();
        if(entity instanceof Player){
            UUID uuid = entity.getUniqueId();
            Cooldowns.setCooldown("schizophrenic", uuid, 12000L);
        }
    }

    @Override
    public void applyTalismanEffects(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        Cooldowns.setCooldown("schizophrenic", uuid, 2000L);
    }

    @Override
    public void onMicrophonePacket(MicrophonePacketEvent event) {
        // if cancelled, nothing
        if (event.isCancelled())
            return;

        VoicechatServerApi api = event.getVoicechat();
        Float baseDistance = 48F;

        Player player = getSpectatorPlayer(event);
        if (player == null) {
            return;
        }

        Location playerLocation = player.getLocation();

        for (Player recipient : Bukkit.getOnlinePlayers()) {
            processRecipient(api, baseDistance, player, playerLocation, recipient, event);
        }
    }

    private Player getSpectatorPlayer(MicrophonePacketEvent event) {
        // get who sent the packet
        Player player = (Player) event.getSenderConnection().getPlayer().getPlayer();
        if (player == null || player.getGameMode() != GameMode.SPECTATOR) { // confirm that only spectators can send
                                                                            // these packets
            return null;
        }
        return player;
    }

    private void processRecipient(VoicechatServerApi api, Float baseDistance, Player player, Location playerLocation,
            Player recipient, MicrophonePacketEvent event) {
        UUID recipientUuid = recipient.getUniqueId();
        if (Cooldowns.onCooldown("schizophrenic", recipientUuid)) {
            // custom packets

            if (shouldSkipRecipient(player, recipient)) {
                return;
            }

            VoicechatConnection connection = getValidConnection(api, recipient, player);
            if (connection != null) {
                sendLocationalSoundPacket(api, connection, event, playerLocation, baseDistance);
            }
        }
    }

    private boolean shouldSkipRecipient(Player player, Player recipient) {
        // skip sending to self
        if (recipient.equals(player)) {
            return true;
        }

        // ensure they are in the same world
        if (!recipient.getWorld().equals(player.getWorld())) {
            return true;
        }

        return false;
    }

    private VoicechatConnection getValidConnection(VoicechatServerApi api, Player recipient, Player player) {
        // get the recipients voice chat connection
        return api.getConnectionOf(recipient.getUniqueId());
    }

    private void sendLocationalSoundPacket(VoicechatServerApi api, VoicechatConnection connection,
            MicrophonePacketEvent event, Location playerLocation, Float baseDistance) {
        api.sendLocationalSoundPacketTo(
                connection,
                event.getPacket().locationalSoundPacketBuilder()
                        .position(
                                api.createPosition(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()))
                        .distance(baseDistance)
                        .build());
    }

    @Override
    public void onInitialize(VoicechatServerApi api) {

    }

    @Override
    public long getBaseCooldown() {
        return 2000;
    }

    @Override
    public String getId() {
        return "sealoftheschizophrenic";
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
        return ChatColor.GRAY + "Seal Of The Schizophrenic";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§3Melee hits make players hear",
            "§3spectator voice chat for 6 seconds.",
            "§3Grants the carrier the permanent",
            "§3ability to hear nearby spectators.",
            "",
            "§cOn Hit",
            "§3Talisman",
            "§9§obeangame"
        );
    }

    @Override
    public Map<String, Integer> getEnchantments() {
        return Map.of();
    }

    @Override
    public Material getMaterial() {
        return Material.SUGAR;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_UNBREAKABLE);
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
