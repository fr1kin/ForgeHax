package com.matt.forgehax.mods;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.util.ArrayHelper;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.spam.SpamEntry;
import com.matt.forgehax.util.spam.SpamTokens;
import com.matt.forgehax.util.spam.SpamTrigger;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class ChatSpammerMod extends ToggleMod {
    public final Options<SpamEntry> spams = getCommandStub().builders().<SpamEntry>newOptionsBuilder()
            .name("spam")
            .description("Contents to spam")
            .factory(SpamEntry::new)
            .supplier(Sets::newHashSet)
            .build();

    private final Queue<SpamMessage> sendQueue = Queues.newPriorityQueue();

    public ChatSpammerMod() {
        super("ChatSpammer", false, "Spam chat");
    }

    private boolean send(SpamMessage sm) {
        if(!sendQueue.contains(sm))
            return sendQueue.add(sm);
        else
            return false;
    }

    @Override
    protected void onLoad() {
        getCommandStub().builders().newCommandBuilder()
                .name("add")
                .description("Add new spam list")
                .options(parser -> {
                    parser.accepts("keyword", "Message activation keyword").withRequiredArg();
                    parser.accepts("type", "Spam type (random, sequential)").withRequiredArg();
                    parser.accepts("trigger", "How the spam will be triggered (spam, reply, reply_with_input, player_connect, player_disconnect)").withRequiredArg();
                    parser.accepts("enabled", "Enabled").withRequiredArg();
                })
                .processor(data -> {
                    data.requiredArguments(1);
                    String name = data.getArgumentAsString(0);

                    SpamEntry entry = spams.get(name);
                    if(entry == null) {
                        entry = new SpamEntry(name);
                        spams.add(entry);
                        data.write("Added new entry \"" + name + "\"");
                    }

                    if(data.hasOption("keyword")) entry.setKeyword(data.getOptionAsString("keyword"));
                    if(data.hasOption("type")) entry.setType(data.getOptionAsString("type"));
                    if(data.hasOption("trigger")) entry.setTrigger(data.getOptionAsString("trigger"));
                    if(data.hasOption("enabled")) entry.setEnabled(SafeConverter.toBoolean(data.getOptionAsString("enabled")));

                    if(data.getArgumentCount() == 2) {
                        String msg = data.getArgumentAsString(1);
                        entry.add(msg);
                        data.write("Added message \"" + msg + "\"");
                    }

                    data.write("keyword=" + entry.getKeyword());
                    data.write("type=" + entry.getType().name());
                    data.write("trigger=" + entry.getTrigger().name());
                    data.write("enabled=" + Boolean.toString(entry.isEnabled()));

                    spams.serialize();
                })
                .build();
    }

    private long nextSend = 0;

    @SubscribeEvent
    public void onTick(LocalPlayerUpdateEvent event) {
        if(!sendQueue.isEmpty()) {
            getLocalPlayer().sendChatMessage(sendQueue.poll().getMessage());
        } else if(nextSend > System.currentTimeMillis()) {
            for(SpamEntry e : spams) {
                if(e.isEnabled() && e.getTrigger().equals(SpamTrigger.SPAM)) {
                    send(new SpamMessage(e.next(), PriorityEnum.DEFAULT));
                    nextSend = System.currentTimeMillis() + 10000;
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(ChatMessageEvent event) {
        if(event.getProfile() != null && event.getProfile().getId().equals(getLocalPlayer().getGameProfile().getId())) return;

        String[] args = event.getMessage().split(" ");
        final String sender = event.getProfile() != null ? event.getProfile().getId().toString() : "null";
        final String keyword = ArrayHelper.getOrDefault(args, 0, Strings.EMPTY);
        final String arg = ArrayHelper.getOrDefault(args, 1, Strings.EMPTY);
        spams.stream()
                .filter(SpamEntry::isEnabled)
                .filter(e -> e.getKeyword().equalsIgnoreCase(keyword))
                .forEach(e -> {
                    switch (e.getTrigger()) {
                        case REPLY: {
                            send(new SpamMessage(e.next(), PriorityEnum.HIGH, sender));
                            break;
                        }
                        case REPLY_WITH_INPUT: {
                            if(!Strings.isNullOrEmpty(arg)) send(new SpamMessage(SpamTokens.PLAYER_NAME.fill(e.next(), arg), PriorityEnum.HIGH, sender));
                            break;
                        }
                        default:
                            break;
                    }
                });
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent.Join event) {
        final String player = event.getProfile() != null ? event.getProfile().getId().toString() : "null";
        spams.stream()
                .filter(SpamEntry::isEnabled)
                .forEach(e -> {
                    switch (e.getTrigger()) {
                        case PLAYER_CONNECT:
                        {
                            send(new SpamMessage(SpamTokens.PLAYER_NAME.fill(e.next(), player), PriorityEnum.HIGH));
                            break;
                        }
                        default:
                            break;
                    }
                });
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerConnectEvent.Leave event) {
        final String player = event.getProfile() != null ? event.getProfile().getId().toString() : "null";
        spams.stream()
                .filter(SpamEntry::isEnabled)
                .forEach(e -> {
                    switch (e.getTrigger()) {
                        case PLAYER_DISCONNECT:
                        {
                            send(new SpamMessage(SpamTokens.PLAYER_NAME.fill(e.next(), player), PriorityEnum.HIGH));
                            break;
                        }
                        default:
                            break;
                    }
                });
    }

    private static class SpamMessage implements Comparable<SpamMessage> {
        private final String message;
        private final PriorityEnum priority;
        private final String activator;

        public SpamMessage(String message, PriorityEnum priority, String activator) {
            this.message = message;
            this.priority = priority;
            this.activator = activator;
        }

        public SpamMessage(String message, PriorityEnum priority) {
            this(message, priority, null);
        }

        public String getMessage() {
            return message;
        }

        @Override
        public int compareTo(SpamMessage o) {
            return priority.compareTo(o.priority);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof SpamMessage && activator != null && activator.equals(((SpamMessage) obj).activator));
        }

        @Override
        public int hashCode() {
            return activator.hashCode();
        }
    }
}
