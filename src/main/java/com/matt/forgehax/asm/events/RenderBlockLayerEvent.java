package com.matt.forgehax.asm.events;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderBlockLayerEvent extends Event {
  private final BlockRenderLayer renderLayer;
  private final double partialTicks;

  public RenderBlockLayerEvent(BlockRenderLayer renderLayer, double partialTicks) {
    this.renderLayer = renderLayer;
    this.partialTicks = partialTicks;
  }

  public BlockRenderLayer getRenderLayer() {
    return renderLayer;
  }

  public double getPartialTicks() {
    return partialTicks;
  }

  @Cancelable
  public static class Pre extends RenderBlockLayerEvent {
    public Pre(BlockRenderLayer renderLayer, double partialTicks) {
      super(renderLayer, partialTicks);
    }
  }

  public static class Post extends RenderBlockLayerEvent {
    public Post(BlockRenderLayer renderLayer, double partialTicks) {
      super(renderLayer, partialTicks);
    }
  }
}
