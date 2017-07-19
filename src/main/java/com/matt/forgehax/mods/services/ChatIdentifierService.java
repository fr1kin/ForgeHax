package com.matt.forgehax.mods.services;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import joptsimple.internal.Strings;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 7/18/2017 by fr1kin
 */
@RegisterMod
public class ChatIdentifierService extends ServiceMod {
    // should split into two groups: group 1: senders name. group 2: message
    private static final Pattern[] MESSAGE_PATTERNS = {
            Pattern.compile("<(.*?)> (.*)"),
    };

    private static final Pattern[] PRIVATE_MESSAGE_PATTERNS = {
            Pattern.compile("(.*?) whispers: (.*)"),
    };

    public ChatIdentifierService() {
        super("ChatIdentifierService", "Listens to incoming chat messages and identifies the sender");
    }

    private static boolean dispatchChatEvent(String message, Pattern[] patterns, boolean pm) {
        for(Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(message);
            if(matcher.find()) {
                String messageSender = matcher.group(1);
                String messageOnly = matcher.group(2);
                if(!Strings.isNullOrEmpty(messageSender)) {
                    for(NetworkPlayerInfo info : Helper.getLocalPlayer().connection.getPlayerInfoMap()) {
                        if(String.CASE_INSENSITIVE_ORDER.compare(messageSender, info.getGameProfile().getName()) == 0) {
                            MinecraftForge.EVENT_BUS.post(new ChatMessageEvent(info.getGameProfile(), messageOnly, pm));
                            return true; // found match, stop here
                        }
                    }
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onChatMessage(PacketEvent.Incoming.Pre event) {
        if(event.getPacket() instanceof SPacketChat) {
            SPacketChat packet = (SPacketChat) event.getPacket();
            String message = packet.getChatComponent().getUnformattedText();
            if(!Strings.isNullOrEmpty(message)) {
                // try normal messages
                if(!dispatchChatEvent(message, MESSAGE_PATTERNS, false))
                    dispatchChatEvent(message, PRIVATE_MESSAGE_PATTERNS, true); // no normal msg pattern was matched, try pm pattern now

                // if reached here then the message is unrecognized
            }
        }
    }
}
