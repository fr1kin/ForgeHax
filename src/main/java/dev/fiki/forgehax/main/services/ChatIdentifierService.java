package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.entity.PlayerInfo;
import dev.fiki.forgehax.api.entity.PlayerInfoHelper;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.ChatMessageEvent;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import joptsimple.internal.Strings;
import net.minecraft.network.play.server.SChatPacket;

import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.fiki.forgehax.main.Common.getEventBus;
import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod
public class ChatIdentifierService extends ServiceMod {

  // should split into two groups: group 1: senders name. group 2: message
  private static final Pattern[] MESSAGE_PATTERNS = {
      Pattern.compile("<(.*?)> (.*)"), // vanilla
  };

  private static final Pattern[] INCOMING_PRIVATE_MESSAGES = {
      Pattern.compile("(.*?) whispers to you: (.*)"), // vanilla
      Pattern.compile("(.*?) whispers: (.*)"), // 2b2t
  };

  private static final Pattern[] OUTGOING_PRIVATE_MESSAGES = {
      Pattern.compile("[Tt]o (.*?): (.*)"), // 2b2t and vanilla i think
  };

  private static boolean extract(String message, Pattern[] patterns, BiConsumer<PlayerInfo, String> callback) {
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(message);
      if (matcher.find()) {
        final String messageSender = matcher.group(1);
        final String messageOnly = matcher.group(2);
        if (!Strings.isNullOrEmpty(messageSender)) {
          PlayerInfoHelper.getOrCreateByUsername(messageSender)
              .thenAccept(info -> callback.accept(info, messageOnly));
          return true;
        }
      }
    }
    return false;
  }

  @SuppressWarnings("Duplicates")
  @SubscribeListener
  public void onChatMessage(PacketInboundEvent event) {
    if (getLocalPlayer() == null || getLocalPlayer().connection == null) {
      return;
    } else if (event.getPacket() instanceof SChatPacket) {
      SChatPacket packet = (SChatPacket) event.getPacket();
      String message = packet.getMessage().getString();
      if (!Strings.isNullOrEmpty(message)) {
        // normal public messages
        if (extract(message, MESSAGE_PATTERNS,
            (info, msg) -> getEventBus().post(ChatMessageEvent.newPublicChat(info, msg)))) {
          return;
        }

        // private messages to the local player
        if (extract(message, INCOMING_PRIVATE_MESSAGES,
            (info, msg) -> PlayerInfoHelper.getOrCreate(getLocalPlayer().getGameProfile())
                .thenAccept(selfInfo -> getEventBus().post(ChatMessageEvent.newPrivateChat(info, selfInfo, msg))))) {
          return;
        }

        // outgoing pms from local player
        if (extract(message, OUTGOING_PRIVATE_MESSAGES,
            (info, msg) -> PlayerInfoHelper.getOrCreate(getLocalPlayer().getGameProfile())
                .thenAccept(selfInfo -> getEventBus().post(ChatMessageEvent.newPrivateChat(selfInfo, info, msg))))) {
          return;
        }

        log.warn("Unable to process message \"{}\"", message);
        // if reached here then the message is unrecognized
      }
    }
  }
}
