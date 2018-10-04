package com.matt.forgehax.mods;

import static com.matt.forgehax.util.spam.SpamTokens.MESSAGE;
import static com.matt.forgehax.util.spam.SpamTokens.PLAYER_NAME;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.matt.forgehax.Helper;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.mods.services.SpamService;
import com.matt.forgehax.util.ArrayHelper;
import com.matt.forgehax.util.command.CommandHelper;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.PlayerInfo;
import com.matt.forgehax.util.entity.PlayerInfoHelper;
import com.matt.forgehax.util.entry.CustomMessageEntry;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.spam.SpamMessage;
import com.matt.forgehax.util.spam.SpamTokens;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import joptsimple.internal.Strings;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 7/21/2017 by fr1kin */
@RegisterMod
public class JoinMessage extends ToggleMod {
  private static final SpamTokens[] SPAM_TOKENS = new SpamTokens[] {PLAYER_NAME, MESSAGE};

  private final Options<CustomMessageEntry> messages =
      getCommandStub()
          .builders()
          .<CustomMessageEntry>newOptionsBuilder()
          .name("messages")
          .description("Custom messages")
          .factory(CustomMessageEntry::new)
          .supplier(Sets::newConcurrentHashSet)
          .build();

  private final Setting<String> keyword =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("keyword")
          .description("Keyword for the join message")
          .defaultTo("!joinmessage")
          .build();

  private final Setting<String> format =
      getCommandStub()
          .builders()
          .<String>newSettingBuilder()
          .name("format")
          .description(
              "Join message format (Use {PLAYER_NAME} for the player joining, {MESSAGE} for the set message)")
          .defaultTo("<{PLAYER_NAME}> {MESSAGE}")
          .build();

  private final Setting<Long> delay =
      getCommandStub()
          .builders()
          .<Long>newSettingBuilder()
          .name("delay")
          .description("Delay between each message in ms")
          .defaultTo(15000L)
          .build();

  private final Setting<Integer> message_length =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("message_length")
          .description("Maximum length of a custom message")
          .defaultTo(25)
          .build();

  private final Setting<Boolean> use_offline =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("use_offline")
          .description("Allows non-authenticated player names to be added")
          .defaultTo(false)
          .build();

  private final Setting<Long> set_cooldown =
      getCommandStub()
          .builders()
          .<Long>newSettingBuilder()
          .name("set_cooldown")
          .description("Setting cooldown for individual players in ms")
          .defaultTo(15000L)
          .build();

  private final Setting<Integer> max_player_messages =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("max_player_messages")
          .description("Maximum number of messages per individual player")
          .defaultTo(5)
          .min(1)
          .max(Integer.MAX_VALUE)
          .changed(
              cb -> {
                messages.forEach(e -> e.setSize(cb.getTo()));
                messages.serialize();
              })
          .build();

  private final Setting<Boolean> debug_messages =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("debug_messages")
          .description("Displays messages in chat if a player fails to use the command properly")
          .defaultTo(false)
          .build();

  private final Map<UUID, AtomicLong> cooldowns = Maps.newConcurrentMap();

  public JoinMessage() {
    super(Category.MISC, "JoinMessage", false, "Allows players to add custom join messages");
  }

  private void debugMessage(String str) {
    if (debug_messages.get())
      Helper.printMessageNaked(
          Strings.EMPTY, str, new Style().setItalic(true).setColor(TextFormatting.GRAY));
  }

  private void setJoinMessage(UUID target, UUID setter, String message) {
    CustomMessageEntry entry = messages.get(target);
    if (entry == null) {
      entry = new CustomMessageEntry(target);
      messages.add(entry);
    }

    String replyMessage = "Join message changed.";

    if (!entry.containsEntry(setter)) {
      entry.setSize(max_player_messages.get() - 1); // evict a random message
      replyMessage = "Join message set.";
    }
    entry.addMessage(setter, message); // correct size now

    // set cooldown
    cooldowns
        .computeIfAbsent(setter, s -> new AtomicLong(0L))
        .set(System.currentTimeMillis() + set_cooldown.get());

    messages.serialize();

    SpamService.send(
        new SpamMessage(replyMessage, "JOIN_MESSAGE_REPLY", 0, null, PriorityEnum.HIGHEST));
  }

  @SubscribeEvent
  public void onPlayerChat(ChatMessageEvent event) {
    String[] args = event.getMessage().split(" ");

    if (args.length < 3) return; // not enough arguments

    final String keyword = ArrayHelper.getOrDefault(args, 0, Strings.EMPTY);
    if (!this.keyword.get().equalsIgnoreCase(keyword)) return;

    final String target = ArrayHelper.getOrDefault(args, 1, Strings.EMPTY);
    if (target.length() > PlayerInfoHelper.MAX_NAME_LENGTH) {
      debugMessage("Input name over valid length");
      return;
    }
    if (target.equalsIgnoreCase(event.getSender().getName())) {
      debugMessage("Cannot set own join message");
      return;
    }

    final String message = CommandHelper.join(args, " ", 2, args.length);
    if (Strings.isNullOrEmpty(message)) {
      debugMessage("Invalid message (null or empty)");
      return;
    }
    if (message.length() > message_length.get()) {
      debugMessage("Message over maximum specified by JoinMessage.message_length");
      return;
    }

    // setter is not in cooldown
    if (System.currentTimeMillis()
        < cooldowns.getOrDefault(event.getSender().getId(), new AtomicLong(0L)).get()) {
      debugMessage("Player is currently in a cooldown");
      return;
    }

    if (use_offline.get()) {
      // use offline ID
      setJoinMessage(EntityPlayerSP.getOfflineUUID(target), event.getSender().getId(), message);
      return; // join message set, stop here
    }

    PlayerInfoHelper.registerWithCallback(
        target,
        new FutureCallback<PlayerInfo>() {
          @Override
          public void onSuccess(@Nullable PlayerInfo result) {
            if (result != null && !result.isOfflinePlayer())
              setJoinMessage(result.getId(), event.getSender().getId(), message);
          }

          @Override
          public void onFailure(Throwable t) {}
        });
  }

  @SubscribeEvent
  public void onPlayerConnect(PlayerConnectEvent.Join event) {
    CustomMessageEntry entry = messages.get(event.getPlayerInfo().getId());
    if (entry != null) {
      // resize if needed
      if (entry.getSize() > max_player_messages.get()) entry.setSize(max_player_messages.get());
      SpamService.send(
          new SpamMessage(
              SpamTokens.fillAll(
                  format.get(),
                  SPAM_TOKENS,
                  event.getPlayerInfo().getName(),
                  entry.getRandomMessage()),
              "JOIN_MESSAGE",
              delay.get(),
              null,
              PriorityEnum.HIGH));
    }
  }
}
