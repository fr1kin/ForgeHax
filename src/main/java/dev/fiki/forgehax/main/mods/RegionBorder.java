package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
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
public class RegionBorder extends ToggleMod {


  private final IntegerSetting chunkDistance = newIntegerSetting()
      .name("chunk-distance")
      .description("how many chunks in front of the region the border should be drawn."
          + " I you don't want it just set it to 0 so it is like the normal region border.")
      .defaultTo(5)
      .build();

  private final BooleanSetting drawRegionBorder = newBooleanSetting()
      .name("draw-region-border")
      .description("whether you even want to draw the actual region border.")
      .defaultTo(true)
      .build();


  public RegionBorder() {
    super(Category.RENDER, "RegionBorder", false, "Shows a border in front of the edge of the region you are in.");
  }

  /**
   * to draw the border
   *
   * @param event
   */
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

    BlockPos from = new BlockPos((((int) Common.getLocalPlayer().getPosX()) / 512) * 512,
        0, (((int) Common.getLocalPlayer().getPosZ()) / 512) * 512);
    BlockPos to = from.add(511, 256, 511);

    int color = Colors.ORANGE.toBuffer();
    if (drawRegionBorder.getValue()) {
      GeometryTessellator.drawCuboid(event.getBuffer(), from, to, GeometryMasks.Line.ALL, color);
    }

    final int chunkDistanceSetting = chunkDistance.getValue() * 16;
    from = from.add(chunkDistanceSetting, 0, chunkDistanceSetting);
    to = to.add(-chunkDistanceSetting, 0, -chunkDistanceSetting);

    color = Colors.YELLOW.toBuffer();
    GeometryTessellator.drawCuboid(event.getBuffer(), from, to, GeometryMasks.Line.ALL, color);

    event.getTessellator().draw();
  }

}

