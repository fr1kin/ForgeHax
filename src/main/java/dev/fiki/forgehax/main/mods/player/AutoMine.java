package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PostGameTickEvent;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.key.BindingHelper;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.game.BlockControllerProcessEvent;
import dev.fiki.forgehax.asm.events.game.LeftClickCounterUpdateEvent;
import lombok.experimental.ExtensionMethod;
import net.minecraft.util.math.RayTraceResult;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "AutoMine",
    description = "Auto mine blocks",
    category = Category.PLAYER
)
@ExtensionMethod({LocalPlayerEx.class})
public class AutoMine extends ToggleMod {

  private boolean pressed = false;

  private void setPressed(boolean state) {
    getGameSettings().keyAttack.setDown(state);
    pressed = state;
  }

  @Override
  protected void onEnabled() {
    BindingHelper.disableContextHandler(getGameSettings().keyAttack);
  }

  @Override
  protected void onDisabled() {
    setPressed(false);
    BindingHelper.restoreContextHandler(getGameSettings().keyAttack);
  }

  @SubscribeListener
  public void onTick(PreGameTickEvent event) {
    if (isInWorld()) {
      RayTraceResult tr = getLocalPlayer().getBlockViewTrace();

      if (RayTraceResult.Type.MISS.equals(tr.getType())) {
        setPressed(false);
        return;
      }

      setPressed(true);
    }
  }

  @SubscribeListener
  public void onTick(PostGameTickEvent event) {
    if (isInWorld()) {
      setPressed(false);
    }
  }

  @SubscribeListener
  public void onLeftClickCouterUpdate(LeftClickCounterUpdateEvent event) {
    // prevent the leftClickCounter from changing
    event.setCanceled(true);
  }

  @SubscribeListener
  public void onBlockCounterUpdate(BlockControllerProcessEvent event) {
    // bug fix - left click is actually false after processing the key bindings
    // this will set that boolean to the correct value
    if (pressed) {
      event.setLeftClicked(true);
    }
  }
}
