package dev.fiki.forgehax.api.events;

import dev.fiki.forgehax.api.event.Event;
import net.minecraft.client.world.ClientWorld;

public abstract class ClientWorldEvent extends Event {
  private final ClientWorld world;

  private ClientWorldEvent(ClientWorld world) {
    this.world = world;
  }

  public ClientWorld getWorld() {
    return world;
  }

  public abstract boolean isLoading();

  public boolean isUnloading() {
    return !isLoading();
  }

  public static class Load extends ClientWorldEvent {
    public Load(ClientWorld world) {
      super(world);
    }

    @Override
    public boolean isLoading() {
      return true;
    }
  }

  public static class Unload extends ClientWorldEvent {
    public Unload(ClientWorld world) {
      super(world);
    }

    @Override
    public boolean isLoading() {
      return false;
    }
  }
}
