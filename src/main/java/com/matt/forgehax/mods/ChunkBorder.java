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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class ChunkBorder extends ToggleMod {

  private final Setting<Integer> minY =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("min-y")
          .description("From where the outline should be rendered.")
          .defaultTo(0)
          .build();

  private final Setting<Integer> maxY =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("max-y")
          .description("To where the outline should be rendered.")
          .defaultTo(255)
          .build();

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
    event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

    final int fromX = getLocalPlayer().chunkCoordX * 16;
    final int fromZ = getLocalPlayer().chunkCoordZ * 16;

    final int color = Colors.YELLOW.toBuffer();
    GeometryTessellator.drawCuboid(event.getBuffer(), fromX, minY.get(), fromZ, fromX + 15, maxY.get(), fromZ + 15, GeometryMasks.Line.ALL, color);

    event.getTessellator().draw();
  }

}

