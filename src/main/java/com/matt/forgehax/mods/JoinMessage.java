package com.matt.forgehax.mods;

import com.google.common.collect.Sets;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.mods.services.SpamService;
import com.matt.forgehax.util.ArrayHelper;
import com.matt.forgehax.util.command.CommandHelper;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.PlayerIdHelper;
import com.matt.forgehax.util.entry.CustomMessageEntry;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.spam.SpamMessage;
import com.matt.forgehax.util.spam.SpamTokens;
import com.mojang.authlib.GameProfile;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

/**
 * Created on 7/21/2017 by fr1kin
 */
@RegisterMod
public class JoinMessage extends ToggleMod {
    private static final SpamTokens[] SPAM_TOKENS = new SpamTokens[]{SpamTokens.PLAYER_NAME, SpamTokens.MESSAGE};

    private final Options<CustomMessageEntry> messages = getCommandStub().builders().<CustomMessageEntry>newOptionsBuilder()
            .name("messages")
            .description("Custom messages")
            .factory(CustomMessageEntry::new)
            .supplier(Sets::newConcurrentHashSet)
            .build();

    private final Setting<String> keyword = getCommandStub().builders().<String>newSettingBuilder()
            .name("keyword")
            .description("Keyword for the join message")
            .defaultTo("!joinmessage")
            .build();

    private final Setting<String> format = getCommandStub().builders().<String>newSettingBuilder()
            .name("format")
            .description("Join message format")
            .defaultTo("<{PLAYER_NAME}> {MESSAGE}")
            .build();

    private final Setting<Long> delay = getCommandStub().builders().<Long>newSettingBuilder()
            .name("delay")
            .description("Delay between each message in ms")
            .defaultTo(15000L)
            .build();

    private final Setting<Integer> message_length = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("message_length")
            .description("Maximum length of a custom message")
            .defaultTo(25)
            .build();

    public JoinMessage() {
        super("JoinMessage", false, "Allows players to add custom join messages");
    }

    @SubscribeEvent
    public void onPlayerChat(ChatMessageEvent event) {
        String[] args = event.getMessage().split(" ");

        if(args.length < 3) return; // not enough arguments

        final String keyword = ArrayHelper.getOrDefault(args, 0, Strings.EMPTY);
        if(!this.keyword.get().equalsIgnoreCase(keyword)) return;

        final String player = ArrayHelper.getOrDefault(args, 1, Strings.EMPTY);
        if(player.length() > 16) return; // length over valid player name
        if(player.equalsIgnoreCase(event.getProfile().getName())) return;

        final String message = CommandHelper.join(args, " ", 2, args.length);
        if(Strings.isNullOrEmpty(message)) return; // invalid message
        if(message.length() > message_length.get()) return; // message too long

        final GameProfile profile = event.getProfile();
        if(profile == null) return;

        new Thread(() -> {
            UUID uuid = PlayerIdHelper.getIdEfficiently(player);
            if(uuid != null) {
                CustomMessageEntry entry = messages.get(player);
                if(entry == null) {
                    entry = new CustomMessageEntry(player);
                    entry.setSetterId(profile.getName());
                    entry.setMessage(message);
                    messages.add(entry);
                } else {
                    entry.setSetterId(profile.getName());
                    entry.setMessage(message);
                }

                messages.serialize();

                SpamService.send(new SpamMessage(
                        "Join message set",
                        "JOIN_MESSAGE_REPLY",
                        2500,
                        null,
                        PriorityEnum.HIGHEST
                ));
            }
        }).start();
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent.Join event) {
        CustomMessageEntry entry = messages.get(event.getProfile().getName());
        if(entry != null) {
            SpamService.send(new SpamMessage(
                    SpamTokens.fillAll(format.get(), SPAM_TOKENS, event.getProfile().getName(), entry.getMessage()),
                    "JOIN_MESSAGE",
                    delay.get(),
                    null,
                    PriorityEnum.HIGH
            ));
        }
    }
}
