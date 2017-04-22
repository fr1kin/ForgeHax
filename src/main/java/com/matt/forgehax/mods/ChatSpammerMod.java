package com.matt.forgehax.mods;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.matt.forgehax.asm.events.PacketEvent;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    warning: im warning you now don't look below
 */

public class ChatSpammerMod extends ToggleMod {
    private static final Map<String, Object> EMPTY_MAP = Collections.unmodifiableMap(Maps.newHashMap());
    private static final String EMPTY_SENDER = String.valueOf("");

    private static final int CHAT_MAX_SIZE = 256;

    private static final String SPAM_FILE_JSON_NAME = "spam.json";

    private static final String PLAYER_LOOKUP_TOKEN = "\\{PLAYER_NAME}";

    private static Map<Character, Integer> getCharacterCount(String msg) {
        Map<Character, Integer> output = Maps.newHashMap();
        for(Character character : msg.toCharArray()) {
            Integer count = output.get(character);
            if(Objects.isNull(count)) {
                output.put(character, 1);
            } else {
                output.replace(character, count + 1);
            }
        }
        return output;
    }

    private static class CommonFunctions {
        static String randomlySelectFrom(List<String> messages, Map<String,Object> values) throws Exception {
            return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        }

        static String randomlySelectFromAndReplaceToken(List<String> messages, Map<String,Object> values) throws Exception {
            String username = (String)values.getOrDefault("username", "");
            return randomlySelectFrom(messages, values).replaceAll(PLAYER_LOOKUP_TOKEN, username);
        }
    }

    public enum Commands {
        DEFAULT("default", CommonFunctions::randomlySelectFromAndReplaceToken),
        PLAYER_JOIN("playerjoin", CommonFunctions::randomlySelectFromAndReplaceToken),
        PLAYER_LEAVE("playerleave", CommonFunctions::randomlySelectFromAndReplaceToken),
        ASK_AUTISM("!askautism", CommonFunctions::randomlySelectFromAndReplaceToken),
        INSULT("!insult", (messages, args) -> {
            String insultedPlayer = ((String)args.getOrDefault("message", "")).split(" ")[0];
            if (!Strings.isNullOrEmpty(insultedPlayer)) {
                Map<String, Object> output = Maps.newHashMap();
                output.put("username", insultedPlayer);
                return CommonFunctions.randomlySelectFromAndReplaceToken(messages, output);
            } else return null;
        })
        ;

        private final String keyword;
        private final SpamFunction function;
        public final List<String> messages = Lists.newArrayList();

        public Property enabled = null;

        Commands(String keyword, SpamFunction function) {
            this.keyword = keyword;
            this.function = function;
        }

        public boolean isEnabled() {
            return enabled.getBoolean();
        }

        public String getKeyword() {
            return keyword;
        }

