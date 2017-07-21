package com.matt.forgehax.mods;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.util.ArrayHelper;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.spam.SpamEntry;
import com.matt.forgehax.util.spam.SpamTokens;
import com.matt.forgehax.util.spam.SpamTrigger;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.nio.file.Files;
import java.util.Queue;
import java.util.Scanner;

import static com.matt.forgehax.Helper.getFileManager;
import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class ChatBot extends ToggleMod {
    public final Options<SpamEntry> spams = getCommandStub().builders().<SpamEntry>newOptionsBuilder()
            .name("spam")
            .description("Contents to spam")
            .factory(SpamEntry::new)
            .supplier(Sets::newHashSet)
            .build();

    public final Setting<Integer> max_message_length = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("max_message_length")
            .description("Maximum length allowed for a message")
            .defaultTo(256)
            .min(0)
            .max(256)
            .build();

    public final Setting<Integer> max_input_length = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("max_input_length")
            .description("Maximum chat input length allowed")
            .defaultTo(16)
            .min(0)
            .max(256)
            .build();

    public final Setting<Long> spam_delay = getCommandStub().builders().<Long>newSettingBuilder()
            .name("spam_delay")
            .description("Delay between each message in ms")
            .defaultTo(15000L)
            .changed(cb -> {
                nextSend = 0;
            })
            .build();

    private final Queue<SpamMessage> sendQueue = Queues.newPriorityQueue();

    public ChatBot() {
        super("ChatBot", false, "Spam chat");
    }

    private boolean send(SpamMessage sm) {
        if(!sm.getMessage().isEmpty()
                && sm.getMessage().length() <= max_message_length.get()
                && !sendQueue.contains(sm))
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

                    boolean givenInput = data.hasOption("keyword")
                            || data.hasOption("type")
                            || data.hasOption("trigger")
                            || data.hasOption("enabled");

                    SpamEntry entry = spams.get(name);
                    if (entry == null) {
                        entry = new SpamEntry(name);
                        spams.add(entry);
                        data.write("Added new entry \"" + name + "\"");
                    }

                    if (data.hasOption("keyword")) entry.setKeyword(data.getOptionAsString("keyword"));
                    if (data.hasOption("type")) entry.setType(data.getOptionAsString("type"));
                    if (data.hasOption("trigger")) entry.setTrigger(data.getOptionAsString("trigger"));
                    if (data.hasOption("enabled"))
                        entry.setEnabled(SafeConverter.toBoolean(data.getOptionAsString("enabled")));

                    if (data.getArgumentCount() == 2) {
                        String msg = data.getArgumentAsString(1);
                        entry.add(msg);
                        data.write("Added message \"" + msg + "\"");
                    }

                    if(givenInput) {
                        data.write("keyword=" + entry.getKeyword());
                        data.write("type=" + entry.getType().name());
                        data.write("trigger=" + entry.getTrigger().name());
                        data.write("enabled=" + Boolean.toString(entry.isEnabled()));
                    }

                    data.markSuccess();
                })
                .success(e -> spams.serialize())
                .build();

        getCommandStub().builders().newCommandBuilder()
                .name("import")
                .description("Import a txt or json file")
                .processor(data -> {
                    data.requiredArguments(2);
                    String name = data.getArgumentAsString(0);
                    String fileN = data.getArgumentAsString(1);

                    SpamEntry entry = spams.get(name);
                    if (entry == null) {
                        entry = new SpamEntry(name);
                        spams.add(entry);
                        data.write("Added new entry \"" + name + "\"");
                    }

                    File file = getFileManager().getFileInBaseDirectory(fileN);
                    if(file.exists()) {
                        if(fileN.endsWith(".json")) {
                            try {
                                JsonParser parser = new JsonParser();
                                JsonElement element = parser.parse(new String(Files.readAllBytes(file.toPath())));
                                if(element.isJsonArray()) {
                                    JsonArray head = (JsonArray) element;
                                    int count = 0;
                                    for(JsonElement e : head) {
                                        if(e.isJsonPrimitive()) {
                                            String str = e.getAsString();
                                            entry.add(str);
                                            ++count;
                                        }
                                    }
                                    data.write("Successfully imported " + count + " messages");
                                } else {
                                    data.write("Json head must be a JsonArray");
                                }
                            } catch (Throwable t) {
                                data.write("Failed parsing json: " + t.getMessage());
                            }
                        } else if(fileN.endsWith(".txt")) {
                            try {
                                Scanner scanner = new Scanner(file);
                                int count = 0;
                                while(scanner.hasNextLine()) {
                                    entry.add(scanner.nextLine());
                                    ++count;
                                }
                                data.write("Successfully imported " + count + " messages");
                            } catch (Throwable t) {
                                data.write("Failed parsing text: " + t.getMessage());
                            }
                        } else {
                            data.write("Invalid file extension for \"" + fileN + "\" (requires .txt or .json)");
                        }
                    } else {
                        data.write("Could not find file \"" + fileN + "\" in base directory");
                    }
                })
                .success(e -> spams.serialize())
                .build();

        getCommandStub().builders().newCommandBuilder()
                .name("export")
                .description("")
                .processor(data -> {

                })
                .build();

        getCommandStub().builders().newCommandBuilder()
                .name("remove")
                .description("Remove spam entry")
                .processor(data -> {
                    data.requiredArguments(1);
                    String name = data.getArgumentAsString(0);

                    SpamEntry entry = spams.get(name);
                    if(entry != null) {
                        spams.remove(entry);
                        data.write("Removed entry \"" + name + "\"");
                        data.markSuccess();
                    } else {
                        data.write("Invalid entry \"" + name + "\"");
                        data.markFailed();
                    }
                })
                .success(e -> spams.serialize())
                .build();
    }

    private long nextSend = 0;

    @SubscribeEvent
    public void onTick(LocalPlayerUpdateEvent event) {
        if(System.currentTimeMillis() > nextSend) {
            if(!sendQueue.isEmpty()) {
                getLocalPlayer().sendChatMessage(sendQueue.poll().getMessage());
                nextSend = System.currentTimeMillis() + spam_delay.get();
            } else if(!spams.isEmpty()) {
                for(SpamEntry e : spams) {
                    if(e.isEnabled() && e.getTrigger().equals(SpamTrigger.SPAM)) {
                        send(new SpamMessage(e.next(), PriorityEnum.DEFAULT, "self"));
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(ChatMessageEvent event) {
        if(event.getProfile() == null || event.getProfile().getId().equals(getLocalPlayer().getGameProfile().getId())) return;

        String[] args = event.getMessage().split(" ");
        final String sender = event.getProfile().getId().toString();
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
                            if(!Strings.isNullOrEmpty(arg)
                                    && arg.length() <= max_input_length.get())
                                send(new SpamMessage(SpamTokens.PLAYER_NAME.fill(e.next(), arg), PriorityEnum.HIGH, sender));
                            break;
                        }
                        default: break;
                    }
                });
    }

    @SubscribeEvent
    public void onPlayerConnect(PlayerConnectEvent.Join event) {
        final String player = event.getProfile() != null ? event.getProfile().getName() : "null";
        spams.stream()
                .filter(SpamEntry::isEnabled)
                .forEach(e -> {
                    switch (e.getTrigger()) {
                        case PLAYER_CONNECT:
                        {
                            send(new SpamMessage(SpamTokens.PLAYER_NAME.fill(e.next(), player), PriorityEnum.HIGH));
                            break;
                        }
                        default: break;
                    }
                });
    }

    @SubscribeEvent
    public void onPlayerDisconnect(PlayerConnectEvent.Leave event) {
        final String player = event.getProfile() != null ? event.getProfile().getName() : "null";
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
