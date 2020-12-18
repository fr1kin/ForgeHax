package dev.fiki.forgehax.api.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

public class GuiContainerRenderEvent extends GuiRenderEvent {
  GuiContainerRenderEvent(Screen gui, MatrixStack stack, int mouseX, int mouseY, float renderPartialTicks) {
    super(gui, stack, mouseX, mouseY, renderPartialTicks);
  }

  public ContainerScreen getContainerScreen() {
    return (ContainerScreen) getGui();
  }

  public static class Background extends GuiContainerRenderEvent {
    public Background(Screen gui, MatrixStack stack, int mouseX, int mouseY, float renderPartialTicks) {
      super(gui, stack, mouseX, mouseY, renderPartialTicks);
    }
  }
}
