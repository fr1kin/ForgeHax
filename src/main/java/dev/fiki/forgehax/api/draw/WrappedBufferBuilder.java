package dev.fiki.forgehax.api.draw;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public final class WrappedBufferBuilder extends BufferBuilder {
  private final BufferBuilder builder;
  private final Consumer<BufferBuilder> onBegin;
  private final Consumer<BufferBuilder> onFinish;

  public WrappedBufferBuilder(BufferBuilder builder,
      Consumer<BufferBuilder> onBegin,
      Consumer<BufferBuilder> onFinish) {
    super(0);
    this.builder = builder;
    this.onBegin = onBegin;
    this.onFinish = onFinish;
  }

  @Override
  protected void growBuffer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sortVertexData(float cameraX, float cameraY, float cameraZ) {
    builder.sortVertexData(cameraX, cameraY, cameraZ);
  }

  @Override
  public State getVertexState() {
    return builder.getVertexState();
  }

  @Override
  public void setVertexState(State state) {
    builder.setVertexState(state);
  }

  @Override
  public void begin(int glMode, VertexFormat format) {
    builder.begin(glMode, format);

    if (onBegin != null) {
      onBegin.accept(this);
    }
  }

  @Override
  public void finishDrawing() {
    builder.finishDrawing();

    if (onFinish != null) {
      onFinish.accept(this);
    }
  }

  @Override
  public void putByte(int indexIn, byte byteIn) {
    builder.putByte(indexIn, byteIn);
  }

  @Override
  public void putShort(int indexIn, short shortIn) {
    builder.putShort(indexIn, shortIn);
  }

  @Override
  public void putFloat(int indexIn, float floatIn) {
    builder.putFloat(indexIn, floatIn);
  }

  @Override
  public void endVertex() {
    builder.endVertex();
  }

  @Override
  public void nextVertexFormatIndex() {
    builder.nextVertexFormatIndex();
  }

  @Override
  public IVertexBuilder color(int red, int green, int blue, int alpha) {
    return builder.color(red, green, blue, alpha);
  }

  @Override
  public void addVertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
    builder.addVertex(x, y, z, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
  }

  @Override
  public Pair<DrawState, ByteBuffer> getNextBuffer() {
    return builder.getNextBuffer();
  }

  @Override
  public void reset() {
    builder.reset();
  }

  @Override
  public void discard() {
    builder.discard();
  }

  @Override
  public VertexFormatElement getCurrentElement() {
    return builder.getCurrentElement();
  }

  @Override
  public boolean isDrawing() {
    return builder.isDrawing();
  }

  @Override
  public void putBulkData(ByteBuffer buffer) {
    builder.putBulkData(buffer);
  }

  @Override
  public VertexFormat getVertexFormat() {
    return builder.getVertexFormat();
  }

  @Override
  public void setDefaultColor(int red, int green, int blue, int alpha) {
    builder.setDefaultColor(red, green, blue, alpha);
  }

  @Override
  public IVertexBuilder pos(double x, double y, double z) {
    return builder.pos(x, y, z);
  }

  @Override
  public IVertexBuilder tex(float u, float v) {
    return builder.tex(u, v);
  }

  @Override
  public IVertexBuilder overlay(int u, int v) {
    return builder.overlay(u, v);
  }

  @Override
  public IVertexBuilder lightmap(int u, int v) {
    return builder.lightmap(u, v);
  }

  @Override
  public IVertexBuilder texShort(short u, short v, int index) {
    return builder.texShort(u, v, index);
  }

  @Override
  public IVertexBuilder normal(float x, float y, float z) {
    return builder.normal(x, y, z);
  }

  @Override
  public IVertexBuilder color(float red, float green, float blue, float alpha) {
    return builder.color(red, green, blue, alpha);
  }

  @Override
  public IVertexBuilder lightmap(int lightmapUV) {
    return builder.lightmap(lightmapUV);
  }

  @Override
  public IVertexBuilder overlay(int overlayUV) {
    return builder.overlay(overlayUV);
  }

  @Override
  public void addQuad(MatrixStack.Entry matrixEntryIn, BakedQuad quadIn, float redIn, float greenIn, float blueIn, int combinedLightIn, int combinedOverlayIn) {
    builder.addQuad(matrixEntryIn, quadIn, redIn, greenIn, blueIn, combinedLightIn, combinedOverlayIn);
  }

  @Override
  public void addQuad(MatrixStack.Entry matrixEntryIn, BakedQuad quadIn, float[] colorMuls, float redIn, float greenIn, float blueIn, int[] combinedLightsIn, int combinedOverlayIn, boolean mulColor) {
    builder.addQuad(matrixEntryIn, quadIn, colorMuls, redIn, greenIn, blueIn, combinedLightsIn, combinedOverlayIn, mulColor);
  }

  @Override
  public IVertexBuilder pos(Matrix4f matrixIn, float x, float y, float z) {
    return builder.pos(matrixIn, x, y, z);
  }

  @Override
  public IVertexBuilder normal(Matrix3f matrixIn, float x, float y, float z) {
    return builder.normal(matrixIn, x, y, z);
  }

  @Override
  public IVertexBuilder getVertexBuilder() {
    return builder.getVertexBuilder();
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixStack, BakedQuad bakedQuad, float red, float green, float blue, int lightmapCoord, int overlayColor, boolean readExistingColor) {
    builder.addVertexData(matrixStack, bakedQuad, red, green, blue, lightmapCoord, overlayColor, readExistingColor);
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixEntry, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int lightmapCoord, int overlayColor) {
    builder.addVertexData(matrixEntry, bakedQuad, red, green, blue, alpha, lightmapCoord, overlayColor);
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixEntry, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int lightmapCoord, int overlayColor, boolean readExistingColor) {
    builder.addVertexData(matrixEntry, bakedQuad, red, green, blue, alpha, lightmapCoord, overlayColor);
  }

  @Override
  public void addVertexData(MatrixStack.Entry matrixEntry, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
    builder.addVertexData(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
  }

  @Override
  public int applyBakedLighting(int lightmapCoord, ByteBuffer data) {
    return builder.applyBakedLighting(lightmapCoord, data);
  }

  @Override
  public void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
    builder.applyBakedNormals(generated, data, normalTransform);
  }

  @Override
  public int hashCode() {
    return builder.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return builder.equals(obj);
  }

  @Override
  public String toString() {
    return builder.toString();
  }
}
