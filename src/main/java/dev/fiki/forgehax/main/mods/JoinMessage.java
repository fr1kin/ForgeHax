package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import dev.fiki.forgehax.main.events.ChatMessageEvent;
import dev.fiki.forgehax.main.events.PlayerConnectEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.cmd.settings.LongSetting;
import dev.fiki.forgehax.main.util.cmd.settings.StringSetting;
import dev.fiki.forgehax.main.util.cmd.settings.collections.CustomSettingSet;
import dev.fiki.forgehax.main.util.common.PriorityEnum;
import dev.fiki.forgehax.main.util.entity.PlayerInfo;
import dev.fiki.forgehax.main.util.entity.PlayerInfoHelper;
import dev.fiki.forgehax.main.util.entry.CustomMessageEntry;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.spam.SpamMessage;
import dev.fiki.forgehax.main.util.spam.SpamTokens;
import dev.fiki.forgehax.main.mods.services.SpamService;
import dev.fiki.forgehax.main.util.ArrayHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

import joptsimple.internal.Strings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 7/21/2017 by fr1kin
 */
// TODO: 1.15
//@RegisterMod
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

  public JoinMessage() {
    super(Category.MISC, "JoinMessage", false, "Allows players to add custom join messages");
  }

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

  @SubscribeEvent
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
        < cooldowns.getOrDefault(event.getSender().getId(), new AtomicLong(0L)).get()) {
      debugMessage("Player is currently in a cooldown");
      return;
    }

    if (use_offline.getValue()) {
      // use offline ID
      setJoinMessage(PlayerEntity.getOfflineUUID(target), event.getSender().getId(), message);
      return; // join message set, stop here
    }

    PlayerInfoHelper.registerWithCallback(
        target,
        new FutureCallback<PlayerInfo>() {
          @Override
          public void onSuccess(@Nullable PlayerInfo result) {
            if (result != null && !result.isOfflinePlayer()) {
              setJoinMessage(result.getId(), event.getSender().getId(), message);
            }
          }

          @Override
          public void onFailure(Throwable t) {
          }
        });
  }

  @SubscribeEvent
  public void onPlayerConnect(PlayerConnectEvent.Join event) {
    CustomMessageEntry entry = messages.search(e -> e.getPlayer().equals(event.getPlayerInfo().getId()))
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
