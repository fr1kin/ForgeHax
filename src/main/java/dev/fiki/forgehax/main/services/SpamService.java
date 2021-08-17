package dev.fiki.forgehax.main.services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.fiki.forgehax.api.cmd.settings.LongSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.spam.SpamMessage;
import dev.fiki.forgehax.main.Common;
import joptsimple.internal.Strings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

  public final LongSetting delay = newLongSetting()
      .name("delay")
      .description("Delay between each message in ms")
      .defaultTo(5000L)
      .changedListener(
          (from, to) -> {
            nextSendMs = 0L;
          })
      .build();

  /**
   * Next time to send a message
   */
  private long nextSendMs = 0L;

  private Map<String, AtomicLong> customDelays = Maps.newConcurrentMap();

  {
    newSimpleCommand()
        .name("reset")
        .description("Resets spam delay and send list")
        .executor(
            args -> {
              nextSendMs = Long.MAX_VALUE;
              SENDING.clear();
              customDelays.clear();
              nextSendMs = 0;
              args.inform("Reset chat spam");
            })
        .build();
  }

  @SubscribeListener
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
                Common.getLocalPlayer().chat(msg.getMessage());
                customDelays.computeIfAbsent(msg.getType(), t -> new AtomicLong(0L))
                    .set(System.currentTimeMillis() + msg.getDelay());
                nextSendMs = System.currentTimeMillis() + delay.getValue();
                SENDING.remove(msg);
              });
    }
  }
}
