package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getWorld;
import static dev.fiki.forgehax.main.Common.isInWorld;

@RegisterMod(
    name = "ManualEntityDelete",
    description = "Manually delete entities with middle click",
    category = Category.WORLD
)
public class ManualDeleteMod extends ToggleMod {

  @SubscribeEvent
  public void onInput(InputEvent.MouseInputEvent event) {
    if (!isInWorld()) {
      return;
    }

    if (event.getButton() == 2) { // on middle click
      RayTraceResult aim = LocalPlayerUtils.getViewTrace();

      if (RayTraceResult.Type.ENTITY.equals(aim.getType()) && aim instanceof EntityRayTraceResult) {
        EntityRayTraceResult tr = (EntityRayTraceResult) aim;
        getWorld().removeEntityFromWorld(tr.getEntity().getEntityId());
      }
    }
  }
}
