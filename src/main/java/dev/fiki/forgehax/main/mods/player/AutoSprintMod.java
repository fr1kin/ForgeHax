package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.main.util.cmd.settings.EnumSetting;
import dev.fiki.forgehax.main.util.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.key.BindingHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import lombok.RequiredArgsConstructor;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (event.getEntityLiving().moveForward > 0
        && !event.getEntityLiving().collidedHorizontally
        && !event.getEntityLiving().isCrouching()
        && !freecam.isEnabled()) {
      startSprinting();
    }
  }
}
