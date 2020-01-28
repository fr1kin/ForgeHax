package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class ManualDeleteMod extends ToggleMod {
  
  public ManualDeleteMod() {
    super(Category.WORLD, "ManualEntityDelete", false,
        "Manually delete entities with middle click");
  }
  
  @SubscribeEvent
  public void onInput(InputEvent.MouseInputEvent event) {
    if (!isInWorld()) {
      return;
    }

    if (event.getButton() == 2) { // on middle click
      RayTraceResult aim = LocalPlayerUtils.getViewTrace();

      if(RayTraceResult.Type.ENTITY.equals(aim.getType()) && aim instanceof EntityRayTraceResult) {
        EntityRayTraceResult tr = (EntityRayTraceResult) aim;
        getWorld().removeEntityFromWorld(tr.getEntity().getEntityId());
      }
    }
  }
}
