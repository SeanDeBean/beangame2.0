package com.beangamecore.items.type.voicechat;

import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;

public interface BGVCMicPacket {
    void onMicrophonePacket(MicrophonePacketEvent event);
}

