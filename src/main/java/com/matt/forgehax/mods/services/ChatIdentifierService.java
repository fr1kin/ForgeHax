package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.google.common.util.concurrent.FutureCallback;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.util.entity.PlayerInfo;
import com.matt.forgehax.util.entity.PlayerInfoHelper;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import joptsimple.internal.Strings;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 7/18/2017 by fr1kin
 */
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

  public ChatIdentifierService() {
    super("ChatIdentifierService", "Listens to incoming chat messages and identifies the sender");
  }

  private static boolean extract(
    String message, Pattern[] patterns, BiConsumer<GameProfile, String> callback) {
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(message);
      if (matcher.find()) {
        final String messageSender = matcher.group(1);
        final String messageOnly = matcher.group(2);
        if (!Strings.isNullOrEmpty(messageSender)) {
          for (NetworkPlayerInfo data : getLocalPlayer().connection.getPlayerInfoMap()) {
            if (
              String.CASE_INSENSITIVE_ORDER.compare(messageSender, data.getGameProfile().getName())
                == 0) {
              callback.accept(data.getGameProfile(), messageOnly);
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @SuppressWarnings("Duplicates")
  @SubscribeEvent
  public void onChatMessage(PacketEvent.Incoming.Pre event) {
    if (getLocalPlayer() == null || getLocalPlayer().connection == null) {
      return;
    } else if (event.getPacket() instanceof SPacketChat) {
      SPacketChat packet = (SPacketChat) event.getPacket();
      String message = packet.getChatComponent().getUnformattedText();
      if (!Strings.isNullOrEmpty(message)) {
        MC.addScheduledTask(() -> {
          // normal public messages
          if (extract(
            message,
            MESSAGE_PATTERNS,
            (senderProfile, msg) -> {
              PlayerInfoHelper.registerWithCallback(
                senderProfile.getName(),
                new FutureCallback<PlayerInfo>() {
                  @Override
                  public void onSuccess(@Nullable PlayerInfo result) {
                    if (result != null) {
                      MinecraftForge.EVENT_BUS
                        .post(ChatMessageEvent.newPublicChat(result, msg));
                    }
                  }
        
                  @Override
                  public void onFailure(Throwable t) {
                    PlayerInfoHelper.generateOfflineWithCallback(senderProfile.getName(), this);
                  }
                });
            })) {
            return;
          }

          // private messages to the local player
          if (extract(
            message,
            INCOMING_PRIVATE_MESSAGES,
            (senderProfile, msg) -> {
              PlayerInfoHelper.registerWithCallback(
                senderProfile.getName(),
                new FutureCallback<PlayerInfo>() {
                  @Override
                  public void onSuccess(final @Nullable PlayerInfo sender) {
                    // now get the local player
                    if (sender != null) {
                      PlayerInfoHelper.registerWithCallback(
                        getLocalPlayer().getName(),
                        new FutureCallback<PlayerInfo>() {
                          @Override
                          public void onSuccess(@Nullable PlayerInfo result) {
                            if (result != null) {
                              MinecraftForge.EVENT_BUS.post(
                                ChatMessageEvent.newPrivateChat(sender, result, msg));
                            }
                          }
                
                          @Override
                          public void onFailure(Throwable t) {
                            PlayerInfoHelper.generateOfflineWithCallback(
                              getLocalPlayer().getName(), this);
                          }
                        });
                    }
                  }
        
                  @Override
                  public void onFailure(Throwable t) {
                    PlayerInfoHelper.generateOfflineWithCallback(senderProfile.getName(), this);
                  }
                });
            })) {
            return;
          }

          // outgoing pms from local player
          if (extract(
            message,
            OUTGOING_PRIVATE_MESSAGES,
            (receiverProfile, msg) -> {
              PlayerInfoHelper.registerWithCallback(
                receiverProfile.getName(),
                new FutureCallback<PlayerInfo>() {
                  @Override
                  public void onSuccess(final @Nullable PlayerInfo receiver) {
                    // now get the local player
                    if (receiver != null) {
                      PlayerInfoHelper.registerWithCallback(
                        getLocalPlayer().getName(),
                        new FutureCallback<PlayerInfo>() {
                          @Override
                          public void onSuccess(@Nullable PlayerInfo sender) {
                            if (sender != null) {
                              MinecraftForge.EVENT_BUS.post(
                                ChatMessageEvent.newPrivateChat(sender, receiver, msg));
                            }
                          }
                
                          @Override
                          public void onFailure(Throwable t) {
                            PlayerInfoHelper.generateOfflineWithCallback(
                              getLocalPlayer().getName(), this);
                          }
                        });
                    }
                  }
        
                  @Override
                  public void onFailure(Throwable t) {
                    PlayerInfoHelper
                      .generateOfflineWithCallback(receiverProfile.getName(), this);
                  }
                });
            })) {
            return;
          }

          // if reached here then the message is unrecognized
        });
      }
    }
  }
}
