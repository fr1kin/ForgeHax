package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.cmd.settings.EnumSetting;
import dev.fiki.forgehax.main.util.key.Bindings;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        if (!Common.getLocalPlayer().collidedHorizontally && !Common.getLocalPlayer().isSprinting()) {
          Common.getLocalPlayer().setSprinting(true);
        }
        break;
      default:
      case LEGIT:
        if (!isBound) {
          Bindings.sprint.bind();
          isBound = true;
        }
        if (!Bindings.sprint.getBinding().isKeyDown()) {
          Bindings.sprint.setPressed(true);
        }
        break;
    }
  }

  private void stopSprinting() {
    if (isBound) {
      Bindings.sprint.setPressed(false);
      Bindings.sprint.unbind();
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
