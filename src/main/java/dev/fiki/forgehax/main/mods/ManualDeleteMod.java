package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class ManualDeleteMod extends ToggleMod {
  
  public ManualDeleteMod() {
    super(Category.WORLD, "ManualEntityDelete", false,
        "Manually delete entities with middle click");
  }
  
  @SubscribeEvent
  public void onInput(InputEvent.MouseInputEvent event) {
    if (!Globals.isInWorld()) {
      return;
    }

    if (event.getButton() == 2) { // on middle click
      RayTraceResult aim = LocalPlayerUtils.getViewTrace();

      if(RayTraceResult.Type.ENTITY.equals(aim.getType()) && aim instanceof EntityRayTraceResult) {
        EntityRayTraceResult tr = (EntityRayTraceResult) aim;
        Globals.getWorld().removeEntityFromWorld(tr.getEntity().getEntityId());
      }
    }
  }
}
