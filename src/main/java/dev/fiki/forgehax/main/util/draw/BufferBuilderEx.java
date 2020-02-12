package dev.fiki.forgehax.main.util.draw;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import dev.fiki.forgehax.main.util.color.Color;
import lombok.Getter;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;

import static dev.fiki.forgehax.main.Common.getFontRenderer;

@Getter
public class BufferBuilderEx extends BufferBuilder {
  private final Tessellator tessellator;
  private final BufferBuilder original;

  private double offsetX;
  private double offsetY;
  private double offsetZ;

  private Matrix4f matrix = null;

  private IRenderTypeBuffer renderTypeBuffer = null;

  public BufferBuilderEx(Tessellator tessellator, BufferBuilder original) {
    super(0);
    this.tessellator = tessellator;
    this.original = original;
  }

  public BufferBuilderEx(Tessellator tessellator) {
    this(tessellator, tessellator.getBuffer());
  }

  private void onDrawingFinished() {
    offsetX = offsetY = offsetZ = 0.d;
    matrix = null;
    renderTypeBuffer = null;
  }

  public BufferBuilderEx setTranslation(double x, double y, double z) {
    offsetX = x;
    offsetY = y;
    offsetZ = z;
    return this;
  }

  public BufferBuilderEx setTranslation(Vec3d pos) {
    return setTranslation(pos.getX(), pos.getY(), pos.getZ());
  }

  public BufferBuilderEx setTranslation(Vec3i pos) {
    return setTranslation(pos.getX(), pos.getY(), pos.getZ());
  }

  public Matrix4f getMatrixOrIdentity() {
    return matrix == null ? TransformationMatrix.identity().getMatrix() : matrix;
  }

  public BufferBuilderEx setMatrix(Matrix4f matrix) {
    this.matrix = matrix;
    return this;
  }

  public IRenderTypeBuffer getOrCreateRenderTypeBuffer() {
    return renderTypeBuffer == null
        ? renderTypeBuffer = IRenderTypeBuffer.getImpl(this)
        : renderTypeBuffer;
  }

  public BufferBuilderEx setRenderTypeBuffer(IRenderTypeBuffer renderTypeBuffer) {
    this.renderTypeBuffer = renderTypeBuffer;
    return this;
  }

  public BufferBuilderEx appendLine(float startX, float startY, float endX, float endY, @Nullable Color color) {
    pos(startX, startY, 0.0D).color(color).endVertex();
    pos(endX, endY, 0.0D).color(color).endVertex();
    return this;
  }

  public BufferBuilderEx appendLine(float startX, float startY, float endX, float endY) {
    return appendLine(startX, startY, endX, endY, null);
  }

  public BufferBuilderEx appendRect(float x, float y, float w, float h, @Nullable Color color) {
    pos(x, y, 0.0D).color(color).endVertex();
    pos(x, y + h, 0.0D).color(color).endVertex();
    pos(x + w, y + h, 0.0D).color(color).endVertex();
    pos(x + w, y, 0.0D).color(color).endVertex();
    return this;
  }

  public BufferBuilderEx appendRect(float x, float y, float w, float h) {
    return appendRect(x, y, w, h, null);
  }

  public BufferBuilderEx appendTexturedRect(float x, float y,
      float textureX, float textureY,
      float width, float height,
      float depth, @Nullable Color color) {
    pos(x, y + height, depth)
        .tex(textureX + 0, textureY + height)
        .color(color)
        .endVertex();
    pos(x + width, y + height, depth)
        .tex(textureX + width, textureY + height)
        .color(color)
        .endVertex();
    pos(x + width, y + 0, depth)
        .tex(textureX + width, textureY + 0)
        .color(color)
        .endVertex();
    pos(x + 0, y + 0, depth)
        .tex(textureX + 0, textureY + 0)
        .color(color)
        .endVertex();
    return this;
  }

  public BufferBuilderEx appendTexturedRect(float x, float y,
      float textureX, float textureY,
      float width, float height,
      float depth) {
    return appendTexturedRect(x, y, textureX, textureY, width, height, depth, null);
  }

  public void appendGradientRect(float x, float y,
      float x2, float y2,
      Color outlineColor, Color shadeColor) {
    pos(x2, y, 0).color(outlineColor).endVertex();
    pos(x, y, 0).color(outlineColor).endVertex();
    pos(x, y2, 0).color(shadeColor).endVertex();
    pos(x2, y2, 0).color(shadeColor).endVertex();
  }

