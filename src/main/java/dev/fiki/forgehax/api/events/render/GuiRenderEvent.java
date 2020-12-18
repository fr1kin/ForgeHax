package dev.fiki.forgehax.api.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class GuiRenderEvent extends Event {
  private final Screen gui;
  private final MatrixStack stack;
  private final int mouseX;
  private final int mouseY;
  private final float renderPartialTicks;

  @Cancelable
  public static class Pre extends GuiRenderEvent {
    public Pre(Screen gui, MatrixStack stack, int mouseX, int mouseY, float renderPartialTicks) {
      super(gui, stack, mouseX, mouseY, renderPartialTicks);
    }
  }

  public static class Post extends GuiRenderEvent {
    public Post(Screen gui, MatrixStack stack, int mouseX, int mouseY, float renderPartialTicks) {
      super(gui, stack, mouseX, mouseY, renderPartialTicks);
    }
  }
}
