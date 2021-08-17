package dev.fiki.forgehax.api.draw;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.math.AlignHelper;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static dev.fiki.forgehax.api.math.AlignHelper.getFlowDirY2;
import static dev.fiki.forgehax.main.Common.*;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * 2D rendering
 */
public class SurfaceHelper {
  static void _rect(BufferBuilder builder,
      double x, double y, double w, double h,
      Color color) {
    builder.vertex(x, y, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.vertex(x, y + h, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.vertex(x + w, y + h, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.vertex(x + w, y, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
  }

  public static void rect(BufferBuilder builder,
      float x, float y, float w, float h,
      Color color) {
    builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    _rect(builder, x, y, w, h, color);
    builder.end();
  }

  public static void outlinedRect(BufferBuilder builder,
      float x, float y, float w, float h,
      Color color) {
    builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
    _rect(builder, x, y, w, h, color);
    builder.end();
  }

  public static void texturedRect(BufferBuilder builder,
      float x, float y,
      float textureX, float textureY,
      float width, float height,
      float depth) {
    builder.begin(7, DefaultVertexFormats.POSITION_TEX);
    builder.vertex(x, y + height, depth)
        .uv(textureX + 0, textureY + height)
        .endVertex();
    builder.vertex(x + width, y + height, depth)
        .uv(textureX + width, textureY + height)
        .endVertex();
    builder.vertex(x + width, y + 0, depth)
        .uv(textureX + width, textureY + 0)
        .endVertex();
    builder.vertex(x + 0, y + 0, depth)
        .uv(textureX + 0, textureY + 0)
        .endVertex();
    builder.end();
  }

  public static void line(BufferBuilder builder,
      float startX, float startY, float endX, float endY,
      Color color) {
    builder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    builder.vertex(startX, startY, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.vertex(endX, endY, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.end();
  }

  public static void renderString(@Nonnull IRenderTypeBuffer buffer,
      @Nonnull Matrix4f matrix4f,
      @Nonnull String text,
      float x, float y,
      Color color,
      boolean shadow) {
    getFontRenderer().drawInBatch(text,
        Math.round(x), Math.round(y),
        color.toBuffer(), shadow,
        matrix4f, buffer,
        false, 0, 15728880);
  }

  public static void renderString(@Nonnull IRenderTypeBuffer buffer,
      @Nonnull String text,
      float x, float y,
      Color color,
      boolean shadow) {
    renderString(buffer, TransformationMatrix.identity().getMatrix(), text, x, y, color, shadow);
  }

  public static IRenderTypeBuffer.Impl renderString(@Nonnull BufferBuilder builder,
      @Nonnull Matrix4f matrix4f,
      @Nonnull String text,
      float x, float y,
      Color color,
      boolean shadow) {
    IRenderTypeBuffer.Impl render = IRenderTypeBuffer.immediate(builder);
    renderString(render, matrix4f, text, x, y, color, shadow);
    return render;
  }

  public static void renderString(@Nonnull BufferBuilder builder,
      @Nonnull String text,
      float x, float y,
      Color color,
      boolean shadow) {
    renderString(builder, TransformationMatrix.identity().getMatrix(), text, x, y, color, shadow);
  }

  public static double getStringWidth(String text) {
    return getFontRenderer().width(text);
  }

  public static double getStringHeight() {
    return getFontRenderer().lineHeight;
  }

  public static void drawText(String msg, float x, float y, int color, boolean shadow) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuilder();
    Matrix4f matrix4f = TransformationMatrix.identity().getMatrix();

    renderString(builder, matrix4f, msg, x, y, Color.of(color), shadow).endBatch();
  }

  public static void drawText(String msg, float x, float y, int color) {
    drawText(msg, x, y, color, false);
  }

  public static void drawTextShadow(String msg, float x, float y, int color) {
    drawText(msg, x, y, color, true);
  }

  public static void drawTextShadowCentered(String msg, float x, float y, int color) {
    float offsetX = getTextWidth(msg) / 2f;
    float offsetY = getTextHeight() / 2f;
    drawTextShadow(msg, x - offsetX, y - offsetY, color);
  }

  public static void drawTextAlignH(String msg, float x, float y, int color, boolean shadow, int alignmask) {
    final int offsetX = AlignHelper.alignH(getTextWidth(msg), alignmask);
    drawText(msg, x - offsetX, y, color, shadow);
  }

  public static void drawTextShadowAlignH(String msg, int x, int y, int color, int alignmask) {
    drawTextAlignH(msg, x, y, color, true, alignmask);
  }

  public static void drawTextAlign(String msg, int x, int y, int color, boolean shadow, int alignmask) {
    final int offsetX = AlignHelper.alignH(getTextWidth(msg), alignmask);
    final int offsetY = AlignHelper.alignV(getTextHeight(), alignmask);
    drawText(msg, x - offsetX, y - offsetY, color, shadow);
  }

  public static void drawTextShadowAlign(String msg, int x, int y, int color, int alignmask) {
    drawTextAlign(msg, x, y, color, true, alignmask);
  }

  public static void drawTextAlign(String msg, int x, int y, int color, double scale, boolean shadow, int alignmask) {
    final int offsetX = AlignHelper.alignH((int) (getTextWidth(msg) * scale), alignmask);
    final int offsetY = AlignHelper.alignV((int) (getTextHeight() * scale), alignmask);
    if (scale != 1.0d) {
      drawText(msg, x - offsetX, y - offsetY, color, scale, shadow);
    } else {
      drawText(msg, x - offsetX, y - offsetY, color, shadow);
    }
  }

  public static void drawTextAlign(List<String> msgList, int x, int y, int color, double scale, boolean shadow, int alignmask) {
    pushMatrix();
    disableDepthTest();
    scaled(scale, scale, scale);

    final int offsetY = AlignHelper.alignV((int) (getTextHeight() * scale), alignmask);
    final int height = (int) (getFlowDirY2(alignmask) * (getTextHeight() + 1) * scale);
    final float invScale = (float) (1 / scale);

    for (int i = 0; i < msgList.size(); i++) {
      final int offsetX = AlignHelper.alignH((int) (getTextWidth(msgList.get(i)) * scale), alignmask);

      drawText(msgList.get(i),
          (int) ((x - offsetX) * invScale), (int) ((y - offsetY + height * i) * invScale),
          color, shadow);
    }

    disableDepthTest();
    popMatrix();
  }

  public static void drawText(String msg, int x, int y, int color, double scale, boolean shadow) {
    disableDepthTest();
    scaled(scale, scale, scale);
    drawText(msg, x, y, color, shadow);
  }

  public static void drawText(String msg, int x, int y, int color, double scale) {
    drawText(msg, x, y, color, scale, false);
  }

  public static void drawTextShadow(String msg, int x, int y, int color, double scale) {
    drawText(msg, x, y, color, scale, true);
  }

  @Deprecated
  public static int getTextWidth(String text, double scale) {
    return (int) (getStringWidth(text) * scale);
  }

  @Deprecated
  public static int getTextWidth(String text) {
    return getTextWidth(text, 1.D);
  }

  @Deprecated
  public static int getTextHeight() {
    return (int) getStringHeight();
  }

  @Deprecated
  public static int getTextHeight(double scale) {
    return (int) (getStringHeight() * scale);
  }

  public static void drawItem(ItemStack item, int x, int y) {
    MC.getItemRenderer().renderGuiItem(item, x, y);
  }

  public static void drawItemOverlay(ItemStack stack, int x, int y) {
    MC.getItemRenderer().renderGuiItemDecorations(getFontRenderer(), stack, x, y, null);
  }

  public static void setItemRendererDepth(float depth) {
    MC.getItemRenderer().blitOffset = depth;
  }

  private static void renderModel(IBakedModel modelIn, ItemStack stack,
      int combinedLightIn, int combinedOverlayIn,
      MatrixStack matrixStackIn, IVertexBuilder bufferIn) {
    Random random = new Random();
    long i = 42L;

    for (Direction direction : Direction.values()) {
      random.setSeed(42L);
      MC.getItemRenderer().renderQuadList(matrixStackIn, bufferIn,
          modelIn.getQuads(null, direction, random), stack, combinedLightIn, combinedOverlayIn);
    }

    random.setSeed(42L);
    MC.getItemRenderer().renderQuadList(matrixStackIn, bufferIn,
        modelIn.getQuads(null, null, random), stack, combinedLightIn, combinedOverlayIn);
  }

  private static RenderType getRenderType(ItemStack itemStackIn) {
    Item item = itemStackIn.getItem();
    if (item instanceof BlockItem) {
      Block block = ((BlockItem) item).getBlock();
      return RenderTypeLookup.canRenderInLayer(block.defaultBlockState(), RenderType.translucent())
          ? RenderTypeEx.blockTranslucentCull()
          : RenderTypeEx.blockCutout();
    } else {
      return RenderTypeEx.blockTranslucentCull();
    }
  }

  private static IVertexBuilder getBuffer(IRenderTypeBuffer bufferIn, RenderType renderTypeIn, boolean isItemIn, boolean glintIn) {
    return glintIn ? VertexBuilderUtils.create(
        bufferIn.getBuffer(isItemIn
            ? RenderType.glint()
            : RenderType.entityGlint()),
        bufferIn.getBuffer(renderTypeIn))
        : bufferIn.getBuffer(renderTypeIn);
  }

  public static boolean renderItem(LivingEntity living, World world, ItemStack itemStack, MatrixStack stack, IRenderTypeBuffer buffer) {
    IBakedModel model = MC.getItemRenderer().getModel(itemStack, world, living);
    MC.getItemRenderer().render(itemStack, ItemCameraTransforms.TransformType.GUI,
        false, stack, buffer, 15728880, OverlayTexture.NO_OVERLAY, model);
    return model.usesBlockLight();
  }

  public static boolean renderItem(LivingEntity living, ItemStack itemStack, MatrixStack stack, IRenderTypeBuffer buffer) {
    return renderItem(living, living.level, itemStack, stack, buffer);
  }

  public static boolean renderItem(ItemStack itemStack, MatrixStack stack, IRenderTypeBuffer buffer) {
    return renderItem(null, null, itemStack, stack, buffer);
  }

  public static boolean renderItemInGui(ItemStack itemStack, MatrixStack stack, IRenderTypeBuffer buffer) {
    try {
      stack.pushPose();
      stack.translate(8, 8, 0);
      stack.scale(1, -1, 1);
      stack.scale(16, 16, 16);
      return renderItem(getLocalPlayer(), null, itemStack, stack, buffer);
    } finally {
      stack.popPose();
    }
  }

  public static void finish(IRenderTypeBuffer buffers, RenderType renderType,
      @Nullable Consumer<RenderType> pre, @Nullable Consumer<RenderType> post) {
    final ReflectionTools reflections = ReflectionTools.getInstance();

    BufferBuilder defaultBuilder = reflections.IRenderTypeBuffer$Impl_builder.get(buffers);
    BufferBuilder builder = reflections.IRenderTypeBuffer$Impl_fixedBuffers.get(buffers)
        .getOrDefault(renderType, defaultBuilder);

    boolean lastRender = Objects.equals(reflections.IRenderTypeBuffer$Impl_lastState.get(buffers),
        renderType.asOptional());
    if (lastRender || builder != defaultBuilder) {
      if (reflections.IRenderTypeBuffer$Impl_startedBuffers.get(buffers).remove(builder)) {
        if (builder.building()) {
          if (reflections.RenderType_sortOnUpload.get(renderType)) {
            builder.sortQuads(0.f, 0.f, 0.f);
          }

          if (pre != null) {
            pre.accept(renderType);
          }

          VertexBuilderEx.draw(builder);

          if (post != null) {
            post.accept(renderType);
          }
        }

        if (lastRender) {
          reflections.IRenderTypeBuffer$Impl_lastState.set(buffers, Optional.empty());
        }
      }
    }
  }

  public static void finish(final IRenderTypeBuffer buffers,
      @Nullable final Consumer<RenderType> pre, @Nullable final Consumer<RenderType> post) {
    final ReflectionTools reflections = ReflectionTools.getInstance();

    reflections.IRenderTypeBuffer$Impl_lastState.get(buffers).ifPresent(renderType -> {
      IVertexBuilder builder = buffers.getBuffer(renderType);
      if (builder == reflections.IRenderTypeBuffer$Impl_builder.get(buffers)) {
        finish(buffers, renderType, pre, post);
      }
    });

    for (RenderType renderType : reflections.IRenderTypeBuffer$Impl_fixedBuffers.get(buffers).keySet()) {
      finish(buffers, renderType, pre, post);
    }
  }

  public static void renderItemOverlay(BufferBuilder buffer, MatrixStack matrixStack,
      FontRenderer fr, ItemStack stack,
      int x, int y, @Nullable String text) {
    // copied from MC source
    if (!stack.isEmpty()) {
      if (stack.getCount() != 1 || text != null) {
        String val = text == null ? String.valueOf(stack.getCount()) : text;
        matrixStack.pushPose();
        matrixStack.translate((float)(x + 19 - 2 - fr.width(val)), (float)(y + 6 + 3), 200.f);

        renderString(buffer, matrixStack.last().pose(), val, 0, 0, Colors.WHITE, true).endBatch();

        matrixStack.popPose();
      }

      if (stack.getItem().showDurabilityBar(stack)) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        double health = stack.getItem().getDurabilityForDisplay(stack);
        int i = Math.round(13.0F - (float)health * 13.0F);
        int j = stack.getItem().getRGBDurabilityForDisplay(stack);

        buffer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        VertexBuilderEx.rect(buffer, GL_TRIANGLES,
            x + 2, y + 13, 13, 2,
            Colors.BLACK, matrixStack.last().pose());
        VertexBuilderEx.rect(buffer, GL_TRIANGLES,
            x + 2, y + 13, i, 1,
            Color.of(j >> 16 & 255, j >> 8 & 255, j & 255, 255), matrixStack.last().pose());
        VertexBuilderEx.draw(buffer);

        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
      }

      ClientPlayerEntity lp = getLocalPlayer();
      float f3 = lp == null ? 0.0F : lp.getCooldowns()
          .getCooldownPercent(stack.getItem(), Minecraft.getInstance().getDeltaFrameTime());
      if (f3 > 0.0F) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        buffer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        VertexBuilderEx.rect(buffer, GL_TRIANGLES,
            x, y + MathHelper.floor(16.0F * (1.0F - f3)),
            16, MathHelper.ceil(16.0F * f3),
            Colors.WHITE.setAlpha(127), matrixStack.last().pose());
        VertexBuilderEx.draw(buffer);

        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
      }

    }
  }

  public static void drawScaledCustomSizeModalRect(double x, double y,
      float u, float v,
      double uWidth, double vHeight,
      double width, double height,
      double tileWidth, double tileHeight) {
    float f = 1.0F / (float) tileWidth;
    float f1 = 1.0F / (float) tileHeight;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuilder();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    bufferbuilder
        .vertex(x, y + height, 0.0D)
        .uv(u * f, (v + (float) vHeight) * f1)
        .endVertex();
    bufferbuilder
        .vertex(x + width, y + height, 0.0D)
        .uv((u + (float) uWidth) * f, (v + (float) vHeight) * f1)
        .endVertex();
    bufferbuilder
        .vertex(x + width, y, 0.0D)
        .uv((u + (float) uWidth) * f, v * f1)
        .endVertex();
    bufferbuilder
        .vertex(x, y, 0.0D)
        .uv(u * f, v * f1)
        .endVertex();
    tessellator.end();
  }

  @Deprecated
  public static void drawRect(int x, int y, int i, int height, int toBuffer) {
    throw new UnsupportedOperationException("TODO 1.16"); // TODO: 1.16
//    AbstractGui.func_238463_a_(TransformationMatrix.identity(), x, y, i, height, toBuffer);
  }

  @Deprecated
  public static void drawOutlinedRect(int x, int y, int width, int height, int color) {
    float f3 = (float) (color >> 24 & 255) / 255.0F;
    float f = (float) (color >> 16 & 255) / 255.0F;
    float f1 = (float) (color >> 8 & 255) / 255.0F;
    float f2 = (float) (color & 255) / 255.0F;
    Matrix4f matrix = TransformationMatrix.identity().getMatrix();
    BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
    RenderSystem.enableBlend();
    RenderSystem.disableTexture();
    RenderSystem.defaultBlendFunc();
    bufferbuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.vertex(matrix, (float) x, (float) y, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.vertex(matrix, (float) x, (float) y + height, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.vertex(matrix, (float) x + width, (float) y + height, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.vertex(matrix, (float) x + width, (float) y, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.end();
    WorldVertexBufferUploader.end(bufferbuilder);
    RenderSystem.enableTexture();
    RenderSystem.disableBlend();
  }

  @Deprecated
  public static void texturedRect(int x, int y, int i, int i1, int i2, int i3, int depth) {
    drawRect(x, y, i, i1, depth);
  }

  @Deprecated
  public static void drawOutlinedRectShaded(int x, int y, int w, int h, int colorOutline, int shade, float width) {
    float f = (float) (colorOutline >> 24 & 255) / 255.0F;
    float f1 = (float) (colorOutline >> 16 & 255) / 255.0F;
    float f2 = (float) (colorOutline >> 8 & 255) / 255.0F;
    float f3 = (float) (colorOutline & 255) / 255.0F;
    float f4 = (float) (shade >> 24 & 255) / 255.0F;
    float f5 = (float) (shade >> 16 & 255) / 255.0F;
    float f6 = (float) (shade >> 8 & 255) / 255.0F;
    float f7 = (float) (shade & 255) / 255.0F;
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.disableAlphaTest();
    RenderSystem.defaultBlendFunc();
    RenderSystem.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuilder();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.vertex((double) w, (double) y, 0).color(f1, f2, f3, f).endVertex();
    bufferbuilder.vertex((double) x, (double) y, 0).color(f1, f2, f3, f).endVertex();
    bufferbuilder.vertex((double) x, (double) h, 0).color(f5, f6, f7, f4).endVertex();
    bufferbuilder.vertex((double) w, (double) h, 0).color(f5, f6, f7, f4).endVertex();
    tessellator.end();
    RenderSystem.shadeModel(7424);
    RenderSystem.disableBlend();
    RenderSystem.enableAlphaTest();
    RenderSystem.enableTexture();
  }

  @Deprecated
  public static void drawTextureRect(double x, double y, double width, double height, float u, float v, float t, float s) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder renderer = tessellator.getBuilder();
    renderer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
    renderer.vertex(x + width, y, 0F).uv(t, v).endVertex();
    renderer.vertex(x, y, 0F).uv(u, v).endVertex();
    renderer.vertex(x, y + height, 0F).uv(u, s).endVertex();
      renderer.vertex(x, y + height, 0F).uv(u, s).endVertex();
    renderer.vertex(x + width, y + height, 0F).uv(t, s).endVertex();
    renderer.vertex(x + width, y, 0F).uv(t, v).endVertex();
    tessellator.end();
  }

  @Deprecated
  public static void drawLine(float size, float x, float y, float x1, float y1) {
    RenderSystem.lineWidth(size);
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    buffer.begin(GL_LINES, DefaultVertexFormats.POSITION);
    buffer.vertex(x, y, 0F).endVertex();
    buffer.vertex(x1, y1, 0F).endVertex();
    tessellator.end();
    GlStateManager._enableTexture();
  }
}
