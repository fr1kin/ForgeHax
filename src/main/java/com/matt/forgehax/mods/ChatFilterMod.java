package com.matt.forgehax.mods;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.serialization.ISerializableJson;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@RegisterMod
public class ChatFilterMod extends ToggleMod {
  private final Map<String, Pattern> patternCache = new WeakHashMap<>();

  private final Options<FilterEntry> filterList =
      getCommandStub()
          .builders()
          .<FilterEntry>newOptionsBuilder()
          .name("filters")
          .description("Saved filter config")
          .factory(FilterEntry::new)
          .supplier(MemeSet::new)
          .build();

  public ChatFilterMod() {
    super(Category.MISC, "ChatFilter", false, "Filter chat by regex");
  }

  @SubscribeEvent
  public void onChatMessage(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketChat) {
      final SPacketChat packet = event.getPacket();
      final String message = packet.getChatComponent().getUnformattedText();

      final boolean shouldFilter = filterList.stream()
          .map(FilterEntry::getRegex)
          .map(regex -> patternCache.computeIfAbsent(regex, Pattern::compile))
          .anyMatch(pattern -> partialMatch(pattern, message));

      if (shouldFilter) {
        event.setCanceled(true);
      }
    }
  }

  private static boolean partialMatch(Pattern pattern, String str) {
    final Matcher m = pattern.matcher(str);
    return m.find();
  }


  @Override
  protected void onLoad() {
    super.onLoad();
    filterList.deserializeAll();

    // TODO: allow flags
    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("filter")
        .description("filter <name> <regex>")
        .processor(
            data -> {
              data.requiredArguments(2);
              final String name = data.getArgumentAsString(0);
              final String regex = data.getArgumentAsString(1);

              try {
                Pattern.compile(regex);
                this.filterList.add(new FilterEntry(name, regex));
                Helper.printMessage("Added regex with name \"%s\"", name);
              } catch (PatternSyntaxException ex) {
                Helper.printError("Invalid regex: " + ex.getMessage());
              }
            })
        .build();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("remove")
        .description("remove a filter by name")
        .processor(
            data -> {
              data.requiredArguments(1);
              final String name = data.getArgumentAsString(0);

              final boolean changed = filterList.removeIf(entry -> entry.name.equals(name));
              if (changed) {
                Helper.printMessage("Removed filter with name \"%s\"", name);
              } else {
                Helper.printMessage("No filter found with name \"%s\"", name);
              }
            })
        .build();

    getCommandStub()
        .builders()
        .newCommandBuilder()
        .name("list")
        .description("List all the filters")
        .processor(data -> {
          Helper.printMessage("Filters (%d):", filterList.size());
          for (FilterEntry entry : filterList) {
            data.write(entry.name + ": " + "\"" + entry.regex + "\"");
          }
        })
        .build();
  }


  private static class FilterEntry implements ISerializableJson {
    final String name;
    private String regex;

    FilterEntry(String name) {
      this.name = name;
    }

    FilterEntry(String name, String regex) {
      this(name);
      this.regex = regex;
    }

    public String getRegex() {
      return this.regex;
    }

    @Override
    public void serialize(JsonWriter writer) throws IOException {
      writer.value(this.regex);
    }

    @Override
    public void deserialize(JsonReader reader)  {
      this.regex  = new JsonParser().parse(reader).getAsString();
    }

    @Override
    public String getUniqueHeader() {
      return this.name;
    }

    @Override
    public String toString() {
      return getUniqueHeader();
    }

  }

  private static class MemeSet implements Collection<FilterEntry> {
    private final Map<String, FilterEntry> map = new HashMap<>();

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return map.containsKey(((FilterEntry)o).name);
    }

    @Override
    public Iterator<FilterEntry> iterator() {
      return map.values().iterator();
    }

    @Override
    public Object[] toArray() {
      return map.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return map.values().toArray(a);
    }

    @Override
    public boolean add(FilterEntry filterEntry) {
      return map.put(filterEntry.name, filterEntry) != null;
    }

    @Override
    public boolean remove(Object o) {
      return map.remove(((FilterEntry)o).name) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return map.values().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends FilterEntry> c) {
      boolean changed = false;
      for (FilterEntry entry : c) {
        if (map.put(entry.name, entry) != null) changed = true;
      }
      return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return map.values().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return map.values().retainAll(c);
    }

    @Override
    public void clear() {
      map.clear();
    }
  }

}
