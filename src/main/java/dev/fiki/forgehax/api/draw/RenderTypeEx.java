package dev.fiki.forgehax.api.draw;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class RenderTypeEx extends RenderType {
  private static final RenderType GL_LINES = RenderType.create("fh_lines",
      DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
      RenderType.State.builder()
          .setLineState(new RenderState.LineState(OptionalDouble.of(1.f)))
          .setCullState(RenderState.NO_CULL)
          .setDepthTestState(RenderState.NO_DEPTH_TEST)
          .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
          .setFogState(RenderState.NO_FOG)
          .createCompositeState(false)
  );

  private static final RenderType GL_LINE_LOOP = RenderType.create("fh_line_loop",
      DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINE_LOOP, 256,
      RenderType.State.builder()
          .setLineState(new RenderState.LineState(OptionalDouble.empty()))
          .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
          .setFogState(RenderState.NO_FOG)
          .createCompositeState(false)
  );

  private static final RenderType GL_TRIANGLES = RenderType.create("fh_triangles",
      DefaultVertexFormats.POSITION_COLOR, GL11.GL_TRIANGLES, 256,
      RenderType.State.builder()
          .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
          .setFogState(RenderState.NO_FOG)
          .createCompositeState(false)
  );

  private static final RenderType GL_QUADS = RenderType.create("fh_quads",
      DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
      RenderType.State.builder()
          .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
          .setFogState(RenderState.NO_FOG)
          .createCompositeState(false)
  );

  private static final RenderType BLOCK_TRANSLUCENT_CULL = entityTranslucentCull(PlayerContainer.BLOCK_ATLAS);

  private static final RenderType BLOCK_CUTOUT = entityCutout(PlayerContainer.BLOCK_ATLAS);

  public static RenderType glLines() {
    return GL_LINES;
  }

  public static RenderType glLineLoop() {
    return GL_LINE_LOOP;
  }

  public static RenderType glTriangle() {
    return GL_TRIANGLES;
  }

  public static RenderType glQuads() {
    return GL_QUADS;
  }

  public static RenderType blockTranslucentCull() {
    return BLOCK_TRANSLUCENT_CULL;
  }

  public static RenderType blockCutout() {
    return BLOCK_CUTOUT;
  }

  public static RenderType entityTranslucentCull(ResourceLocation texture) {
    RenderType.State state = RenderType.State.builder()
        .setTextureState(new RenderState.TextureState(texture, false, false))
        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
        .setDiffuseLightingState(RenderState.NO_DIFFUSE_LIGHTING)
        .setAlphaState(DEFAULT_ALPHA)
        .setLightmapState(RenderState.NO_LIGHTMAP)
        .setOverlayState(RenderState.OVERLAY)
        .createCompositeState(true);
    return create("fh_entity_translucent_cull", DefaultVertexFormats.NEW_ENTITY,
        GL11.GL_QUADS, 256, true, true, state);
  }

  public static RenderType entityCutout(ResourceLocation texture) {
    RenderType.State state = RenderType.State.builder()
        .setTextureState(new RenderState.TextureState(texture, false, false))
        .setTransparencyState(NO_TRANSPARENCY)
        .setDiffuseLightingState(RenderState.NO_DIFFUSE_LIGHTING)
        .setAlphaState(DEFAULT_ALPHA)
        .setLightmapState(RenderState.NO_LIGHTMAP)
        .setOverlayState(RenderState.OVERLAY)
        .createCompositeState(true);
    return create("fh_entity_cutout", DefaultVertexFormats.NEW_ENTITY,
        GL11.GL_QUADS, 256, true, false, state);
  }

  private RenderTypeEx(String id,
      VertexFormat vertexFormat,
      int glMode, int bufferSize,
      boolean useDelegate, boolean needsSorting,
      Runnable enableTask, Runnable disableTask) {
    super(id, vertexFormat, glMode, bufferSize, useDelegate, needsSorting, enableTask, disableTask);
  }
}