  /**
   * If you use any of these make sure you call ::finishString() at the end!
   */
  public BufferBuilderEx appendString(@Nonnull IRenderTypeBuffer buffer,
      @Nonnull String text,
      float x, float y,
      @Nonnull Color color,
      boolean shadow) {
    getFontRenderer().renderString(text,
        (int) x, (int) y,
        color.toBuffer(), shadow,
        getMatrixOrIdentity(), buffer,
        false, 0, 15728880);
    return this;
  }

  public BufferBuilderEx appendString(@Nonnull String text,
      float x, float y,
      @Nonnull Color color,
      boolean shadow) {
    return appendString(getOrCreateRenderTypeBuffer(), text, x, y, color, shadow);
  }

  public BufferBuilderEx appendString(@Nonnull String text,
      float x, float y,
      @Nonnull Color color) {
    return appendString(text, x, y, color, false);
  }

  public BufferBuilderEx appendStringWithShadow(@Nonnull String text,
      float x, float y,
      @Nonnull Color color) {
    return appendString(text, x, y, color, true);
  }

  public void finishString() {
    if (renderTypeBuffer != null && renderTypeBuffer instanceof IRenderTypeBuffer.Impl) {
      ((IRenderTypeBuffer.Impl) renderTypeBuffer).finish();
    }
  }

