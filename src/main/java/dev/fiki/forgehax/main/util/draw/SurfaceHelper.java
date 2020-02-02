package dev.fiki.forgehax.main.util.draw;

import static dev.fiki.forgehax.main.Common.*;
import static com.mojang.blaze3d.systems.RenderSystem.*;
import static dev.fiki.forgehax.main.util.math.AlignHelper.getFlowDirY2;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.draw.font.Fonts;
import dev.fiki.forgehax.main.util.draw.font.MinecraftFontRenderer;

import java.util.List;
import javax.annotation.Nullable;

import dev.fiki.forgehax.main.util.math.AlignHelper;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/**
 * 2D rendering
 */
public class SurfaceHelper {

  public static void drawString(@Nullable MinecraftFontRenderer fontRenderer, String text,
      double x, double y,
      Color color, boolean shadow) {
    if (fontRenderer == null) {
      IRenderTypeBuffer.Impl rt = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
      getFontRenderer().renderString(text,
          Math.round(x), Math.round(y),
          color.toBuffer(), shadow,
          TransformationMatrix.identity().getMatrix(), rt,
          false, 0, 15728880);
    } else {
//      fontRenderer.drawString(text, x, y, color.toBuffer(), shadow);
    }
  }

  public static double getStringWidth(@Nullable MinecraftFontRenderer fontRenderer, String text) {
    return fontRenderer == null ? getFontRenderer().getStringWidth(text) : fontRenderer.getStringWidth(text);
  }

  public static double getStringHeight(@Nullable MinecraftFontRenderer fontRenderer) {
    return fontRenderer == null ? getFontRenderer().FONT_HEIGHT : fontRenderer.getStringHeight("HEIGHT");
  }

