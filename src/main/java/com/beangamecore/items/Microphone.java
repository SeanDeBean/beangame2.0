package com.beangamecore.items;

import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import com.beangamecore.registry.BeangameItemRegistry;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.List;
import java.util.Map;

public class Microphone extends BeangameItem implements BGVoicechat, BGVCMicPacket {
    
    @Override
    public void onMicrophonePacket(MicrophonePacketEvent event) {
        if(event.isCancelled()) return;
        VoicechatServerApi api = event.getVoicechat();
        Float baseDistanace = 48F;
        Player player = (Player)event.getSenderConnection().getPlayer().getPlayer();
        if(player == null){
            return;
        }
        if(BeangameItemRegistry.getFromItemStack(player.getInventory().getItemInMainHand())
                .map(item -> item.is(this.getClass()))
                .orElse(false)) {
            baseDistanace = 300F;
        } else {
            return;
        }
        event.cancel();

        Location playerLocation = player.getLocation();
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
                    .position(api.createPosition(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()))
                    .distance(baseDistanace)
                    .build());
            }
        }
    }

    @Override
    public void onInitialize(VoicechatServerApi api) {

    }

    @Override
    public long getBaseCooldown() {
        return 0;
    }

    @Override
    public String getId() {
        return "microphone";
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
        return ChatColor.GOLD + "Microphone";
    }

    @Override
    public List<String> getLore() {
        return List.of(
            "§aWhen held in main hand, increases",
            "§avoice chat range from 48 to 300",
            "§ablocks. Allows players to hear you",
            "§afrom much further distances.",
            "",
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
        return Material.NETHERITE_SHOVEL;
    }

    @Override
    public int getCustomModelData() {
        return 102;
    }

    @Override
    public List<ItemFlag> getItemFlags() {
        return List.of(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
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
    public EquipmentSlotGroup getSlot(){
        return null;
    }

    @Override
    public int getArmor() {
        return 0;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

}

