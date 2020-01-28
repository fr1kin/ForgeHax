package com.matt.forgehax.events;

import net.minecraftforge.eventbus.api.Event;

public class ClientTickEvent extends Event {
  // TODO: add listener service
  public static class Pre extends ClientTickEvent {}
  public static class Post extends ClientTickEvent {}
}
