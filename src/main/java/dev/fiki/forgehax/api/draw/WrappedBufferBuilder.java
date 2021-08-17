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
  protected void ensureVertexCapacity() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sortQuads(float p_181674_1_, float p_181674_2_, float p_181674_3_) {
    builder.sortQuads(p_181674_1_, p_181674_2_, p_181674_3_);
  }

  @Override
  public State getState() {
    return builder.getState();
  }

  @Override
  public void restoreState(State p_178993_1_) {
    builder.restoreState(p_178993_1_);
  }

  @Override
  public void end() {
    builder.end();
  }

  @Override
  public void nextElement() {
    builder.nextElement();
  }

  @Override
  public void vertex(float p_225588_1_, float p_225588_2_, float p_225588_3_, float p_225588_4_, float p_225588_5_, float p_225588_6_, float p_225588_7_, float p_225588_8_, float p_225588_9_, int p_225588_10_, int p_225588_11_, float p_225588_12_, float p_225588_13_, float p_225588_14_) {
    builder.vertex(p_225588_1_, p_225588_2_, p_225588_3_, p_225588_4_, p_225588_5_, p_225588_6_, p_225588_7_, p_225588_8_, p_225588_9_, p_225588_10_, p_225588_11_, p_225588_12_, p_225588_13_, p_225588_14_);
  }

  @Override
  public Pair<DrawState, ByteBuffer> popNextBuffer() {
    return builder.popNextBuffer();
  }

  @Override
  public void clear() {
    builder.clear();
  }

  @Override
  public VertexFormatElement currentElement() {
    return builder.currentElement();
  }

  @Override
  public boolean building() {
    return builder.building();
  }

  @Override
  public void begin(int glMode, VertexFormat format) {
    builder.begin(glMode, format);

    if (onBegin != null) {
      onBegin.accept(this);
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
  public IVertexBuilder color(int red, int green, int blue, int alpha) {
    return builder.color(red, green, blue, alpha);
  }

  @Override
  public void discard() {
    builder.discard();
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
  public IVertexBuilder normal(float x, float y, float z) {
    return builder.normal(x, y, z);
  }

  @Override
  public IVertexBuilder color(float red, float green, float blue, float alpha) {
    return builder.color(red, green, blue, alpha);
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
  public void defaultColor(int p_225611_1_, int p_225611_2_, int p_225611_3_, int p_225611_4_) {
    builder.defaultColor(p_225611_1_, p_225611_2_, p_225611_3_, p_225611_4_);
  }

  @Override
  public IVertexBuilder vertex(double p_225582_1_, double p_225582_3_, double p_225582_5_) {
    return builder.vertex(p_225582_1_, p_225582_3_, p_225582_5_);
  }

  @Override
  public IVertexBuilder uv(float p_225583_1_, float p_225583_2_) {
    return builder.uv(p_225583_1_, p_225583_2_);
  }

  @Override
  public IVertexBuilder overlayCoords(int p_225585_1_, int p_225585_2_) {
    return builder.overlayCoords(p_225585_1_, p_225585_2_);
  }

  @Override
  public IVertexBuilder uv2(int p_225587_1_, int p_225587_2_) {
    return builder.uv2(p_225587_1_, p_225587_2_);
  }

  @Override
  public IVertexBuilder uvShort(short p_227847_1_, short p_227847_2_, int p_227847_3_) {
    return builder.uvShort(p_227847_1_, p_227847_2_, p_227847_3_);
  }

  @Override
  public IVertexBuilder uv2(int p_227886_1_) {
    return builder.uv2(p_227886_1_);
  }

  @Override
  public IVertexBuilder overlayCoords(int p_227891_1_) {
    return builder.overlayCoords(p_227891_1_);
  }

  @Override
  public void putBulkData(MatrixStack.Entry p_227889_1_, BakedQuad p_227889_2_, float p_227889_3_, float p_227889_4_, float p_227889_5_, int p_227889_6_, int p_227889_7_) {
    builder.putBulkData(p_227889_1_, p_227889_2_, p_227889_3_, p_227889_4_, p_227889_5_, p_227889_6_, p_227889_7_);
  }

  @Override
  public void putBulkData(MatrixStack.Entry p_227890_1_, BakedQuad p_227890_2_, float[] p_227890_3_, float p_227890_4_, float p_227890_5_, float p_227890_6_, int[] p_227890_7_, int p_227890_8_, boolean p_227890_9_) {
    builder.putBulkData(p_227890_1_, p_227890_2_, p_227890_3_, p_227890_4_, p_227890_5_, p_227890_6_, p_227890_7_, p_227890_8_, p_227890_9_);
  }

  @Override
  public IVertexBuilder vertex(Matrix4f p_227888_1_, float p_227888_2_, float p_227888_3_, float p_227888_4_) {
    return builder.vertex(p_227888_1_, p_227888_2_, p_227888_3_, p_227888_4_);
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
