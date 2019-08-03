package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getFileManager;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.mods.services.SpamService;
import com.matt.forgehax.util.ArrayHelper;
import com.matt.forgehax.util.SafeConverter;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.serialization.GsonConstant;
import com.matt.forgehax.util.spam.SpamEntry;
import com.matt.forgehax.util.spam.SpamMessage;
import com.matt.forgehax.util.spam.SpamTokens;
import com.matt.forgehax.util.spam.SpamTrigger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Scanner;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class ChatBot extends ToggleMod {
  private final Options<SpamEntry> spams =
      getCommandStub()
          .builders()
          .<SpamEntry>newOptionsBuilder()
          .name("spam")
          .description("Contents to spam")
          .factory(SpamEntry::new)
          .supplier(Sets::newConcurrentHashSet)
          .build();

  private final Setting<Integer> max_input_length =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("max_input_length")
          .description("Maximum chat input length allowed")
          .defaultTo(16)
          .min(0)
          .max(256)
          .build();

  private final Setting<Boolean> resetSequentialIndex =
    getCommandStub()
    .builders()
    .<Boolean>newSettingBuilder()
    .name("reset-sequential")
    .description("start spam list anew in sequential mode")
    .defaultTo(false)
    .build();

  public ChatBot() {
    super(Category.MISC, "ChatBot", false, "Spam chat");
  }

  @Override
  protected void onDisabled() {
    if (resetSequentialIndex.get()) {
      for (SpamEntry e : spams) {
        e.reset();
      }
    }
  }

  @Override
  protected void onLoad() {
    spams
        .builders()
        .newCommandBuilder()
        .name("add")
        .description("Add new spam list")
        .options(
            parser -> {
              parser.accepts("keyword", "Message activation keyword").withRequiredArg();
              parser.accepts("type", "Spam type (random, sequential)").withRequiredArg();
              parser
                  .accepts(
                      "trigger",
                      "How the spam will be triggered (spam, reply, reply_with_input, player_connect, player_disconnect)")
                  .withRequiredArg();
              parser.accepts("enabled", "Enabled").withRequiredArg();
              parser
                  .accepts("delay", "Custom delay between messages of the same type")
                  .withRequiredArg();
            })
        .processor(
            data -> {
              data.requiredArguments(1);
              String name = data.getArgumentAsString(0);

              boolean givenInput =
                  data.hasOption("keyword")
                      || data.hasOption("type")
                      || data.hasOption("trigger")
                      || data.hasOption("enabled")
                      || data.hasOption("delay");

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
                if (!entry.isEnabled() && resetSequentialIndex.get()) entry.reset();
              if (data.hasOption("delay"))
                entry.setDelay(SafeConverter.toLong(data.getOptionAsString("delay")));

              if (data.getArgumentCount() == 2) {
                String msg = data.getArgumentAsString(1);
                entry.add(msg);
                data.write("Added message \"" + msg + "\"");
              }

              if (givenInput) {
                data.write("keyword=" + entry.getKeyword());
                data.write("type=" + entry.getType().name());
                data.write("trigger=" + entry.getTrigger().name());
                data.write("enabled=" + Boolean.toString(entry.isEnabled()));
                data.write("delay=" + Long.toString(entry.getDelay()));
              }

              data.markSuccess();
            })
        .success(e -> spams.serialize())
        .build();

    spams
        .builders()
        .newCommandBuilder()
        .name("import")
        .description("Import a txt or json file")
        .processor(
            data -> {
              data.requiredArguments(2);
              String name = data.getArgumentAsString(0);
              String fileN = data.getArgumentAsString(1);

              SpamEntry entry = spams.get(name);
              if (entry == null) {
                entry = new SpamEntry(name);
                spams.add(entry);
                data.write("Added new entry \"" + name + "\"");
              }

              Path file = getFileManager().getBaseResolve(fileN);
              if (Files.exists(file)) {
                if (fileN.endsWith(".json")) {
                  try {
                    JsonParser parser = new JsonParser();
                    JsonElement element = parser.parse(new String(Files.readAllBytes(file)));
                    if (element.isJsonArray()) {
                      JsonArray head = (JsonArray) element;
                      int count = 0;
                      for (JsonElement e : head) {
                        if (e.isJsonPrimitive()) {
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
                } else if (fileN.endsWith(".txt")) {
                  try {
                    Scanner scanner = new Scanner(file);
                    int count = 0;
                    while (scanner.hasNextLine()) {
                      String next = scanner.nextLine();
                      if (!Strings.isNullOrEmpty(next)) {
                        entry.add(next);
                        ++count;
                      }
                    }
                    data.write("Successfully imported " + count + " messages");
                  } catch (Throwable t) {
                    data.write("Failed parsing text: " + t.getMessage());
                  }
                } else {
                  data.write(
                      "Invalid file extension for \"" + fileN + "\" (requires .txt or .json)");
                }
              } else {
                data.write("Could not find file \"" + fileN + "\" in base directory");
              }
            })
        .success(e -> spams.serialize())
        .build();

    spams
        .builders()
        .newCommandBuilder()
        .name("export")
        .description("Export all the contents of an entry")
        .processor(
            data -> {
              data.requiredArguments(2);
              String name = data.getArgumentAsString(0);
              String fileN = data.getArgumentAsString(1);

              SpamEntry entry = spams.get(name);
              if (entry == null) {
                data.write("No such entry: " + name);
                return;
              }

              if (!fileN.endsWith(".json") && !fileN.endsWith(".txt")) fileN += ".txt";

              Path file = getFileManager().getBaseResolve(fileN);

              try {
                if (!Files.isDirectory(file.getParent())) Files.createDirectories(file.getParent());

                if (name.endsWith(".json")) {
                  final JsonArray head = new JsonArray();
                  entry.getMessages().forEach(str -> head.add(new JsonPrimitive(str)));
                  Files.write(file, GsonConstant.GSON_PRETTY.toJson(head).getBytes());
                } else {
                  final StringBuilder builder = new StringBuilder();
                  entry
                      .getMessages()
                      .forEach(
                          str -> {
                            builder.append(str);
                            builder.append('\n');
                          });
                  Files.write(file, builder.toString().getBytes());
                }
                data.markSuccess();
              } catch (Throwable t) {
                data.write("Failed exporting file: " + t.getMessage());
              }
            })
        .build();

    spams
        .builders()
        .newCommandBuilder()
        .name("remove")
        .description("Remove spam entry")
        .processor(
            data -> {
              data.requiredArguments(1);
              String name = data.getArgumentAsString(0);

              SpamEntry entry = spams.get(name);
              if (entry != null) {
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

    spams
        .builders()
        .newCommandBuilder()
        .name("list")
        .description("List all current entries")
        .processor(
            data -> {
              final StringBuilder builder = new StringBuilder();
              Iterator<SpamEntry> it = spams.iterator();
              while (it.hasNext()) {
                SpamEntry next = it.next();
                builder.append(next.getName());
                if (it.hasNext()) builder.append(", ");
              }
              data.write(builder.toString());
              data.markSuccess();
            })
        .build();
  }

  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (SpamService.isEmpty() && !spams.isEmpty())
      for (SpamEntry e : spams) {
        if (e.isEnabled() && !e.isEmpty() && e.getTrigger().equals(SpamTrigger.SPAM)) {
          SpamService.send(
              new SpamMessage(
                  e.next(),
                  "SPAM" + e.getName(),
                  e.getDelay(),
                  "self" + e.getName().toLowerCase(),
                  PriorityEnum.DEFAULT));
          return;
        }
      }
  }

  @SubscribeEvent
  public void onChat(ChatMessageEvent event) {
    if (event.getSender().isLocalPlayer()) return;

    String[] args = event.getMessage().split(" ");
    final String sender = event.getSender().getId().toString();
    final String keyword = ArrayHelper.getOrDefault(args, 0, Strings.EMPTY);
    final String arg = ArrayHelper.getOrDefault(args, 1, Strings.EMPTY);
    spams
        .stream()
        .filter(SpamEntry::isEnabled)
        .filter(e -> e.getKeyword().equalsIgnoreCase(keyword))
        .forEach(
            e -> {
              switch (e.getTrigger()) {
                case REPLY:
                  {
                    SpamService.send(
                        new SpamMessage(
                            SpamTokens.SENDER_NAME.fill(e.next(), event.getSender().getName()),
                            "REPLY" + e.getName(),
                            e.getDelay(),
                            sender,
                            PriorityEnum.HIGH));
                    break;
                  }
                case REPLY_WITH_INPUT:
                  {
                    if (!Strings.isNullOrEmpty(arg) && arg.length() <= max_input_length.get())
                      SpamService.send(
                          new SpamMessage(
                              SpamTokens.fillAll(
                                  e.next(),
                                  SpamTokens.PLAYERNAME_SENDERNAME,
                                  arg,
                                  event.getSender().getName()),
                              "REPLY_WITH_INPUT" + e.getName(),
                              e.getDelay(),
                              sender,
                              PriorityEnum.HIGH));
                    break;
                  }
                default:
                  break;
              }
            });
  }

  @SubscribeEvent
  public void onPlayerConnect(PlayerConnectEvent.Join event) {
    final String player = event.getProfile() != null ? event.getProfile().getName() : "null";
    spams
        .stream()
        .filter(SpamEntry::isEnabled)
        .forEach(
            e -> {
              switch (e.getTrigger()) {
                case PLAYER_CONNECT:
                  {
                    SpamService.send(
                        new SpamMessage(
                            SpamTokens.fillAll(
                                e.next(),
                                SpamTokens.PLAYERNAME_NAMEHISTORY,
                                player,
                                event.getPlayerInfo().getNameHistoryAsString()),
                            "PLAYER_CONNECT" + e.getName(),
                            e.getDelay(),
                            null,
                            PriorityEnum.HIGH));
                    break;
                  }
                default:
                  break;
              }
            });
  }

  @SubscribeEvent
  public void onPlayerDisconnect(PlayerConnectEvent.Leave event) {
    final String player = event.getProfile() != null ? event.getProfile().getName() : "null";
    spams
        .stream()
        .filter(SpamEntry::isEnabled)
        .forEach(
            e -> {
              switch (e.getTrigger()) {
                case PLAYER_DISCONNECT:
                  {
                    SpamService.send(
                        new SpamMessage(
                            SpamTokens.fillAll(
                                e.next(),
                                SpamTokens.PLAYERNAME_NAMEHISTORY,
                                player,
                                event.getPlayerInfo().getNameHistoryAsString()),
                            "PLAYER_DISCONNECT" + e.getName(),
                            e.getDelay(),
                            null,
                            PriorityEnum.HIGH));
                    break;
                  }
                default:
                  break;
              }
            });
  }
}
