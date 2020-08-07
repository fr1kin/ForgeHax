package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.key.BindingHelper;
import dev.fiki.forgehax.main.util.key.KeyBindingEx;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import static dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting.*;

@RegisterMod
public class BindEventService extends ServiceMod {
  private boolean bindConfigLoaded = false;

  private void updateBindings(KeyBindingSetting setting, int keyAction) {
    final KeyBindingEx key = setting.getKeyBinding();
    switch (keyAction) {
      case GLFW.GLFW_PRESS:
        key.setPressed(true);
        setting.getListeners(IKeyPressedListener.class)
            .forEach(l -> l.onKeyPressed(key));
        break;
      case GLFW.GLFW_REPEAT:
        key.setPressed(true);
        setting.getListeners(IKeyDownListener.class)
            .forEach(l -> l.onKeyDown(key));
        break;
      case GLFW.GLFW_RELEASE:
        key.setPressed(false);
        setting.getListeners(IKeyReleasedListener.class)
            .forEach(l -> l.onKeyReleased(key));
        break;
    }
  }

  @SubscribeEvent
  public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    for (KeyBindingSetting setting : getRegistry()) {
      if (InputMappings.Type.KEYSYM.equals(setting.getKeyInput().getType())
          && setting.getKeyBinding().matchesKey(event.getKey(), event.getScanCode())
          && setting.getKeyBinding().checkConflicts()) {
        updateBindings(setting, event.getAction());
      }
    }
  }

  @SubscribeEvent
  public void onMouseEvent(InputEvent.MouseInputEvent event) {
    for (KeyBindingSetting setting : getRegistry()) {
      if (InputMappings.Type.MOUSE.equals(setting.getKeyInput().getType())
          && setting.getKeyCode() == event.getButton()
          && setting.getKeyBinding().checkConflicts()) {
        updateBindings(setting, event.getAction());
      }
    }
  }

  @SubscribeEvent
  public void onGuiOpened(GuiOpenEvent event) {
    if (!bindConfigLoaded && event.getGui() instanceof MainMenuScreen) {
      bindConfigLoaded = true;
      // TODO: load config
    }
  }

  @SubscribeEvent
  public void onPacketOutgoing(PacketOutboundEvent event) {
    if(BindingHelper.isSuppressingSettingsPacket()
        && event.getPacket() instanceof CClientSettingsPacket) {
      event.setCanceled(false);
    }
  }
}