  public BufferBuilderEx appendFilledCuboid(final double x0, final double y0, final double z0,
      final double x1, final double y1, final double z1,
      final int sides, Color color) {
    if ((sides & GeometryMasks.Quad.DOWN) != 0) {
      pos(x1, y0, z0).color(color).endVertex();
      pos(x1, y0, z1).color(color).endVertex();
      pos(x0, y0, z1).color(color).endVertex();
      pos(x0, y0, z0).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Quad.UP) != 0) {
      pos(x1, y1, z0).color(color).endVertex();
      pos(x0, y1, z0).color(color).endVertex();
      pos(x0, y1, z1).color(color).endVertex();
      pos(x1, y1, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Quad.NORTH) != 0) {
      pos(x1, y0, z0).color(color).endVertex();
      pos(x0, y0, z0).color(color).endVertex();
      pos(x0, y1, z0).color(color).endVertex();
      pos(x1, y1, z0).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
      pos(x0, y0, z1).color(color).endVertex();
      pos(x1, y0, z1).color(color).endVertex();
      pos(x1, y1, z1).color(color).endVertex();
      pos(x0, y1, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Quad.WEST) != 0) {
      pos(x0, y0, z0).color(color).endVertex();
      pos(x0, y0, z1).color(color).endVertex();
      pos(x0, y1, z1).color(color).endVertex();
      pos(x0, y1, z0).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Quad.EAST) != 0) {
      pos(x1, y0, z1).color(color).endVertex();
      pos(x1, y0, z0).color(color).endVertex();
      pos(x1, y1, z0).color(color).endVertex();
      pos(x1, y1, z1).color(color).endVertex();
    }

    return this;
  }

  public BufferBuilderEx appendFilledCuboid(Vec3d start, Vec3d finish,
      final int sides, Color color) {
    return appendFilledCuboid(start.getX(), start.getY(), start.getZ(),
        finish.getX(), finish.getY(), finish.getZ(), sides, color);
  }

  public BufferBuilderEx appendFilledCuboid(Vec3i start, Vec3i finish,
      final int sides, Color color) {
    return appendFilledCuboid(start.getX(), start.getY(), start.getZ(),
        finish.getX(), finish.getY(), finish.getZ(), sides, color);
  }

  public BufferBuilderEx appendFilledCuboid(AxisAlignedBB bb,
      final int sides, Color color) {
    return appendFilledCuboid(bb.minX, bb.minY, bb.maxX,
        bb.maxX, bb.maxY, bb.maxZ, sides, color);
  }

  public BufferBuilderEx appendOutlinedCuboid(double x0, double y0, double z0,
      double x1, double y1, double z1,
      final int sides, Color color) {
    if ((sides & GeometryMasks.Line.DOWN_WEST) != 0) {
      pos(x0, y0, z0).color(color).endVertex();
      pos(x0, y0, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.UP_WEST) != 0) {
      pos(x0, y1, z0).color(color).endVertex();
      pos(x0, y1, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.DOWN_EAST) != 0) {
      pos(x1, y0, z0).color(color).endVertex();
      pos(x1, y0, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.UP_EAST) != 0) {
      pos(x1, y1, z0).color(color).endVertex();
      pos(x1, y1, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.DOWN_NORTH) != 0) {
      pos(x0, y0, z0).color(color).endVertex();
      pos(x1, y0, z0).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.UP_NORTH) != 0) {
      pos(x0, y1, z0).color(color).endVertex();
      pos(x1, y1, z0).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.DOWN_SOUTH) != 0) {
      pos(x0, y0, z1).color(color).endVertex();
      pos(x1, y0, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.UP_SOUTH) != 0) {
      pos(x0, y1, z1).color(color).endVertex();
      pos(x1, y1, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.NORTH_WEST) != 0) {
      pos(x0, y0, z0).color(color).endVertex();
      pos(x0, y1, z0).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.NORTH_EAST) != 0) {
      pos(x1, y0, z0).color(color).endVertex();
      pos(x1, y1, z0).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.SOUTH_WEST) != 0) {
      pos(x0, y0, z1).color(color).endVertex();
      pos(x0, y1, z1).color(color).endVertex();
    }

    if ((sides & GeometryMasks.Line.SOUTH_EAST) != 0) {
      pos(x1, y0, z1).color(color).endVertex();
      pos(x1, y1, z1).color(color).endVertex();
    }

    return this;
  }

  public BufferBuilderEx appendOutlinedCuboid(Vec3d start, Vec3d finish,
      final int sides, Color color) {
    return appendOutlinedCuboid(start.getX(), start.getY(), start.getZ(),
        finish.getX(), finish.getY(), finish.getZ(), sides, color);
  }

  public BufferBuilderEx appendOutlinedCuboid(Vec3i start, Vec3i finish,
      final int sides, Color color) {
    return appendOutlinedCuboid(start.getX(), start.getY(), start.getZ(),
        finish.getX(), finish.getY(), finish.getZ(), sides, color);
  }

  public BufferBuilderEx appendOutlinedCuboid(AxisAlignedBB bb,
      final int sides, Color color) {
    return appendOutlinedCuboid(bb.minX, bb.minY, bb.maxX,
        bb.maxX, bb.maxY, bb.maxZ, sides, color);
  }

  public void beginLines(VertexFormat format) {
    original.begin(GL11.GL_LINES, format);
  }

  public void beginLineLoop(VertexFormat format) {
    original.begin(GL11.GL_LINE_LOOP, format);
  }

  public void beginQuads(VertexFormat format) {
    original.begin(GL11.GL_QUADS, format);
  }

  public BufferBuilderEx color(@Nullable Color color) {
    if (color != null) {
      float[] color4f = color.toFloatArray();
      return color(color4f[0], color4f[1], color4f[2], color4f[3]);
    }
    return this;
  }

  public void draw() {
    tessellator.draw();
  }

  //
  // everything below wraps anything BufferBuilder implements
  //

  @Override
  public BufferBuilderEx pos(double x, double y, double z) {
    original.pos(x + getOffsetX(), y + getOffsetY(), z + getOffsetZ());
    return this;
  }

  @Override
  public BufferBuilderEx pos(Matrix4f matrixIn, float x, float y, float z) {
    original.pos(matrixIn, x + (float) getOffsetX(), y + (float) getOffsetY(), z + (float) getOffsetZ());
    return this;
  }

  @Override
  public BufferBuilderEx normal(Matrix3f matrixIn, float x, float y, float z) {
    original.normal(matrixIn, x, y, z);
    return this;
  }

  @Override
  public void sortVertexData(float cameraX, float cameraY, float cameraZ) {
    original.sortVertexData(cameraX, cameraY, cameraZ);
  }

  @Override
  public State getVertexState() {
    return original.getVertexState();
  }

  @Override
  public void setVertexState(State state) {
    original.setVertexState(state);
  }

  @Override
  public void begin(int glMode, VertexFormat format) {
    original.begin(glMode, format);
  }

  @Override
  public void finishDrawing() {
    original.finishDrawing();
    onDrawingFinished();
  }

  @Override
  public void putByte(int indexIn, byte byteIn) {
    original.putByte(indexIn, byteIn);
  }

  @Override
  public void putShort(int indexIn, short shortIn) {
    original.putShort(indexIn, shortIn);
  }

  @Override
  public void putFloat(int indexIn, float floatIn) {
    original.putFloat(indexIn, floatIn);
  }

  @Override
  public void endVertex() {
    original.endVertex();
  }

  @Override
  public void nextVertexFormatIndex() {
    original.nextVertexFormatIndex();
  }

  @Override
  public BufferBuilderEx color(int red, int green, int blue, int alpha) {
    original.color(red, green, blue, alpha);
    return this;
  }

  @Override
  public BufferBuilderEx tex(float u, float v) {
    original.tex(u, v);
    return this;
  }

  @Override
  public BufferBuilderEx overlay(int u, int v) {
    original.overlay(u, v);
    return this;
  }

  @Override
  public BufferBuilderEx lightmap(int u, int v) {
    original.lightmap(u, v);
    return this;
  }

  @Override
  public BufferBuilderEx texShort(short p_227847_1_, short p_227847_2_, int p_227847_3_) {
    original.texShort(p_227847_1_, p_227847_2_, p_227847_3_);
    return this;
  }

  @Override
  public BufferBuilderEx normal(float x, float y, float z) {
    original.normal(x, y, z);
    return this;
  }

  @Override
  public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
    original.vertex(x, y, z, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
  }

  @Override
  public BufferBuilderEx color(float red, float green, float blue, float alpha) {
    original.color(red, green, blue, alpha);
    return this;
  }

  @Override
  public BufferBuilderEx lightmap(int packedLight) {
    original.lightmap(packedLight);
    return this;
  }

  @Override
  public BufferBuilderEx overlay(int packedLight) {
    original.overlay(packedLight);
    return this;
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixEntryIn, BakedQuad quadIn, float redIn, float greenIn, float blueIn, int combinedLightIn, int combinedOverlayIn) {
    original.addVertexData(matrixEntryIn, quadIn, redIn, greenIn, blueIn, combinedLightIn, combinedOverlayIn);
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixStackIn, BakedQuad quadIn, float[] colorMuls, float red, float green, float blue, int[] lightmapCoords, int overlayColor, boolean readExistingColor) {
    original.addVertexData(matrixStackIn, quadIn, colorMuls, red, green, blue, lightmapCoords, overlayColor, readExistingColor);
  }

  @Override
  public Pair<DrawState, ByteBuffer> getAndResetData() {
    return original.getAndResetData();
  }

  @Override
  public void reset() {
    original.reset();
  }

  @Override
  public void discard() {
    original.discard();
  }

  @Override
  public VertexFormatElement getCurrentElement() {
    return original.getCurrentElement();
  }

  @Override
  public boolean isDrawing() {
    return original.isDrawing();
  }

  @Override
  public void putBulkData(ByteBuffer buffer) {
    original.putBulkData(buffer);
  }

  @Override
  public VertexFormat getVertexFormat() {
    return original.getVertexFormat();
  }

  @Override
  public void setDefaultColor(int red, int green, int blue, int alpha) {
    original.setDefaultColor(red, green, blue, alpha);
  }

  @Override
  public IVertexBuilder getVertexBuilder() {
    return this;
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixStack, BakedQuad bakedQuad, float red, float green, float blue, int lightmapCoord, int overlayColor, boolean readExistingColor) {
    original.addVertexData(matrixStack, bakedQuad, red, green, blue, lightmapCoord, overlayColor, readExistingColor);
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixEntry, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int lightmapCoord, int overlayColor) {
    original.addVertexData(matrixEntry, bakedQuad, red, green, blue, alpha, lightmapCoord, overlayColor);
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixEntry, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int lightmapCoord, int overlayColor, boolean readExistingColor) {
    original.addVertexData(matrixEntry, bakedQuad, red, green, blue, alpha, lightmapCoord, overlayColor, readExistingColor);
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixEntry, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
    original.addVertexData(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
  }

  @Override
  public int applyBakedLighting(int lightmapCoord, ByteBuffer data) {
    return original.applyBakedLighting(lightmapCoord, data);
  }

  @Override
  public void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
    original.applyBakedNormals(generated, data, normalTransform);
  }
}
