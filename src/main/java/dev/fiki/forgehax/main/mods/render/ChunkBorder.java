package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "ChunkBorder",
    description = "Shows a border at the border around the chunk you are in",
    category = Category.RENDER
)
@ExtensionMethod({VectorEx.class, VertexBuilderEx.class})
public class ChunkBorder extends ToggleMod {
  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    val stack = event.getStack();
    val builder = event.getBuffer();
    stack.pushPose();

    builder.beginLines(DefaultVertexFormats.POSITION_COLOR);

    BlockPos from = new BlockPos(getLocalPlayer().xChunk * 16, 0, getLocalPlayer().zChunk * 16);
    BlockPos to = new BlockPos(from.getX() + 15, 256, from.getZ() + 15);

    builder.outlinedCube(from, to, GeometryMasks.Line.ALL, Colors.YELLOW, stack.getLastMatrix());

    builder.draw();
    stack.popPose();
  }
}

