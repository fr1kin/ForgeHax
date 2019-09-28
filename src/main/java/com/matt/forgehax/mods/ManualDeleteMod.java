package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

@RegisterMod
public class ManualDeleteMod extends ToggleMod {
  
  public ManualDeleteMod() {
    super(Category.WORLD, "ManualEntityDelete", false,
        "Manually delete entities with middle click");
  }
  
  @SubscribeEvent
  public void onInput(MouseEvent event) {
    if (getWorld() == null || getLocalPlayer() == null) {
      return;
    }
    
    if (event.getButton() == 2 && Mouse.getEventButtonState()) { // on middle click
      RayTraceResult aim = MC.objectMouseOver;
      if (aim == null) {
        return;
      }
      if (aim.typeOfHit == RayTraceResult.Type.ENTITY) {
        if (aim.entityHit != null) {
          MC.world.removeEntity(aim.entityHit);
        }
      }
    }
  }
}
