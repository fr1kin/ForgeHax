package dev.fiki.forgehax.main.util.tesselation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;

@Getter
public class BufferBuilderEx extends BufferBuilder {
  private final BufferBuilder original;

  private double offsetX;
  private double offsetY;
  private double offsetZ;

  public BufferBuilderEx(BufferBuilder original) {
    super(0);
    this.original = original;
  }

  public void setTranslation(double x, double y, double z) {
    offsetX = x;
    offsetY = y;
    offsetZ = z;
  }

  public void setTranslation(Vec3d pos) {
    setTranslation(pos.getX(), pos.getY(), pos.getZ());
  }

  @Override
  public IVertexBuilder pos(double x, double y, double z) {
    return original.pos(x + offsetX, y + offsetY, z + offsetZ);
  }

  @Override
  public IVertexBuilder pos(Matrix4f matrixIn, float x, float y, float z) {
    return original.pos(matrixIn, x + (float) offsetX, y + (float) offsetY, z + (float) offsetZ);
  }

  @Override
  public IVertexBuilder normal(Matrix3f matrixIn, float x, float y, float z) {
    return original.normal(matrixIn, x, y, z);
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
    setTranslation(0, 0, 0);
    original.finishDrawing();
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
  public IVertexBuilder color(int red, int green, int blue, int alpha) {
    return original.color(red, green, blue, alpha);
  }

  @Override
  public IVertexBuilder tex(float u, float v) {
    return original.tex(u, v);
  }

  @Override
  public IVertexBuilder overlay(int u, int v) {
    return original.overlay(u, v);
  }

  @Override
  public IVertexBuilder lightmap(int u, int v) {
    return original.lightmap(u, v);
  }

  @Override
  public IVertexBuilder texShort(short p_227847_1_, short p_227847_2_, int p_227847_3_) {
    return original.texShort(p_227847_1_, p_227847_2_, p_227847_3_);
  }

  @Override
  public IVertexBuilder normal(float x, float y, float z) {
    return original.normal(x, y, z);
  }

  @Override
  public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
    original.vertex(x, y, z, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
  }

  @Override
  public IVertexBuilder color(float red, float green, float blue, float alpha) {
    return original.color(red, green, blue, alpha);
  }

  @Override
  public IVertexBuilder lightmap(int packedLight) {
    return original.lightmap(packedLight);
  }

  @Override
  public IVertexBuilder overlay(int packedLight) {
    return original.overlay(packedLight);
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
    return original.getVertexBuilder();
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
