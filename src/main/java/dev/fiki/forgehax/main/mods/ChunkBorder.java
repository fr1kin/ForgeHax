package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class ChunkBorder extends ToggleMod {
  public ChunkBorder() {
    super(Category.RENDER, "ChunkBorder", false, "Shows a border at the border around the chunk you are in.");
  }

  /**
   * to draw the border
   *
   * @param event
   */
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    BufferBuilderEx builder = event.getBuffer();
    builder.beginLines(DefaultVertexFormats.POSITION_COLOR);

    BlockPos from = new BlockPos(getLocalPlayer().chunkCoordX * 16, 0, getLocalPlayer().chunkCoordZ * 16);
    BlockPos to = new BlockPos(from.getX() + 15, 256, from.getZ() + 15);

    builder.putOutlinedCuboid(from, to, GeometryMasks.Line.ALL, Colors.YELLOW);

    builder.draw();
  }

}

