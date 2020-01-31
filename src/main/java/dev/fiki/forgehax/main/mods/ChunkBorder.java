package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.tesselation.GeometryMasks;
import dev.fiki.forgehax.main.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    BlockPos from = new BlockPos(Globals.MC.player.chunkCoordX * 16, 0, Globals.MC.player.chunkCoordZ * 16);
    BlockPos to = new BlockPos(from.getX() + 15, 256, from.getZ() + 15);

    int color = Colors.YELLOW.toBuffer();
    GeometryTessellator.drawCuboid(event.getBuffer(), from, to, GeometryMasks.Line.ALL, color);

    event.getTessellator().draw();
  }

}

