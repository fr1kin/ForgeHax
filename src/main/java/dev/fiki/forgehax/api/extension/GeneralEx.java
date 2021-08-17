package dev.fiki.forgehax.api.extension;

import dev.fiki.forgehax.asm.events.packet.PacketEvent;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Log4j2
public class GeneralEx {
  public static void dispatchNetworkPacket(NetworkManager nm, IPacket<?> packet, boolean silent) {
    if (nm != null) {
      if (silent) {
        PacketEvent.ignore(packet);
      }
      nm.send(packet);
    } else {
      log.warn("NetworkManager is null! Packet {} was not dispatched (possibly not connected to server?)", packet);
    }
  }

  public static void dispatchNetworkPacket(NetworkManager nm, IPacket<?> packet) {
    dispatchNetworkPacket(nm, packet, false);
  }

  public static void dispatchSilentNetworkPacket(NetworkManager nm, IPacket<?> packet) {
    dispatchNetworkPacket(nm, packet, true);
  }

  public static <T> Stream<T> negated(Stream<T> stream, Predicate<? super T> predicate) {
    return stream.filter(predicate.negate());
  }

  public static <T> CompletableFuture<T> wait(CompletableFuture<T> stage, long ms, Executor async, Executor main) {
    return stage.thenApplyAsync(a -> {
      if (ms > 0) {
        try {
          Thread.sleep(ms);
        } catch (InterruptedException e) {
          // ignore
        }
      }
      return a;
    }, async).thenApplyAsync(a -> a, main);
  }

  public static <T> CompletableFuture<T> waitTicks(CompletableFuture<T> stage, int ticks, Executor async, Executor main) {
    return wait(stage, 50L * Math.max(ticks, 0), async, main);
  }

  public static <T> boolean containsIndex(Collection<T> list, int index) {
    return list != null && index >= 0 && index < list.size();
  }

  public static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  public static long clamp(long value, long min, long max) {
    return Math.max(min, Math.min(max, value));
  }

  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static float clamp(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  public static double scale(double x, double from_min, double from_max, double to_min, double to_max) {
    return to_min + (to_max - to_min) * ((x - from_min) / (from_max - from_min));
  }

  public static String globToRegex(String glob) {
    StringBuilder builder = new StringBuilder("^");

    boolean literal = false;
    for (int i = 0; i < glob.length(); i++) {
      char at = glob.charAt(i);
      switch (at) {
        case '*':
        case '?':
          // this is an expression, end literal if started
          if (literal) {
            builder.append("\\E");
            literal = false;
          }
          // nasty
          switch (at) {
            case '*':
              // do not allow repeated wildcards
              if (i - 1 < 0 || glob.charAt(i - 1) != '*') {
                // * = match any multiple characters
                builder.append(".*");
              }
              break;
            case '?':
              // ? = match any single character
              builder.append('.');
          }
          break;
        default:
          if (!literal) {
            builder.append("\\Q");
            literal = true;
          }
          builder.append(at);
      }
    }

    // end literal if started
    if (literal) {
      builder.append("\\E");
    }

    return builder.append("$").toString();
  }
}
