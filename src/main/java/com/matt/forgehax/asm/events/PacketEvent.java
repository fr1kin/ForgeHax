package com.matt.forgehax.asm.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PacketEvent extends Event {
  
  private final Packet<?> packet;

  public PacketEvent(Packet<?> packetIn) {
    packet = packetIn;
  }

  public <T extends Packet<?>> T getPacket() {
    return (T) packet;
  }

  public static class Outgoing extends PacketEvent {
  
    public Outgoing(Packet<?> packetIn) {
      super(packetIn);
    }

    @Cancelable
    public static class Pre extends Outgoing {
  
      public Pre(Packet<?> packetIn) {
        super(packetIn);
      }
    }

    public static class Post extends Outgoing {
  
      public Post(Packet<?> packetIn) {
        super(packetIn);
      }
    }
  }

  public static class Incoming extends PacketEvent {
  
    public Incoming(Packet<?> packetIn) {
      super(packetIn);
    }

    @Cancelable
    public static class Pre extends Incoming {
  
      public Pre(Packet<?> packetIn) {
        super(packetIn);
      }
    }

    public static class Post extends Incoming {
  
      public Post(Packet<?> packetIn) {
        super(packetIn);
      }
    }
  }
}
