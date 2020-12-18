package dev.fiki.forgehax.api.event;

public interface ListenerFlags {
  int NONE = 0;
  int ALLOW_CANCELED = 1;
  int ALLOW_IGNORED_PACKETS = 2;

  static boolean present(EventListener listener, int flag) {
    return (listener.getFlags() & flag) != 0;
  }
}
