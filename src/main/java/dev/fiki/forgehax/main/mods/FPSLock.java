package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.PreClientTickEvent;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@RegisterMod
public class FPSLock extends ToggleMod {

  private final IntegerSetting defaultFps = newIntegerSetting()
      .name("default-fps")
      .description("default FPS to revert to")
      .defaultTo(Common.getGameSettings().framerateLimit)
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

  public FPSLock() {
    super(
        Category.MISC,
        "FPSLock",
        false,
        "Lock the fps to a lower-than-allowed value, and restore when disabled");
  }

  private int getFps() {
    if (no_focus_fps.getValue() > 0
        && GLFW.glfwGetWindowAttrib(Common.getMainWindow().getHandle(), GLFW.GLFW_FOCUSED) == GLFW.GLFW_FALSE) {
      return no_focus_fps.getValue();
    } else if (Common.MC.currentScreen != null) {
      return menu_fps.getValue() > 0 ? menu_fps.getValue() : defaultFps.getValue();
    } else {
      return fps.getValue() > 0 ? fps.getValue() : defaultFps.getValue();
    }
  }

  @Override
  protected void onDisabled() {
    Common.getGameSettings().framerateLimit = defaultFps.getValue();
  }

  @SubscribeEvent
  void onTick(PreClientTickEvent event) {
    Common.getGameSettings().framerateLimit = getFps();
  }
}
