package com.matt.forgehax.asm.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class DrawBlockBoundingBoxEvent extends Event {
  public float red;
  public float green;
  public float blue;
  public float alpha;

  public DrawBlockBoundingBoxEvent(float r, float g, float b, float a) {
    this.red = r;
    this.green = g;
    this.blue = b;
    this.alpha = a;
  }

  public static class Pre extends DrawBlockBoundingBoxEvent {
    public Pre(float r, float g, float b, float a) {
      super(r, g, b, a);
    }
  }

  public static class Post extends Event {}
}
