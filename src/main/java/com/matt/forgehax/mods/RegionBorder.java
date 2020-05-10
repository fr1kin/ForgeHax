package com.matt.forgehax.mods;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
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
public class RegionBorder extends ToggleMod {


  private final Setting<Integer> chunkDistance =
    getCommandStub()
      .builders()
      .<Integer>newSettingBuilder()
      .name("chunk-distance")
      .description("how many chunks in front of the region the border should be drawn. I you don't want it just set it to 0 so it is like the normal region border.")
      .defaultTo(5)
      .build();

  private final Setting<Boolean> drawRegionBorder = getCommandStub().builders().<Boolean>newSettingBuilder()
    .name("draw-region-border")
    .description("whether you even want to draw the actual region border.")
    .defaultTo(true)
    .build();


  public RegionBorder() {
    super(Category.RENDER, "RegionBorder", false, "Shows a border in front of the edge of the region you are in.");
  }

  /**
   * to draw the border
   * @param event
   */
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

    BlockPos from = new BlockPos((((int) MC.player.posX) / 512) * 512, 0, (((int) MC.player.posZ) / 512) * 512);
    BlockPos to = from.add(511, 256, 511);

    int color = Colors.ORANGE.toBuffer();
    if(drawRegionBorder.getAsBoolean()) {
      GeometryTessellator.drawCuboid(event.getBuffer(), from, to, GeometryMasks.Line.ALL, color);
    }

    final int chunkDistanceSetting = chunkDistance.getAsInteger() * 16;
    from = from.add(chunkDistanceSetting, 0, chunkDistanceSetting);
    to = to.add(-chunkDistanceSetting, 0, -chunkDistanceSetting);

    color = Colors.YELLOW.toBuffer();
    GeometryTessellator.drawCuboid(event.getBuffer(), from, to, GeometryMasks.Line.ALL, color);

    event.getTessellator().draw();
  }

}

