package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.BlockControllerProcessEvent;
import dev.fiki.forgehax.common.events.LeftClickCounterUpdateEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.key.Bindings;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AutoMine extends ToggleMod {

  private boolean pressed = false;

  public AutoMine() {
    super(Category.PLAYER, "AutoMine", false, "Auto mine blocks");
  }

  private void setPressed(boolean state) {
    Bindings.attack.setPressed(state);
    pressed = state;
  }

  @Override
  protected void onEnabled() {
    Bindings.attack.bind();
  }

  @Override
  protected void onDisabled() {
    setPressed(false);
    Bindings.attack.unbind();
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (!Common.isInWorld()) {
      return;
    }

    switch (event.phase) {
      case START: {
        RayTraceResult tr = LocalPlayerUtils.getMouseOverBlockTrace();

        if (tr == null) {
          setPressed(false);
          return;
        }

        setPressed(true);
        break;
      }
      case END:
        setPressed(false);
        break;
    }
  }

  @SubscribeEvent
  public void onLeftClickCouterUpdate(LeftClickCounterUpdateEvent event) {
    // prevent the leftClickCounter from changing
    event.setCanceled(true);
  }

  @SubscribeEvent
  public void onBlockCounterUpdate(BlockControllerProcessEvent event) {
    // bug fix - left click is actually false after processing the key bindings
    // this will set that boolean to the correct value
    if (pressed) {
      event.setLeftClicked(true);
    }
  }
}
