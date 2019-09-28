package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getFileManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.exception.CommandExecuteException;
import com.matt.forgehax.util.entry.ClassEntry;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.serialization.GsonConstant;
import io.netty.buffer.ByteBuf;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import joptsimple.internal.Strings;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.Packet;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 10/12/2017 by fr1kin
 */
@RegisterMod
public class PacketLogger extends ToggleMod implements GsonConstant {
  
  private static final String DATE = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date());
  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
  
  private static final Path INBOUND =
      getFileManager().getMkBaseResolve("logs/packets/IN-" + DATE + ".log");
  private static final Path OUTBOUND =
      getFileManager().getMkBaseResolve("logs/packets/OUT-" + DATE + ".log");
  
  private volatile BufferedWriter stream_packet_in = null;
  private volatile BufferedWriter stream_packet_out = null;
  
  private final Setting<Boolean> blacklist_on =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("blacklist_on")
          .description("Enables the blacklist")
          .defaultTo(false)
          .build();
  
  private final Options<ClassEntry> blacklist =
      getCommandStub()
          .builders()
          .<ClassEntry>newOptionsBuilder()
          .name("blacklist")
          .description("Classes to ignore")
          .factory(ClassEntry::new)
          .supplier(Sets::newConcurrentHashSet)
          .build();
  
  public PacketLogger() {
    super(Category.MISC, "PacketLogger", false, "Logs all packets to file");
  }
  
  @Override
  protected void onLoad() {
    blacklist
        .builders()
        .newCommandBuilder()
        .name("add")
        .description("Add class")
        .processor(
            data -> {
              data.requiredArguments(1);
              final String className = data.getArgumentAsString(0).toLowerCase();
              
              if (Strings.isNullOrEmpty(className)) {
                throw new CommandExecuteException("Empty or null argument");
              }
              
              Optional<Class<?>> match =
                  getLoadedClasses(Launch.classLoader)
                      .stream()
                      .filter(Packet.class::isAssignableFrom)
                      .filter(clazz -> clazz.getCanonicalName().toLowerCase().contains(className))
                      .sorted(
                          (o1, o2) ->
                              String.CASE_INSENSITIVE_ORDER.compare(
                                  o1.getCanonicalName(), o2.getCanonicalName()))
                      .findFirst();
              
              if (match.isPresent()) {
                Class<?> clazz = match.get();
                blacklist.add(new ClassEntry(clazz));
                data.write(String.format("Added class \"%s\"", clazz.getName()));
                data.markSuccess();
              } else {
                data.write(
                    String.format("Could not find any class name matching \"%s\"", className));
                data.markFailed();
              }
            })
        .success(cb -> blacklist.serialize())
        .build();
    
    blacklist
        .builders()
        .newCommandBuilder()
        .name("remove")
        .description("Remove class")
        .processor(
            data -> {
              data.requiredArguments(1);
              final String className = data.getArgumentAsString(0).toLowerCase();
              
              if (Strings.isNullOrEmpty(className)) {
                throw new CommandExecuteException("Empty or null argument");
              }
              
              Optional<ClassEntry> match =
                  blacklist
                      .stream()
                      .filter(entry -> className.contains(entry.getClassName().toLowerCase()))
                      .sorted(
                          (o1, o2) ->
                              String.CASE_INSENSITIVE_ORDER.compare(
                                  o1.getClassName(), o2.getClassName()))
                      .findFirst();
              
              if (match.isPresent()) {
                ClassEntry entry = match.get();
                if (blacklist.remove(entry)) {
                  data.write(String.format("Removed class \"%s\"", entry.getClassName()));
                  data.markSuccess();
                } else {
                  data.write(String.format("Could not remove class \"%s\"", entry.getClassName()));
                  data.markFailed();
                }
              } else {
                data.write(
                    String.format("Could not find any class name matching \"%s\"", className));
                data.markFailed();
              }
            })
        .success(cb -> blacklist.serialize())
        .build();
    
    blacklist
        .builders()
        .newCommandBuilder()
        .name("list")
        .description("List current contents")
        .processor(
            data -> {
              StringBuilder builder = new StringBuilder();
              Iterator<ClassEntry> it = blacklist.iterator();
              if (it.hasNext()) {
                builder.append(it.next().getClassName());
                if (it.hasNext()) {
                  builder.append(", ");
                }
              }
              data.write(builder.toString());
              data.markSuccess();
            })
        .build();
  }
  
  @Override
  protected void onEnabled() {
    try {
      if (!Files.exists(INBOUND)) {
        Files.createFile(INBOUND);
      }
      if (!Files.exists(OUTBOUND)) {
        Files.createFile(OUTBOUND);
      }
      
      stream_packet_in = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(INBOUND.toFile(), true), StandardCharsets.UTF_8));
      stream_packet_out = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(OUTBOUND.toFile(), true), StandardCharsets.UTF_8));
    } catch (Throwable t) {
    }
  }
  
  @Override
  protected void onDisabled() {
    if (stream_packet_in != null) {
      try {
        stream_packet_in.close();
        stream_packet_in = null;
      } catch (Throwable t) {
      }
    }
    
    if (stream_packet_out != null) {
      try {
        stream_packet_out.close();
        stream_packet_out = null;
      } catch (Throwable t) {
      }
    }
  }
  
  @Override
  protected void onUnload() {
    onDisabled();
  }
  
  @SubscribeEvent
  public void onPacketInbound(PacketEvent.Incoming.Pre event) {
    if (!blacklist_on.get() || blacklist.get(event.getPacket().getClass()) == null) {
      logPacket(stream_packet_in, event.getPacket());
    }
  }
  
  @SubscribeEvent
  public void onPacketOutbound(PacketEvent.Outgoing.Pre event) {
    if (!blacklist_on.get() || blacklist.get(event.getPacket().getClass()) == null) {
      logPacket(stream_packet_out, event.getPacket());
    }
  }
  
  //
  //
  //
  
  private static void logPacket(BufferedWriter stream, Packet<?> packet) {
    if (stream == null || packet == null) {
      return;
    }
    
    try {
      stream.write(
          String.format(
              "[%s][%s] %s\n",
              TIME_FORMAT.format(new Date()),
              packet.getClass().getSimpleName(),
              GSON_PRETTY.toJson(objectToElement(packet, true, null, Lists.newArrayList()))
          )
      );
      stream.flush();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  
  private static JsonElement objectToElement(
      Object obj,
      boolean deep,
      @Nullable Class<?> superClassLimit,
      @Nonnull final List<Object> dejaVu) {
    JsonObject json = new JsonObject();
    dejaVu.add(obj);
    if (obj != null) {
      Class<?> clazz = obj.getClass();
      do {
        try {
          for (Field field : clazz.getDeclaredFields()) {
            try {
              if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                Object value = null;
                String name;
                try {
                  value = field.get(obj);
                  name =
                      clazz.getSimpleName()
                          + "."
                          + field.getName()
                          + "@"
                          + Integer.toHexString(Objects.hashCode(value));
                } catch (Throwable t) {
                  name = "null@0";
                }
                if (dejaVu.contains(value)) {
                  json.addProperty(name, Objects.toString(value));
                } else {
                  json.add(name, objectAsJson(value, dejaVu));
                }
              }
            } catch (Throwable t) {
            }
          }
        } catch (Throwable t) {
        } finally {
          if (!deep || (superClassLimit != null && clazz.equals(superClassLimit))) {
            clazz = null;
          } else {
            clazz = clazz.getSuperclass();
          }
        }
      } while (clazz != null && !Object.class.equals(clazz));
    }
    return json;
  }
  
  private static JsonElement objectAsJson(Object obj, @Nonnull final List<Object> dejaVu) {
    if (obj == null) {
      return new JsonPrimitive("null");
    }
    try {
      if (obj.getClass().isPrimitive() || obj instanceof String) {
        return new JsonPrimitive(obj.toString());
      }
      if (obj.getClass().isArray()) {
        JsonArray array = new JsonArray();
        for (Object child : (Object[]) obj) {
          array.add(objectAsJson(child, dejaVu));
        }
        return array;
      } else if (obj instanceof Collection) {
        JsonArray array = new JsonArray();
        for (Object child : (Collection) obj) {
          array.add(objectAsJson(child, dejaVu));
        }
        return array;
      } else if (obj instanceof Map) {
        JsonObject json = new JsonObject();
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) obj).entrySet()) {
          json.add(entry.getKey().toString(), objectAsJson(entry.getValue(), dejaVu));
        }
        return json;
      } else if (obj instanceof ByteBuf) {
        return new JsonPrimitive(((ByteBuf) obj).toString(Charset.defaultCharset()));
      } else if (obj instanceof ITextComponent) {
        return new JsonPrimitive(((ITextComponent) obj).getUnformattedText());
      } else if (defaultToString(obj).equals(obj.toString())) { // not unique toString method
        // NOTE: make sure this is the last if statement
        return objectToElement(obj, true, null, dejaVu);
      }
    } catch (Throwable t) {
    }
    return new JsonPrimitive(obj.toString());
  }
  
  private static String defaultToString(Object o) {
    try {
      return o.getClass().getName() + "@" + Integer.toHexString(Objects.hashCode(o));
    } catch (Throwable t) {
      return "null@0";
    }
  }
  
  private static Collection<Class<?>> getLoadedClasses(ClassLoader loader) {
    try {
      Objects.requireNonNull(loader);
      Class<?> mclass = loader.getClass();
      while (mclass != ClassLoader.class) {
        mclass = mclass.getSuperclass();
      }
      Field classes = mclass.getDeclaredField("classes");
      classes.setAccessible(true);
      return (Vector<Class<?>>) classes.get(loader);
    } catch (Throwable t) {
      return Collections.emptyList();
    }
  }
}
