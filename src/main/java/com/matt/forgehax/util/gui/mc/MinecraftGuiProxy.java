package com.matt.forgehax.util.gui.mc;

import com.google.common.collect.Maps;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Created on 9/12/2017 by fr1kin */
public class MinecraftGuiProxy extends GuiScreen implements Globals {
  private final IGuiBase element;

  private final Map<Integer, InputEntry> inputs = Maps.newHashMap();

  public MinecraftGuiProxy(IGuiBase element) {
    this.element = element;
    this.allowUserInput = true;
  }

  @Override
  public void initGui() {
    inputs.clear();

    ScaledResolution resolution = new ScaledResolution(MC);
    element.init(resolution.getScaledWidth_double(), resolution.getScaledHeight_double());
  }

  @Override
  public void handleKeyboardInput() throws IOException {
    int keyCode = Keyboard.getEventKey();
    if (keyCode == Keyboard.KEY_NONE || keyCode >= Keyboard.KEYBOARD_SIZE)
      return; // unknown key or key index greater than what lwjgl supports

    if (keyCode == Keyboard.KEY_ESCAPE) {
      MC.displayGuiScreen(null);
      return;
    }

    InputEntry entry =
        inputs.computeIfAbsent(keyCode, i -> new InputEntry(InputEntry.KEYBOARD, keyCode));

    boolean down = Keyboard.getEventKeyState();
    long currentTimeMS = System.currentTimeMillis();

    GuiKeyEvent.Type type;
    if (entry.getTicks() < 0) { // initially pressed
      if (!down)
        return; // stop executing if key hasnt been initially pressed AND the key is being released
      // (double release event)
      type = GuiKeyEvent.Type.PRESSED;
      entry.setTimePressed(currentTimeMS); // update last click time
    } else {
      type = down ? GuiKeyEvent.Type.DOWN : GuiKeyEvent.Type.RELEASED;
    }

    // update ticks
    entry.incrementTicks();

    element.onKeyEvent(
        new GuiKeyEvent(
            type,
            keyCode,
            entry.getTicks(),
            entry.getTime(),
            currentTimeMS - entry.getTimePressed()));

    if (type == GuiKeyEvent.Type.RELEASED) {
      entry.setTime(entry.getTimePressed());
      entry.setTimePressed(-1);
      entry.resetTicks(); // reset ticks after event
    }

    super.handleKeyboardInput();
  }

  @Override
  public void handleMouseInput() throws IOException {
    int mouseCode = Mouse.getEventButton();
    if (mouseCode < 0 || mouseCode >= Mouse.getButtonCount())
      return; // only allow number of buttons supported by mouse (for mouse event, 0 is not an
    // unknown button)

    InputEntry entry =
        inputs.computeIfAbsent(
            Keyboard.KEYBOARD_SIZE + 1 + mouseCode,
            i -> new InputEntry(InputEntry.MOUSE, mouseCode));

    boolean down = Mouse.getEventButtonState();
    long currentTimeMS = System.currentTimeMillis();

    GuiMouseEvent.Type type;
    if (entry.getTicks() < 0) { // initially pressed
      if (!down)
        return; // stop executing if key hasnt been initially pressed AND the key is being released
      // (double release event)
      type = GuiMouseEvent.Type.PRESSED;
      entry.setTimePressed(currentTimeMS); // update last click time
    } else {
      type = down ? GuiMouseEvent.Type.DOWN : GuiMouseEvent.Type.RELEASED;
    }

    // update ticks
    entry.incrementTicks();

    int[] mpos = getMousePos();
    element.onMouseEvent(
        new GuiMouseEvent(
            type,
            mouseCode,
            mpos[0],
            mpos[1],
            Mouse.getEventDWheel(),
            entry.getTime(),
            currentTimeMS - entry.getTimePressed()));

    if (type == GuiMouseEvent.Type.RELEASED) {
      entry.setTime(entry.getTimePressed());
      entry.setTimePressed(-1);
      entry.resetTicks(); // reset ticks after event
    }

    super.handleMouseInput();
  }

  @Override
  public void updateScreen() {
    int[] mpos = getMousePos();
    element.onUpdate(new GuiUpdateEvent(mpos[0], mpos[1]));
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    GlStateManager.pushMatrix();
    SurfaceBuilder.enableBlend();
    SurfaceBuilder.disableTexture2D();

    element.onRender(new GuiRenderEvent(Tessellator.getInstance(), partialTicks, mouseX, mouseY));

    SurfaceBuilder.disableBlend();
    SurfaceBuilder.enableTexture2D();
    GlStateManager.popMatrix();
  }

  private static int[] getMousePos() {
    ScaledResolution resolution = new ScaledResolution(MC);
    int width = resolution.getScaledWidth();
    int height = resolution.getScaledHeight();
    int mouseX = Mouse.getX() * width / MC.displayWidth;
    int mouseY = height - Mouse.getY() * height / MC.displayHeight - 1;
    return new int[] {mouseX, mouseY};
  }

  private static class InputEntry {
    private static final int KEYBOARD = 0;
    private static final int MOUSE = 1;

    private final int type;
    private final int code;

    private int ticks = -1;
    private long time = -1;

    private long timePressed = -1;

    public InputEntry(int type, int code) {
      this.type = type;
      this.code = code;
    }

    public int getTicks() {
      return ticks;
    }

    public void incrementTicks() {
      ++ticks;
    }

    public void resetTicks() {
      ticks = -1;
    }

    public long getTime() {
      return time;
    }

    public void setTime(long time) {
      this.time = time;
    }

    public long getTimePressed() {
      return timePressed;
    }

    public void setTimePressed(long timePressed) {
      this.timePressed = timePressed;
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof InputEntry
              && type == ((InputEntry) obj).type
              && code == ((InputEntry) obj).code);
    }
  }
}
