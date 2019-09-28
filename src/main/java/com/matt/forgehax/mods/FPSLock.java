package com.matt.forgehax.mods;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.opengl.Display;

@RegisterMod
public class FPSLock extends ToggleMod {
  
  private final Setting<Integer> defaultFps =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("default-fps")
          .description("default FPS to revert to")
          .defaultTo(MC.gameSettings.limitFramerate)
          .min(1)
          .build();
  
  private final Setting<Integer> fps =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("fps")
          .description("FPS to use when the world is loaded. Set to 0 to disable.")
          .min(0)
          .defaultTo(0)
          .build();
  private final Setting<Integer> menu_fps =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("menu-fps")
          .description("FPS when the GUI is opened. Set to 0 to disable.")
          .min(0)
          .defaultTo(60)
          .build();
  
  private final Setting<Integer> no_focus_fps =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
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
    if (no_focus_fps.get() > 0 && !Display.isActive()) {
      return no_focus_fps.get();
    } else if (MC.currentScreen != null) {
      return menu_fps.get() > 0 ? menu_fps.get() : defaultFps.get();
    } else {
      return fps.get() > 0 ? fps.get() : defaultFps.get();
    }
  }
  
  @Override
  protected void onDisabled() {
    MC.gameSettings.limitFramerate = defaultFps.get();
  }
  
  @SubscribeEvent
  void onTick(ClientTickEvent event) {
    switch (event.phase) {
      case START:
        MC.gameSettings.limitFramerate = getFps();
        break;
      case END:
      default:
        break;
    }
  }
}