  private static void createRect(BufferBuilder builder,
      double x, double y, double w, double h,
      Color color) {
    builder.pos(x, y, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.pos(x, y + h, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.pos(x + w, y + h, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.pos(x + w, y, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
  }

  public static void rect(BufferBuilder builder,
      float x, float y, float w, float h,
      Color color) {
    builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    createRect(builder, x, y, w, h, color);
    builder.finishDrawing();
  }

  public static void outlinedRect(BufferBuilder builder,
      float x, float y, float w, float h,
      Color color) {
    builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
    createRect(builder, x, y, w, h, color);
    builder.finishDrawing();
  }

  public static void texturedRect(BufferBuilder builder,
      float x, float y,
      float textureX, float textureY,
      float width, float height,
      float zLevel) {
    builder.begin(7, DefaultVertexFormats.POSITION_TEX);
    builder.pos(x, y + height, zLevel)
        .tex(textureX + 0, textureY + height)
        .endVertex();
    builder.pos(x + width, y + height, zLevel)
        .tex(textureX + width, textureY + height)
        .endVertex();
    builder.pos(x + width, y + 0, zLevel)
        .tex(textureX + width, textureY + 0)
        .endVertex();
    builder.pos(x + 0, y + 0, zLevel)
        .tex(textureX + 0, textureY + 0)
        .endVertex();
    builder.finishDrawing();
  }

  public static void line(BufferBuilder builder,
      double x1, double y1, double x2, double y2,
      Color color) {
    builder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    builder.pos(x1, y1, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.pos(x2, y2, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.finishDrawing();
  }

  public static void drawText(String msg, float x, float y, int color, boolean shadow) {
    if(shadow) {
      getFontRenderer().drawStringWithShadow(msg, x, y, color);
    } else {
      getFontRenderer().drawString(msg, x, y, color);
    }
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
    final int offsetX = AlignHelper.alignH((int)(getTextWidth(msg)*scale), alignmask);
    final int offsetY = AlignHelper.alignV((int)(getTextHeight()*scale), alignmask);
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
    final int height = (int)(getFlowDirY2(alignmask) * (getTextHeight()+1) * scale);
    final float invScale = (float)(1 / scale);

    for (int i = 0; i < msgList.size(); i++) {
      final int offsetX = AlignHelper.alignH((int) (getTextWidth(msgList.get(i)) * scale), alignmask);

      drawText(msgList.get(i),
          (int)((x - offsetX) * invScale), (int)((y - offsetY + height*i) * invScale),
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

  @Deprecated
  public static void drawText(String msg, int x, int y, int color, double scale) {
//    drawText(msg, x, y, color, scale, false);
  }

  @Deprecated
  public static void drawTextShadow(String msg, int x, int y, int color, double scale) {
//    drawText(msg, x, y, color, scale, true);
  }

  @Deprecated
  public static int getTextWidth(String text, double scale) {
    return (int) (MC.fontRenderer.getStringWidth(text) * scale);
  }

  @Deprecated
  public static int getTextWidth(String text) {
    return getTextWidth(text, 1.D);
  }

  @Deprecated
  public static int getTextHeight() {
    return MC.fontRenderer.FONT_HEIGHT;
  }

  @Deprecated
  public static int getTextHeight(double scale) {
    return (int) (MC.fontRenderer.FONT_HEIGHT * scale);
  }

  public static void drawItem(ItemStack item, int x, int y) {
    MC.getItemRenderer().renderItemAndEffectIntoGUI(item, x, y);
  }

  public static void drawItemOverlay(ItemStack stack, int x, int y) {
    MC.getItemRenderer().renderItemOverlayIntoGUI(MC.fontRenderer, stack, x, y, null);
  }

  protected static void drawItemAndEffectIntoGUI(@Nullable LivingEntity living, final ItemStack stack,
      int x, int y,
      double scale) {
    if (!stack.isEmpty()) {
      MC.getItemRenderer().zLevel += 50.f;
      try {
        MC.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);
      } finally {
        MC.getItemRenderer().zLevel -= 50.f;
      }
    }
  }

  protected static void renderItemOverlayIntoGUI(
      FontRenderer fr,
      ItemStack stack,
      double xPosition,
      double yPosition,
      @Nullable String text,
      double scale) {
    final double SCALE_RATIO = 1.23076923077D;

    if (!stack.isEmpty()) {
      if (stack.getCount() != 1 || text != null) {
        String s = text == null ? String.valueOf(stack.getCount()) : text;
        disableLighting();
        disableDepthTest();
        disableBlend();
        fr.drawStringWithShadow(
            s,
            (float) (xPosition + 19 - 2 - fr.getStringWidth(s)),
            (float) (yPosition + 6 + 3),
            16777215);
        enableLighting();
        disableDepthTest();
        // Fixes opaque cooldown overlay a bit lower
        // TODO: check if enabled blending still screws things up down the line.
        enableBlend();
      }

      if (stack.getItem().showDurabilityBar(stack)) {
        disableLighting();
        disableDepthTest();
        disableTexture();
        disableAlphaTest();
        disableBlend();
        double health = stack.getItem().getDurabilityForDisplay(stack);
        int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
        int i = Math.round(13.0F - (float) health * 13.0F);
        int j = rgbfordisplay;
        draw(xPosition + (scale / 8.D), yPosition + (scale / SCALE_RATIO), 13, 2, 0, 0, 0, 255);
        draw(
            xPosition + (scale / 8.D),
            yPosition + (scale / SCALE_RATIO),
            i,
            1,
            j >> 16 & 255,
            j >> 8 & 255,
            j & 255,
            255);
        enableBlend();
        enableAlphaTest();
        enableTexture();
        enableLighting();
        disableDepthTest();
      }

      ClientPlayerEntity ply = getLocalPlayer();
      float f3 = ply == null ? 0.0F
          : ply.getCooldownTracker().getCooldown(stack.getItem(), MC.getRenderPartialTicks());

      if (f3 > 0.0F) {
        disableLighting();
        disableDepthTest();
        disableTexture();
        draw(xPosition, yPosition + scale * (1.0F - f3), 16, scale * f3, 255, 255, 255, 127);
        enableTexture();
        enableLighting();
        disableDepthTest();
      }
    }
  }

  private static void draw(
      double x, double y, double width, double height, int red, int green, int blue, int alpha) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder renderer = tessellator.getBuffer();
    renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    renderer
        .pos(x + 0, y + 0, 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    renderer
        .pos(x + 0, y + height, 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    renderer
        .pos(x + width, y + height, 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    renderer
        .pos(x + width, y + 0, 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    Tessellator.getInstance().draw();
  }

  public static void drawScaledCustomSizeModalRect(double x, double y,
      float u, float v,
      double uWidth, double vHeight,
      double width, double height,
      double tileWidth, double tileHeight) {
    float f = 1.0F / (float) tileWidth;
    float f1 = 1.0F / (float) tileHeight;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    bufferbuilder
        .pos(x, y + height, 0.0D)
        .tex(u * f, (v + (float) vHeight) * f1)
        .endVertex();
    bufferbuilder
        .pos(x + width, y + height, 0.0D)
        .tex((u + (float) uWidth) * f, (v + (float) vHeight) * f1)
        .endVertex();
    bufferbuilder
        .pos(x + width, y, 0.0D)
        .tex((u + (float) uWidth) * f, v * f1)
        .endVertex();
    bufferbuilder
        .pos(x, y, 0.0D)
        .tex(u * f, v * f1)
        .endVertex();
    tessellator.draw();
  }

  public static int getHeadWidth(float scale) {
    return (int) (scale * 12);
  }

  public static int getHeadWidth() {
    return getHeadWidth(1.f);
  }

  public static int getHeadHeight(float scale) {
    return (int) (scale * 12);
  }

  public static int getHeadHeight() {
    return getHeadWidth(1.f);
  }

  @Deprecated
  public static void drawRect(int x, int y, int i, int height, int toBuffer) {
    AbstractGui.fill(x, y, i, height, toBuffer);
  }

  @Deprecated
  public static void drawOutlinedRect(int x, int y, int width, int height, int color) {
    float f3 = (float)(color >> 24 & 255) / 255.0F;
    float f = (float)(color >> 16 & 255) / 255.0F;
    float f1 = (float)(color >> 8 & 255) / 255.0F;
    float f2 = (float)(color & 255) / 255.0F;
    Matrix4f matrix = TransformationMatrix.identity().getMatrix();
    BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
    RenderSystem.enableBlend();
    RenderSystem.disableTexture();
    RenderSystem.defaultBlendFunc();
    bufferbuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(matrix, (float)x, (float)y, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.pos(matrix, (float)x, (float)y + height, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.pos(matrix, (float)x + width, (float)y + height, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.pos(matrix, (float)x + width, (float)y, 0.0F).color(f, f1, f2, f3).endVertex();
    bufferbuilder.finishDrawing();
    WorldVertexBufferUploader.draw(bufferbuilder);
    RenderSystem.enableTexture();
    RenderSystem.disableBlend();
  }

  @Deprecated
  public static void texturedRect(int x, int y, int i, int i1, int i2, int i3, int depth) {
    drawRect(x, y, i, i1, depth);
  }

  @Deprecated
  public static void drawOutlinedRectShaded(int x, int y, int w, int h, int colorOutline, int shade, float width) {
    float f = (float)(colorOutline >> 24 & 255) / 255.0F;
    float f1 = (float)(colorOutline >> 16 & 255) / 255.0F;
    float f2 = (float)(colorOutline >> 8 & 255) / 255.0F;
    float f3 = (float)(colorOutline & 255) / 255.0F;
    float f4 = (float)(shade >> 24 & 255) / 255.0F;
    float f5 = (float)(shade >> 16 & 255) / 255.0F;
    float f6 = (float)(shade >> 8 & 255) / 255.0F;
    float f7 = (float)(shade & 255) / 255.0F;
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.disableAlphaTest();
    RenderSystem.defaultBlendFunc();
    RenderSystem.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos((double)w, (double)y, 0).color(f1, f2, f3, f).endVertex();
    bufferbuilder.pos((double)x, (double)y, 0).color(f1, f2, f3, f).endVertex();
    bufferbuilder.pos((double)x, (double)h, 0).color(f5, f6, f7, f4).endVertex();
    bufferbuilder.pos((double)w, (double)h, 0).color(f5, f6, f7, f4).endVertex();
    tessellator.draw();
    RenderSystem.shadeModel(7424);
    RenderSystem.disableBlend();
    RenderSystem.enableAlphaTest();
    RenderSystem.enableTexture();
  }

  public static void drawTextureRect(double x, double y, double width, double height, float u, float v, float t, float s) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder renderer = tessellator.getBuffer();
    renderer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
    renderer.pos(x + width, y, 0F).tex(t, v).endVertex();
    renderer.pos(x, y, 0F).tex(u, v).endVertex();
    renderer.pos(x, y + height, 0F).tex(u, s).endVertex();
    renderer.pos(x, y + height, 0F).tex(u, s).endVertex();
    renderer.pos(x + width, y + height, 0F).tex(t, s).endVertex();
    renderer.pos(x + width, y, 0F).tex(t, v).endVertex();
    tessellator.draw();
  }

  public static void drawLine(float size, float x, float y, float x1, float y1) {
    RenderSystem.lineWidth(size);
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    buffer.begin(GL_LINES, DefaultVertexFormats.POSITION);
    buffer.pos(x, y, 0F).endVertex();
    buffer.pos(x1, y1, 0F).endVertex();
    tessellator.draw();
    GlStateManager.enableTexture();
  }
}
