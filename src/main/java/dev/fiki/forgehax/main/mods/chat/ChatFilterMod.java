package dev.fiki.forgehax.main.mods.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fiki.forgehax.api.cmd.settings.collections.CustomSettingSet;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import net.minecraft.network.play.server.SChatPacket;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: 1.15 fix this

//@RegisterMod(
//    name = "ChatFilter",
//    description = "Filter chat by regex",
//    category = Category.CHAT
//)
public class ChatFilterMod extends ToggleMod {
  private final Map<String, Pattern> patternCache = new WeakHashMap<>();

  private final CustomSettingSet<FilterEntry> filterList = newCustomSettingSet(FilterEntry.class)
      .name("filters")
      .description("Saved filter config")
      .valueSupplier(FilterEntry::new)
      .supplier(MemeSet::new)
      .build();

  @SubscribeListener
  public void onChatMessage(PacketInboundEvent event) {
    if (event.getPacket() instanceof SChatPacket) {
      final SChatPacket packet = (SChatPacket) event.getPacket();
      final String message = packet.getMessage().getString();

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


//  @Override
//  protected void onLoad() {
//    super.onLoad();
//    filterList.deserializeAll();
//
//    // TODO: allow flags
//    getCommandStub()
//        .builders()
//        .newCommandBuilder()
//        .name("filter")
//        .description("filter <name> <regex>")
//        .processor(
//            data -> {
//              data.requiredArguments(2);
//              final String name = data.getArgumentAsString(0);
//              final String regex = data.getArgumentAsString(1);
//
//              try {
//                Pattern.compile(regex);
//                this.filterList.add(new FilterEntry(name, regex));
//                Common.print("Added regex with name \"%s\"", name);
//              } catch (PatternSyntaxException ex) {
//                Common.printError("Invalid regex: " + ex.getMessage());
//              }
//            })
//        .build();
//
//    getCommandStub()
//        .builders()
//        .newCommandBuilder()
//        .name("remove")
//        .description("remove a filter by name")
//        .processor(
//            data -> {
//              data.requiredArguments(1);
//              final String name = data.getArgumentAsString(0);
//
//              final boolean changed = filterList.removeIf(entry -> entry.name.equals(name));
//              if (changed) {
//                Common.print("Removed filter with name \"%s\"", name);
//              } else {
//                Common.print("No filter found with name \"%s\"", name);
//              }
//            })
//        .build();
//
//    getCommandStub()
//        .builders()
//        .newCommandBuilder()
//        .name("list")
//        .description("List all the filters")
//        .processor(data -> {
//          Common.print("Filters (%d):", filterList.size());
//          for (FilterEntry entry : filterList) {
//            data.write(entry.name + ": " + "\"" + entry.regex + "\"");
//          }
//        })
//        .build();
//  }


  private static class FilterEntry implements IJsonSerializable {
    private String name;
    private String regex;

    public String getRegex() {
      return this.regex;
    }

    @Override
    public JsonElement serialize() {
      return new JsonPrimitive(this.regex);
    }

    @Override
    public void deserialize(JsonElement json) {
      this.regex = json.getAsString();
    }
  }

  private static class MemeSet implements Set<FilterEntry> {
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
      return map.containsKey(((FilterEntry) o).name);
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
      return map.remove(((FilterEntry) o).name) != null;
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
