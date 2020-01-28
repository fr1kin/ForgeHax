package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static com.matt.forgehax.Globals.*;

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
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    
    for (TileEntity tileEntity : getWorld().loadedTileEntityList) {
      if (tileEntity instanceof MobSpawnerTileEntity) {
        BlockPos pos = tileEntity.getPos();
        GeometryTessellator.drawCuboid(
            event.getBuffer(), pos, GeometryMasks.Line.ALL, Colors.RED.toBuffer());
      }
    }
    
    event.getTessellator().draw();
  }
}
