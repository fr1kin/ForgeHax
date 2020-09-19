package com.matt.forgehax.mods;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.tesselation.GeometryMasks;
import com.matt.forgehax.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@RegisterMod
public class ChunkBorder extends ToggleMod {



  public ChunkBorder() {
    super(Category.RENDER, "ChunkBorder", false, "Shows a border at the border around the chunk you are in.");
  }

  /**
   * to draw the border
   * @param event
   */
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

    BlockPos from = new BlockPos(MC.player.chunkCoordX * 16, 0, MC.player.chunkCoordZ * 16);
    BlockPos to = new BlockPos(from.getX() + 15, 256, from.getZ() + 15);

    int color = Colors.YELLOW.toBuffer();
    GeometryTessellator.drawCuboid(event.getBuffer(), from, to, GeometryMasks.Line.ALL, color);

    event.getTessellator().draw();
  }

}

