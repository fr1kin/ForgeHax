package dev.fiki.forgehax.api.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import dev.fiki.forgehax.main.Common;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Cancelable
@AllArgsConstructor
public class RenderPlaneEvent extends Event {
  private final MatrixStack stack;
  private final float partialTicks;

  public final int getScreenWidth() {
    return Common.getScreenWidth();
  }

  public final int getScreenHeight() {
    return Common.getScreenHeight();
  }

  public static class Back extends RenderPlaneEvent {
    public Back(MatrixStack stack, float partialTicks) {
      super(stack, partialTicks);
    }
  }

  public static class Top extends RenderPlaneEvent {
    public Top(MatrixStack stack, float partialTicks) {
      super(stack, partialTicks);
    }
  }

  public static class Helmet extends RenderPlaneEvent {
    public Helmet(MatrixStack stack, float partialTicks) {
      super(stack, partialTicks);
    }
  }

  public static class Portal extends RenderPlaneEvent {
    public Portal(MatrixStack stack, float partialTicks) {
      super(stack, partialTicks);
    }
  }
}
