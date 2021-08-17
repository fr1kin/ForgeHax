package dev.fiki.forgehax.main.mods.chat;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.ArrayHelper;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.cmd.settings.LongSetting;
import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.cmd.settings.collections.CustomSettingSet;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.entity.PlayerInfoHelper;
import dev.fiki.forgehax.api.entry.CustomMessageEntry;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.ChatMessageEvent;
import dev.fiki.forgehax.api.events.PlayerConnectEvent;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.spam.SpamMessage;
import dev.fiki.forgehax.api.spam.SpamTokens;
import dev.fiki.forgehax.main.services.SpamService;
import joptsimple.internal.Strings;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

// TODO: 1.15
//@RegisterMod(
//    name = "JoinMessage",
//    description = "Allows players to add custom join messages",
//    category = Category.CHAT
//)
public class JoinMessage extends ToggleMod {

  private static final SpamTokens[] SPAM_TOKENS = new SpamTokens[]{SpamTokens.PLAYER_NAME, SpamTokens.MESSAGE};

  private final CustomSettingSet<CustomMessageEntry> messages = newCustomSettingSet(CustomMessageEntry.class)
      .name("messages")
      .description("Custom messages")
      .valueSupplier(CustomMessageEntry::new)
      .supplier(Sets::newConcurrentHashSet)
      .build();

  private final StringSetting keyword = newStringSetting()
      .name("keyword")
      .description("Keyword for the join message")
      .defaultTo("!joinmessage")
      .build();

  private final StringSetting format = newStringSetting()
      .name("format")
      .description("Join message format (Use {PLAYER_NAME} for the player joining, {MESSAGE} for the set message)")
      .defaultTo("<{PLAYER_NAME}> {MESSAGE}")
      .build();

  private final LongSetting delay = newLongSetting()
      .name("delay")
      .description("Delay between each message in ms")
      .defaultTo(15000L)
      .build();

  private final IntegerSetting message_length = newIntegerSetting()
      .name("message_length")
      .description("Maximum length of a custom message")
      .defaultTo(25)
      .build();

  private final BooleanSetting use_offline = newBooleanSetting()
      .name("use_offline")
      .description("Allows non-authenticated player names to be added")
      .defaultTo(false)
      .build();

  private final LongSetting set_cooldown = newLongSetting()
      .name("set_cooldown")
      .description("Setting cooldown for individual players in ms")
      .defaultTo(15000L)
      .build();

  private final IntegerSetting max_player_messages = newIntegerSetting()
      .name("max_player_messages")
      .description("Maximum number of messages per individual player")
      .defaultTo(5)
      .min(1)
      .max(Integer.MAX_VALUE)
      .changedListener((from, to) -> {
        messages.forEach(e -> e.setSize(to));
        messages.serialize();
      })
      .build();

  private final BooleanSetting debug_messages = newBooleanSetting()
      .name("debug_messages")
      .description("Displays messages in chat if a player fails to use the command properly")
      .defaultTo(false)
      .build();

  private final Map<UUID, AtomicLong> cooldowns = Maps.newConcurrentMap();

  private void debugMessage(String str) {
    if (debug_messages.getValue()) {
      //Helper.printMessageNaked(Strings.EMPTY, str, new Style().setItalic(true).setColor(TextFormatting.GRAY));
    }
  }

  private void setJoinMessage(UUID target, UUID setter, String message) {
    CustomMessageEntry entry = messages.search(e -> e.getPlayer().equals(target)).orElse(null);
    if (entry == null) {
      entry = new CustomMessageEntry();
      entry.setPlayer(target);
      messages.add(entry);
    }

    String replyMessage = "Join message changed.";

    if (!entry.containsEntry(setter)) {
      entry.setSize(max_player_messages.getValue() - 1); // evict a random message
      replyMessage = "Join message set.";
    }
    entry.addMessage(setter, message); // correct size now

    // set cooldown
    cooldowns
        .computeIfAbsent(setter, s -> new AtomicLong(0L))
        .set(System.currentTimeMillis() + set_cooldown.getValue());

    messages.serialize();

    SpamService.send(
        new SpamMessage(replyMessage, "JOIN_MESSAGE_REPLY", 0, null, PriorityEnum.HIGHEST));
  }

  @SubscribeListener
  public void onPlayerChat(ChatMessageEvent event) {
    String[] args = event.getMessage().split(" ");

    if (args.length < 3) {
      return; // not enough arguments
    }

    final String keyword = ArrayHelper.getOrDefault(args, 0, Strings.EMPTY);
    if (!this.keyword.getValue().equalsIgnoreCase(keyword)) {
      return;
    }

    final String target = ArrayHelper.getOrDefault(args, 1, Strings.EMPTY);
    if (target.length() > PlayerInfoHelper.MAX_NAME_LENGTH) {
      debugMessage("Input name over valid length");
      return;
    }
    if (target.equalsIgnoreCase(event.getSender().getName())) {
      debugMessage("Cannot set own join message");
      return;
    }

    final String message = String.join(" ", args);
    if (Strings.isNullOrEmpty(message)) {
      debugMessage("Invalid message (null or empty)");
      return;
    }
    if (message.length() > message_length.getValue()) {
      debugMessage("Message over maximum specified by JoinMessage.message_length");
      return;
    }

    // setter is not in cooldown
    if (System.currentTimeMillis()
        < cooldowns.getOrDefault(event.getSender().getUuid(), new AtomicLong(0L)).get()) {
      debugMessage("Player is currently in a cooldown");
      return;
    }

    if (use_offline.getValue()) {
      // use offline ID
      setJoinMessage(PlayerEntity.createPlayerUUID(target), event.getSender().getUuid(), message);
      return; // join message set, stop here
    }

    PlayerInfoHelper.getOrCreateByUsername(target)
        .exceptionally(ex -> PlayerInfoHelper.getOrCreateOffline(target)
            .getNow(null))
        .thenAccept(info -> {
          if (info != null && !info.isOfflinePlayer()) {
            setJoinMessage(info.getUuid(), event.getSender().getUuid(), message);
          }
        });
  }

  @SubscribeListener
  public void onPlayerConnect(PlayerConnectEvent.Join event) {
    CustomMessageEntry entry = messages.search(e -> e.getPlayer().equals(event.getPlayerInfo().getUuid()))
        .orElse(null);
    if (entry != null) {
      // resize if needed
      if (entry.getSize() > max_player_messages.getValue()) {
        entry.setSize(max_player_messages.getValue());
      }
      SpamService.send(
          new SpamMessage(
              SpamTokens.fillAll(
                  format.getValue(),
                  SPAM_TOKENS,
                  event.getPlayerInfo().getName(),
                  entry.getRandomMessage()),
              "JOIN_MESSAGE",
              delay.getValue(),
              null,
              PriorityEnum.HIGH));
    }
  }
}
