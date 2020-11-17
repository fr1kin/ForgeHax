package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.BufferBuilderEx;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.events.RenderEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "ChunkBorder",
    description = "Shows a border at the border around the chunk you are in",
    category = Category.RENDER
)
public class ChunkBorder extends ToggleMod {
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

