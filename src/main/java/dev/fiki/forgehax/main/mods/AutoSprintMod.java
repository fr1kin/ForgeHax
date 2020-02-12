package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.EnumSetting;
import dev.fiki.forgehax.main.util.key.BindingHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;
import static dev.fiki.forgehax.main.Common.getGameSettings;

@RegisterMod
public class AutoSprintMod extends ToggleMod {

  private boolean isBound = false;

  enum Modes {
    ALWAYS,
    LEGIT
  }

  public final EnumSetting<Modes> mode = newEnumSetting(Modes.class)
      .name("mode")
      .description("Sprint mode")
      .defaultTo(Modes.ALWAYS)
      .build();

  public AutoSprintMod() {
    super(Category.PLAYER, "AutoSprint", false, "Automatically sprints");
  }

  private void startSprinting() {
    switch (mode.getValue()) {
      case ALWAYS:
        if (!getLocalPlayer().collidedHorizontally && !getLocalPlayer().isSprinting()) {
          getLocalPlayer().setSprinting(true);
        }
        break;
      default:
      case LEGIT:
        if (!isBound) {
          BindingHelper.disableContextHandler(getGameSettings().keyBindSprint);
          isBound = true;
        }
        if (!getGameSettings().keyBindSprint.isKeyDown()) {
          getGameSettings().keyBindSprint.setPressed(true);
        }
        break;
    }
  }

  private void stopSprinting() {
    if (isBound) {
      getGameSettings().keyBindSprint.setPressed(false);
      BindingHelper.restoreContextHandler(getGameSettings().keyBindSprint);
      isBound = false;
    }
  }

  /**
   * Stop sprinting when the mod is disabled
   */
  @Override
  public void onDisabled() {
    stopSprinting();
  }

  /**
   * Start sprinting every update tick
   */
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (event.getEntityLiving().moveForward > 0
        && !event.getEntityLiving().collidedHorizontally
        && !event.getEntityLiving().isCrouching()) {
      startSprinting();
    }
  }
}
