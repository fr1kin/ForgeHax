package dev.fiki.forgehax.main.util.draw;

import static dev.fiki.forgehax.main.Globals.*;
import static com.mojang.blaze3d.systems.RenderSystem.*;

import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.draw.font.MinecraftFontRenderer;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
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
      fontRenderer.drawString(text, x, y, color.toBuffer(), shadow);
    }
  }
  
  public static double getStringWidth(@Nullable MinecraftFontRenderer fontRenderer, String text) {
    return fontRenderer == null ? getFontRenderer().getStringWidth(text) : fontRenderer.getStringWidth(text);
  }
  
  public static double getStringHeight(@Nullable MinecraftFontRenderer fontRenderer) {
    return fontRenderer == null ? getFontRenderer().FONT_HEIGHT : fontRenderer.getHeight();
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
      double x, double y, double w, double h, 
      Color color) {
    builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    createRect(builder, x, y, w, h, color);
    builder.finishDrawing();
  }
  
  @Deprecated
  public static void drawOutlinedRectShaded(int x, int y, int w, int h, int colorOutline, int shade, float width) {
    throw new UnsupportedOperationException();
  }
  
  public static void outlinedRect(BufferBuilder builder, 
      double x, double y, double w, double h, 
      Color color) {
    builder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
    createRect(builder, x, y, w, h, color);
    builder.finishDrawing();
  }
  
  public static void texturedRect(BufferBuilder builder, 
      double x, double y,
      double textureX, double textureY,
      double width, double height,
      double zLevel) {
    builder.begin(7, DefaultVertexFormats.POSITION_TEX);
    builder.pos(x, y + height, zLevel)
        .tex(
            (float) (textureX + 0) * 0.00390625F,
            (float) (textureY + height) * 0.00390625F)
        .endVertex();
    builder.pos(x + width, y + height, zLevel)
        .tex(
            (float) (textureX + width) * 0.00390625F,
            (float) (textureY + height) * 0.00390625F)
        .endVertex();
    builder.pos(x + width, y + 0, zLevel)
        .tex(
            (float) (textureX + width) * 0.00390625F,
            (float) (textureY + 0) * 0.00390625F)
        .endVertex();
    builder.pos(x + 0, y + 0, zLevel)
        .tex(
            (float) (textureX + 0) * 0.00390625F,
            (float) (textureY + 0) * 0.00390625F)
        .endVertex();
    builder.finishDrawing();
  }
  
  public static void line(BufferBuilder builder, 
      double x1, double y1, double x2, double y2, 
      Color color) {
    builder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    builder.pos(x1, y1, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.pos(x2, y2, 0.0D)
        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
        .endVertex();
    builder.finishDrawing();
  }
  
  @Deprecated
  public static void drawText(String msg, int x, int y, int color) {
    throw new UnsupportedOperationException();
  }
  
  @Deprecated
  public static void drawTextShadow(String msg, int x, int y, int color) {
//    MC.fontRenderer.drawStringWithShadow(msg, x, y, color);
  }
  
  @Deprecated
  public static void drawTextShadowCentered(String msg, float x, float y, int color) {
//    float offsetX = getTextWidth(msg) / 2f;
//    float offsetY = getTextHeight() / 2f;
//    MC.fontRenderer.drawStringWithShadow(msg, x - offsetX, y - offsetY, color);
  }

  @Deprecated
  public static void drawTextAlignH(String msg, int x, int y, int color, boolean shadow, int alignmask) {
//    final int offsetX = AlignHelper.alignH(getTextWidth(msg), alignmask);
//    MC.fontRenderer.drawString(msg, x - offsetX, y, color, shadow);
  }
  
  @Deprecated
  public static void drawTextShadowAlignH(String msg, int x, int y, int color, int alignmask) {
//    drawTextAlignH(msg, x, y, color, true, alignmask);
  }
  
  @Deprecated
  public static void drawTextAlign(String msg, int x, int y, int color, boolean shadow, int alignmask) {
//    final int offsetX = AlignHelper.alignH(getTextWidth(msg), alignmask);
//    final int offsetY = AlignHelper.alignV(getTextHeight(), alignmask);
//    MC.fontRenderer.drawString(msg, x - offsetX, y - offsetY, color, shadow);
  }
  
  @Deprecated
  public static void drawTextShadowAlign(String msg, int x, int y, int color, int alignmask) {
//    drawTextAlign(msg, x, y, color, true, alignmask);
  }
  
  @Deprecated
  public static void drawTextAlign(String msg, int x, int y, int color, double scale, boolean shadow, int alignmask) {
//    final int offsetX = AlignHelper.alignH((int)(getTextWidth(msg)*scale), alignmask);
//    final int offsetY = AlignHelper.alignV((int)(getTextHeight()*scale), alignmask);
//    if (scale != 1.0d) {
//      drawText(msg, x - offsetX, y - offsetY, color, scale, shadow);
//    } else {
//      MC.fontRenderer.drawString(msg, x - offsetX, y - offsetY, color, shadow);
//    }
  }
  
  @Deprecated
  public static void drawTextAlign(List<String> msgList, int x, int y, int color, double scale, boolean shadow, int alignmask) {
//    pushMatrix();
//    disableDepthTest();
//    scale(scale, scale, scale);
//    
//    final int offsetY = AlignHelper.alignV((int) (getTextHeight() * scale), alignmask);
//    final int height = (int)(getFlowDirY2(alignmask) * (getTextHeight()+1) * scale);
//    final float invScale = (float)(1 / scale);
//    
//    for (int i = 0; i < msgList.size(); i++) {
//      final int offsetX = AlignHelper.alignH((int) (getTextWidth(msgList.get(i)) * scale), alignmask);
//      
//      MC.fontRenderer.drawString(
//          msgList.get(i), (x - offsetX) * invScale, (y - offsetY + height*i) * invScale, color, shadow);
//    }
//    
//    disableDepthTest();
//    popMatrix();
//  }
//
//  public static void drawText(String msg, int x, int y, int color, double scale, boolean shadow) {
//    pushMatrix();
//    disableDepthTest();
//    scale(scale, scale, scale);
//    MC.fontRenderer.drawString(
//        msg, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color, shadow);
//    disableDepthTest();
//    popMatrix();
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
    float f = 1.0F / (float)tileWidth;
    float f1 = 1.0F / (float)tileHeight;
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

  }

  @Deprecated
  public static void drawOutlinedRect(int x, int y, int i, int height, int toBuffer) {

  }

  @Deprecated
  public static void texturedRect(int x, int y, int i, int i1, int i2, int i3, int depth) {

  }
}
