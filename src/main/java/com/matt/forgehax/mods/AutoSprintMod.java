package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoSprintMod extends ToggleMod {
  
  private boolean isBound = false;
  
  enum Modes {
    ALWAYS,
    LEGIT
  }
  
  public final Setting<Modes> mode =
      getCommandStub()
          .builders()
          .<Modes>newSettingEnumBuilder()
          .name("mode")
          .description("Sprint mode")
          .defaultTo(Modes.ALWAYS)
          .build();
  
  public AutoSprintMod() {
    super(Category.MOVEMENT, "AutoSprint", false, "Automatically sprints");
  }
  
  private void startSprinting() {
    switch (mode.get()) {
      case ALWAYS:
        if (!getLocalPlayer().collidedHorizontally && !getLocalPlayer().isSprinting()) {
          getLocalPlayer().setSprinting(true);
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
        && !event.getEntityLiving().isSneaking()) {
      startSprinting();
    }
  }
}
