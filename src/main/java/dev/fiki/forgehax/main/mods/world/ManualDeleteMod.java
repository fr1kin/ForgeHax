package dev.fiki.forgehax.main.mods.world;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.MouseInputEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "ManualEntityDelete",
    description = "Manually delete entities with middle click",
    category = Category.WORLD
)
@ExtensionMethod({LocalPlayerEx.class})
public class ManualDeleteMod extends ToggleMod {

  @SubscribeListener
  public void onInput(MouseInputEvent event) {
    if (!isInWorld()) {
      return;
    }

    if (event.getButton() == 2) { // on middle click
      RayTraceResult aim = getLocalPlayer().getViewTrace();

      if (RayTraceResult.Type.ENTITY.equals(aim.getType()) && aim instanceof EntityRayTraceResult) {
        EntityRayTraceResult tr = (EntityRayTraceResult) aim;
        getWorld().removeEntity(tr.getEntity().getId());
      }
    }
  }
}
