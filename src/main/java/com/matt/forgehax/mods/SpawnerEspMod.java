package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Created on 9/29/2016 by fr1kin
 */
@RegisterMod
public class SpawnerEspMod extends ToggleMod {
  
  public SpawnerEspMod() {
    super(Category.RENDER, "SpawnerESP", false, "Spawner esp");
  }
  
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if(getWorld() == null) {
      return;
    }
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    
    for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
      if (tileEntity instanceof TileEntityMobSpawner) {
        GeometryTessellator.drawCuboid(
            event.getBuffer(), tileEntity.getPos(), GeometryMasks.Line.ALL, Colors.RED.toBuffer());
      }
    }
    
    event.getTessellator().draw();
  }
}
