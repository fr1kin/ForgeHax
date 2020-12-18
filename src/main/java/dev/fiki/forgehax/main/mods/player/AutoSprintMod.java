package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.key.BindingHelper;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.RequiredArgsConstructor;

import static dev.fiki.forgehax.main.Common.getGameSettings;
import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "AutoSprint",
    description = "Automatically sprints",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AutoSprintMod extends ToggleMod {
  private final FreecamMod freecam;

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
  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (event.getPlayer().moveForward > 0
        && !event.getPlayer().collidedHorizontally
        && !event.getPlayer().isCrouching()
        && !freecam.isEnabled()) {
      startSprinting();
    }
  }
}
