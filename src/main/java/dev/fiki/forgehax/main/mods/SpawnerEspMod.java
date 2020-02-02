package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.tesselation.GeometryMasks;
import dev.fiki.forgehax.main.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    
    for (TileEntity tileEntity : Common.getWorld().loadedTileEntityList) {
      if (tileEntity instanceof MobSpawnerTileEntity) {
        BlockPos pos = tileEntity.getPos();
        GeometryTessellator.drawCuboid(
            event.getBuffer(), pos, GeometryMasks.Line.ALL, Colors.RED.toBuffer());
      }
    }
    
    event.getTessellator().draw();
  }
}
