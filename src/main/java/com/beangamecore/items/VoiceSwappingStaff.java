package com.beangamecore.items;

import com.beangamecore.Main;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.damage.entity.BGDDealerHeldI;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import com.beangamecore.util.GlobalCooldowns;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceSwappingStaff extends BeangameItem implements BGVoicechat, BGVCMicPacket, BGDDealerHeldI {

    ConcurrentHashMap<UUID, UUID> toSwap = new ConcurrentHashMap<>();

    @Override
    public void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatServerApi api = event.getVoicechat();
        VoicechatConnection sender = event.getSenderConnection();

        if (sender != null) {
            handleSenderConnection(event, api, sender);
        }
    }

    private void handleSenderConnection(MicrophonePacketEvent event, VoicechatServerApi api,
            VoicechatConnection sender) {
        UUID pSender = sender.getPlayer().getUuid();
        UUID pOther = toSwap.get(pSender);
        if (pOther != null) {
            event.cancel();
            VoicechatConnection other = api.getConnectionOf(pOther);
            sendLocationalSoundToAll(api, event, other, sender);
        }
    }

    private void sendLocationalSoundToAll(VoicechatServerApi api, MicrophonePacketEvent event,
                                      VoicechatConnection other, VoicechatConnection sender) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(sender.getPlayer().getUuid())) {
                // Don't send the swapped audio back to the speaker
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

    public void createPair(UUID p1, UUID p2){
        addPair(p1, p2);
        addPair(p2, p1);
    }

    void addPair(UUID p1, UUID p2){
        toSwap.put(p1, p2);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> toSwap.remove(p1), 800);
    }

    @Override
    public void onInitialize(VoicechatServerApi api) {

    }

    @Override
    public void attackerOnHit(EntityDamageByEntityEvent event, ItemStack item) {
        
        if(event.getEntity() instanceof Player v && event.getDamager() instanceof Player k){
            if (GlobalCooldowns.onCooldown("voiceswappingstaff")){
                GlobalCooldowns.sendCooldownMessage("voiceswappingstaff", (Player) event.getDamager());
                return;
            }
            GlobalCooldowns.setCooldown("voiceswappingstaff", getBaseCooldown());
            createPair(k.getUniqueId(), v.getUniqueId());
        }
    }

    @Override
    public void resetCooldown(UUID player){
        GlobalCooldowns.setCooldown("voiceswappingstaff", -1);
    }

    @Override
    public void setCooldown(UUID player, long millis){
        GlobalCooldowns.setCooldown("voiceswappingstaff", millis);
    }

    @Override
    public long getBaseCooldown() {
        return 60000;
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
        // RecipeAPI r = Main.recipeAPI;
        // return r.bgShapedRecipe(this, "WLM", " SR", "S W", r.eCFromBeangame(Key.bg("walkietalkie")), r.mCFromMaterial(Material.LAPIS_BLOCK), r.mCFromMaterial(Material.MAGENTA_GLAZED_TERRACOTTA), r.mCFromMaterial(Material.STICK), r.mCFromMaterial(Material.REDSTONE_BLOCK));
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
            "§ctarget player for 40 seconds on hit.",
            "§cOnly one swap can be active at a time.",
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

