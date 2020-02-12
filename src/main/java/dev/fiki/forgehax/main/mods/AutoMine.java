package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.BlockControllerProcessEvent;
import dev.fiki.forgehax.common.events.LeftClickCounterUpdateEvent;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.key.BindingHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;
import static dev.fiki.forgehax.main.Common.getGameSettings;

@RegisterMod
public class AutoMine extends ToggleMod {

  private boolean pressed = false;

  public AutoMine() {
    super(Category.PLAYER, "AutoMine", false, "Auto mine blocks");
  }

  private void setPressed(boolean state) {
    getGameSettings().keyBindAttack.setPressed(state);
    pressed = state;
  }

  @Override
  protected void onEnabled() {
    BindingHelper.disableContextHandler(getGameSettings().keyBindAttack);
  }

  @Override
  protected void onDisabled() {
    setPressed(false);
    BindingHelper.restoreContextHandler(getGameSettings().keyBindAttack);
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (!isInWorld()) {
      return;
    }

    switch (event.phase) {
      case START: {
        RayTraceResult tr = LocalPlayerUtils.getBlockViewTrace();

        if (RayTraceResult.Type.MISS.equals(tr.getType())) {
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
