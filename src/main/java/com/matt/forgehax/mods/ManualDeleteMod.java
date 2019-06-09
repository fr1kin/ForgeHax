package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@RegisterMod
public class ManualDeleteMod extends ToggleMod {

  public ManualDeleteMod() {
    super(Category.WORLD, "ManualEntityDelete", false, "Manually delete entities with middle click");
  }

  @SubscribeEvent
  public void onInput(InputEvent.MouseInputEvent event) {
    if(getWorld() == null || getLocalPlayer() == null)
      return;

    if (event.getButton() == 2 && event.getAction() == GLFW.GLFW_PRESS) { // on middle click
      RayTraceResult aim = MC.objectMouseOver;
      if (aim == null) return;
      if (aim.type == RayTraceResult.Type.ENTITY) {
        if (aim.entity != null) {
          MC.world.removeEntity(aim.entity);
        }
      }
    }
  }
}