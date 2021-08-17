package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import org.lwjgl.glfw.GLFW;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "FPSLock",
    description = "Lock the fps to a lower-than-allowed value, and restore when disabled",
    category = Category.MISC
)
public class FPSLock extends ToggleMod {

  private final IntegerSetting defaultFps = newIntegerSetting()
      .name("default-fps")
      .description("default FPS to revert to")
      .defaultTo(60)
      .min(1)
      .build();

  private final IntegerSetting fps = newIntegerSetting()
      .name("fps")
      .description("FPS to use when the world is loaded. Set to 0 to disable.")
      .min(0)
      .defaultTo(0)
      .build();

  private final IntegerSetting menu_fps = newIntegerSetting()
      .name("menu-fps")
      .description("FPS when the GUI is opened. Set to 0 to disable.")
      .min(0)
      .defaultTo(60)
      .build();

  private final IntegerSetting no_focus_fps = newIntegerSetting()
      .name("no-focus-fps")
      .description("FPS when the game window doesn't have focus. Set to 0 to disable.")
      .min(0)
      .defaultTo(3)
      .build();

  private int getFps() {
    if (no_focus_fps.getValue() > 0
        && GLFW.glfwGetWindowAttrib(getMainWindow().getWindow(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_FALSE) {
      return no_focus_fps.getValue();
    } else if (getDisplayScreen() != null) {
      return menu_fps.getValue() > 0 ? menu_fps.getValue() : defaultFps.getValue();
    } else {
      return fps.getValue() > 0 ? fps.getValue() : defaultFps.getValue();
    }
  }

  @Override
  protected void onDisabled() {
    getMainWindow().setFramerateLimit(getGameSettings().framerateLimit);
  }

  @SubscribeListener
  void onTick(PreGameTickEvent event) {
    getMainWindow().setFramerateLimit(getFps());
  }
}