        public String execute(Map<String, Object> values) {
            try {
                Objects.requireNonNull(values);
                if(!messages.isEmpty()) return function.apply(messages, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public boolean isAction() {
            return keyword.charAt(0) == '!';
        }
    }

    private final Queue<SpamMessage> queue = Queues.newPriorityQueue();

    public Property delay;
    public Property defaultSpammerDelay;
    public Property playerCooldownTime;
    public Property uniquenessThreshold;
    public Property charactersPerMinute;
    public Property maxInputLength;

    private long timeLastMessageSent = -1;
    private long timeWhenToReportJoinEvents = -1;

    private String errorMessage = "";

    public ChatSpammerMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    private boolean addToMessageQueue(String sender, Commands command, SpamPriority priority, Map<String, Object> values) {
        String msg = command.execute(values);
        if(!Strings.isNullOrEmpty(msg) && command.isEnabled() && msg.length() <= CHAT_MAX_SIZE) {
            return queue.add(new SpamMessage(sender, msg, priority));
        } else return false;
    }

    private boolean isUsersMessageInQueue(String sender) {
        for(SpamMessage message : queue)
            if(message.getSender().equals(sender))
                return true;
        return false;
    }

    private void updateLastMessageSentTime() {
        timeLastMessageSent = System.currentTimeMillis();
    }

    private void readJson() {
        File json = new File(MOD.getBaseDirectory(), SPAM_FILE_JSON_NAME);
        if(json.exists()) {
            try {
                JsonParser parser = new JsonParser();
                JsonReader reader = new JsonReader(new FileReader(json));
                JsonObject root = parser.parse(reader).getAsJsonObject();
                for(Commands command : Commands.values()) {
                    command.messages.clear();
                    try {
                        JsonArray msgs = root.get(command.getKeyword()).getAsJsonArray();
                        for(JsonElement element : msgs) {
                            command.messages.add(element.getAsString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = ",err";
                return;
            }
        } else {
            final JsonObject root = new JsonObject();
            for(Commands command : Commands.values()) root.add(command.getKeyword(), new JsonArray());
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Files.write(json.toPath(), gson.toJson(root).getBytes(), StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        errorMessage = "";
    }

    private long estimateTimeToType(String message) {
        // distance/rate = time
        double distance = message.length();
        double rate = charactersPerMinute.getInt();
        // time * 60 (to seconds) * 1000 (to ms)
        return (long)((distance/rate) * 60000);
    }

    private long getTimeDelay(String message) {
        return delay.getLong() == -1 ? estimateTimeToType(message) : delay.getLong();
    }

    private long getTimeDelay() {
        return getTimeDelay(!queue.isEmpty() ? queue.peek().getMessage() : "");
    }

    private boolean isUniqueMessage(String msg) {
        int threshold = uniquenessThreshold.getInt();
        if(threshold > 0) {
            for(Map.Entry<Character, Integer> entry : getCharacterCount(msg).entrySet()) {
                if(entry.getValue() >= threshold) return false;
            }
        }
        return true;
    }

    private boolean isWithinInputLimits(String msg) {
        int len = maxInputLength.getInt();
        return len == 0 || msg.length() <= maxInputLength.getInt();
    }

    private boolean isLocalPlayer(String username) {
        return Objects.nonNull(WRAPPER.getLocalPlayer()) && WRAPPER.getLocalPlayer().getDisplayName().getUnformattedText().equals(username);
    }

    private boolean isLocalPlayer(GameProfile profile) {
        return Objects.nonNull(profile) && isLocalPlayer(profile.getName());
    }

    private String getNameFromComponent(GameProfile profile) {
        return Objects.nonNull(profile) ? profile.getName() : "";
    }

    @Override
    public void onEnabled() {
        queue.clear();
        timeLastMessageSent = -1;
        readJson();
    }

    @Override
    public void onDisabled() {
        queue.clear();
    }

    @Override
    public void onConfigUpdated(List<Property> changed) {
        PlayerCooldownChecker.setCooldownPeriod(playerCooldownTime.getLong());
    }

    @Override
    public void loadConfig(Configuration configuration) {

        addSettings(
                delay = configuration.get(getModName(),
                        "delay",
                        5000,
                        "Delay between messages in ms (set to -1 to use CPM delay)"
                ),
                defaultSpammerDelay = configuration.get(getModName(),
                        "default_spammer_delay",
                        5000,
                        "Default spammer delay"
                ),
                playerCooldownTime = configuration.get(getModName(),
                        "player_cooldown_time",
                        1000,
                        "Time a player has to wait until he/she can use the action spam command again"
                ),
                uniquenessThreshold = configuration.get(getModName(),
                        "uniqueness_threshold",
                        10,
                        "Max number of different characters allowed before being counted as spam"
                ),
                charactersPerMinute = configuration.get(getModName(),
                        "chars_per_minute",
                        400,
                        "Number of characters a minute the automated delay should use"
                ),
                maxInputLength = configuration.get(getModName(),
                        "max_input_length",
                        100,
                        "Maximum number of characters a player can input"
                )
        );
        Property[] properties = new Property[Commands.values().length];
        for(int i = 0; i < properties.length; i++) {
            Commands command = Commands.values()[i];
            properties[i] = (command.enabled = configuration.get(
                    getModName(),
                    "Enable " + command.getKeyword(),
                    true,
                    "Enables " + command.getKeyword()
            ));
        }
        addSettings(properties);
        PlayerCooldownChecker.setCooldownPeriod(playerCooldownTime.getLong());
    }

    @Override
    public String getDisplayText() {
        return super.getDisplayText() + String.format(" (t:%.1fs,q:%d%s)",
                MathHelper.clamp((((double)(timeLastMessageSent + getTimeDelay()) - System.currentTimeMillis()) / 1000D), 0, 999),
                queue.size(), errorMessage
        );
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        queue.clear();
        timeWhenToReportJoinEvents = System.currentTimeMillis() + 5000;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                break;
            case END:
            {
                if(Objects.nonNull(WRAPPER.getLocalPlayer()) && System.currentTimeMillis() >= timeLastMessageSent + getTimeDelay()) {
                    if(!queue.isEmpty()) {
                        WRAPPER.getLocalPlayer().sendChatMessage(queue.poll().getMessage());
                        updateLastMessageSentTime();
                    } else if(System.currentTimeMillis() >= timeLastMessageSent + defaultSpammerDelay.getLong()) {
                        addToMessageQueue(EMPTY_SENDER, Commands.DEFAULT, SpamPriority.LOWEST, EMPTY_MAP);
                    }
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketChatMessage) {
            updateLastMessageSentTime();
        }
    }

    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.Incoming.Pre event) {
        if(event.getPacket() instanceof SPacketPlayerListItem && System.currentTimeMillis() > timeWhenToReportJoinEvents) {
            SPacketPlayerListItem playerListPacket = (SPacketPlayerListItem)event.getPacket();
            final Commands command;
            switch (playerListPacket.getAction()) {
                case ADD_PLAYER:
                    command = Commands.PLAYER_JOIN;
                    break;
                case REMOVE_PLAYER:
                    command = Commands.PLAYER_LEAVE;
                    break;
                default:
                    command = null;
                    break;
            }
            if(Objects.nonNull(command) && Objects.nonNull(WRAPPER.getLocalPlayer())) {
                try {
                    playerListPacket.getEntries()
                            .stream()
                            .filter(Objects::nonNull)
                            .filter(data -> {
                                String name = getNameFromComponent(data.getProfile());
                                return !Strings.isNullOrEmpty(name) && !isLocalPlayer(name);
                            })
                            .forEach(data -> {
                                Map<String, Object> values = Maps.newHashMap();
                                values.put("username", getNameFromComponent(data.getProfile()));
                                addToMessageQueue(EMPTY_SENDER, command, SpamPriority.HIGH, values);
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        Pattern divide = Pattern.compile("((<.*?>)(.*))");
        Matcher matcher = divide.matcher(event.getMessage().getUnformattedText());
        if(matcher.matches()) {
            try {
                String username = matcher.group(2).replaceAll("<", "").replaceAll(">", "");
                String message = matcher.group(3);
                if (message.startsWith(" ")) message = message.substring(1);
                if (Objects.nonNull(WRAPPER.getLocalPlayer()) &&
                        !Strings.isNullOrEmpty(username) &&
                        !Strings.isNullOrEmpty(message) &&
                        !WRAPPER.getLocalPlayer().getGameProfile().getName().equals(username) &&
                        PlayerCooldownChecker.isInCooldown(username) &&
                        !isUsersMessageInQueue(username)) {
                    for (Commands command : Commands.values()) {
                        if (command.isAction()) {
                            String[] div = message.split(" ");
                            String key = div[0];
                            if (command.getKeyword().equals(key)) {
                                // attempt to extract extra string
                                StringBuilder builder = new StringBuilder();
                                for (int i = 1; i < div.length; i++) {
                                    builder.append(div[i]);
                                    builder.append(" ");
                                }
                                // remove last space
                                int index = builder.length() - 1;
                                if (index > -1 && builder.charAt(index) == ' ') builder.deleteCharAt(index);
                                String value = builder.toString();
                                Map<String, Object> values = Maps.newHashMap();
                                values.put("username", username);
                                values.put("message", value);
                                if(isUniqueMessage(value) && isWithinInputLimits(value) && addToMessageQueue(username, command, SpamPriority.HIGH, values)) {
                                    // now rebuild chat message
                                    event.setMessage(new TextComponentString(String.format("<§2%s§r> §l%s§r §o%s§r", username, key, value)));
                                    PlayerCooldownChecker.setInCooldown(username);
                                }
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private enum SpamPriority {
        HIGHEST,
        HIGH,
        MEDIUM,
        LOW,
        LOWEST
        ;
    }

    private class SpamMessage implements Comparable<SpamMessage> {
        private final String sender;
        private final String message;
        private final SpamPriority priority;

        public SpamMessage(String sender, String message, SpamPriority priority) {
            this.sender = sender;
            this.message = message;
            this.priority = priority;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }

        public SpamPriority getPriority() {
            return priority;
        }

        @Override
        public int compareTo(SpamMessage o) {
            return getPriority().compareTo(o.getPriority()); //TODO: maybe * -1 to convert it
        }
    }

    private interface SpamFunction {
        String apply(List<String> messages, Map<String, Object> args) throws Exception;
    }

    private static class PlayerCooldownChecker {
        private static final String TIME_KEY = "TIME";
        private static final String MESSAGE_KEY = "MESSAGE";

        private static final Map<String, Map<String, Object>> cooldowns = Maps.newConcurrentMap();

        private static long cooldownPeriod = 1000;

        public static long getCooldownPeriod() {
            return cooldownPeriod;
        }

        public static void setCooldownPeriod(long cooldownPeriod) {
            PlayerCooldownChecker.cooldownPeriod = cooldownPeriod;
        }

        private static boolean isInCooldown(String username) {
            boolean result = false;
            for(Map.Entry<String, Map<String, Object>> entry : cooldowns.entrySet()) {
                final Long period = (Long) entry.getValue().get(TIME_KEY);
                if(period > System.currentTimeMillis()) {
                    if(entry.getKey().equals(username)) result = true;
                    cooldowns.remove(entry.getKey());
                }
            }
            return !result;
        }

        private static Map<String, Object> getOrCreateEntry(String username) {
            return cooldowns.computeIfAbsent(username, str -> {
                Map<String, Object> input = Maps.newHashMap();
                input.put(TIME_KEY, -1);
                input.put(MESSAGE_KEY, "");
                return input;
            });
        }

        private static void setInCooldown(String username) {
            setInCooldown(username, "");
        }

        private static void setInCooldown(String username, String message) {
            Map<String, Object> info = getOrCreateEntry(username);
            info.replace(TIME_KEY, System.currentTimeMillis() + cooldownPeriod);
            info.replace(MESSAGE_KEY, message);
        }
    }
}
