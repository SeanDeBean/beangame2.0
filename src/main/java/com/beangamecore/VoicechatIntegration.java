package com.beangamecore;

import com.beangamecore.commands.MuteCommand;
import com.beangamecore.items.generic.BeangameItem;
import com.beangamecore.items.type.voicechat.BGVCLeaveGroup;
import com.beangamecore.items.type.voicechat.BGVCMicPacket;
import com.beangamecore.items.type.voicechat.BGVoicechat;
import com.beangamecore.registry.BeangameItemRegistry;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LeaveGroupEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class VoicechatIntegration implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "beangame";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if(!(api instanceof VoicechatServerApi serverApi)){
            throw new IllegalStateException("Voicechat API is not a server API!");
        }
        for(BeangameItem i : BeangameItemRegistry.collection()){
            if (i instanceof BGVoicechat v) v.onInitialize(serverApi);
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        Collection<BeangameItem> items = BeangameItemRegistry.collection();
        registration.registerEvent(MicrophonePacketEvent.class, this::mute);
        for (BeangameItem i : items) {
            if(i instanceof BGVCLeaveGroup g){
                registration.registerEvent(LeaveGroupEvent.class, g::onLeaveGroup);
            }
            if(i instanceof BGVCMicPacket p){
                registration.registerEvent(MicrophonePacketEvent.class, p::onMicrophonePacket);
            }
        }
    }

    void mute(MicrophonePacketEvent event) {
        if(!MuteCommand.muted){
            return;
        }
        VoicechatConnection sender = event.getSenderConnection();
        if(sender == null || sender.getPlayer() == null){
            return;
        }
        Player player = (Player) sender.getPlayer().getPlayer();
        if(player == null || player.hasPermission("bg.use")){
            return;
        }
        event.cancel();
        player.spigot().sendMessage(
            ChatMessageType.ACTION_BAR, 
            TextComponent.fromLegacy(ChatColor.WHITE + "Your voice chat is muted!")
        );
    }

}

