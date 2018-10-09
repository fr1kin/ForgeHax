package com.matt.forgehax.events;

public abstract class ServerConnectionEvent {

  // fired when connecting to a server
  public static class Connect {}

  // fired when disconnecting from a server
  public static class Disconnect {}

}
