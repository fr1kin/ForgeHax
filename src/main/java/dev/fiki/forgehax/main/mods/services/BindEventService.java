package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import static dev.fiki.forgehax.main.Common.*;
import static dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting.*;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class BindEventService extends ServiceMod {

  public BindEventService() {
    super("BindEventService");
  }

  private void updateBindings(KeyBindingSetting setting, int keyCode, int keyAction) {
    if (keyCode == setting.getKeyCode()) {
//      int pressTime = FastReflection.Fields.KeyBinding_pressTime.get(setting.getKeyBinding());
      switch (keyAction) {
        case GLFW.GLFW_PRESS:
        case GLFW.GLFW_REPEAT:
          setting.getKeyBinding().setPressed(true);
          if(setting.getKeyBinding().isPressed()) {
            setting.getListeners(IKeyPressedListener.class)
                .forEach(l -> l.onKeyPressed(setting.getKeyBinding()));
          } else if(setting.isKeyDown()) {
            setting.getListeners(IKeyDownListener.class)
                .forEach(l -> l.onKeyDown(setting.getKeyBinding()));
          }
          break;
        case GLFW.GLFW_RELEASE:
          setting.getKeyBinding().setPressed(false);
          setting.getListeners(IKeyReleasedListener.class)
              .forEach(l -> l.onKeyReleased(setting.getKeyBinding()));
          break;
      }
    }
  }

  @SubscribeEvent
  public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    for (KeyBindingSetting setting : getRegistry()) {
      if(InputMappings.Type.KEYSYM.equals(setting.getKeyInput().getType())) {
        updateBindings(setting, event.getKey(), event.getAction());
      }
    }
  }

  @SubscribeEvent
  public void onMouseEvent(InputEvent.MouseInputEvent event) {
    for (KeyBindingSetting setting : getRegistry()) {
      if(InputMappings.Type.MOUSE.equals(setting.getKeyInput().getType())) {
        updateBindings(setting, event.getButton(), event.getAction());
      }
    }
  }
}
