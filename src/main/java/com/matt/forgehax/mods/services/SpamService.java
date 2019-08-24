package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.spam.SpamMessage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 7/21/2017 by fr1kin
 */
@RegisterMod
public class SpamService extends ServiceMod {

  private static final List<SpamMessage> SENDING = Lists.newCopyOnWriteArrayList();

  public static boolean send(SpamMessage spam) {
    if (!SENDING.contains(spam)) {
      return SENDING.add(spam);
    } else {
      return false;
    }
  }

  public static boolean isActivatorPresent(String activator) {
    if (activator == null) {
      return false;
    }
    for (SpamMessage msg : SENDING) {
      if (activator.equalsIgnoreCase(msg.getActivator())) {
        return true;
      }
    }
    return false;
  }

  public static boolean isEmpty() {
    return SENDING.isEmpty();
  }

  public final Setting<Long> delay =
    getCommandStub()
      .builders()
      .<Long>newSettingBuilder()
      .name("delay")
      .description("Delay between each message in ms")
      .defaultTo(5000L)
      .changed(
        cb -> {
          nextSendMs = 0L;
        })
      .build();
  
  /**
   * Next time to send a message
   */
  private long nextSendMs = 0L;

  private Map<String, AtomicLong> customDelays = Maps.newConcurrentMap();

  public SpamService() {
    super("SpamService");
  }

  @Override
  protected void onLoad() {
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("reset")
      .description("Resets spam delay and send list")
      .processor(
        data -> {
          nextSendMs = Long.MAX_VALUE;
          SENDING.clear();
          customDelays.clear();
          nextSendMs = 0;
          data.write("Reset chat spam");
        })
      .build();
  }

  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    if (!SENDING.isEmpty() && System.currentTimeMillis() > nextSendMs) {
      SENDING
        .stream()
        .filter(
          msg -> {
            if (!Strings.isNullOrEmpty(msg.getType())) {
              long time = customDelays.getOrDefault(msg.getType(), new AtomicLong(0)).get();
              return System.currentTimeMillis() > time;
            } else {
              return true;
            }
          })
        .sorted()
        .findFirst()
        .ifPresent(
          msg -> {
            getLocalPlayer().sendChatMessage(msg.getMessage());
            customDelays
              .computeIfAbsent(msg.getType(), t -> new AtomicLong(0L))
              .set(System.currentTimeMillis() + msg.getDelay());
            nextSendMs = System.currentTimeMillis() + delay.get();
            SENDING.remove(msg);
          });
    }
  }
}
