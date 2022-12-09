package com.matt.forgehax.util.draw;

import com.matt.forgehax.Globals;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** 2D rendering */
public class SurfaceUtils implements Globals {
  public static void drawLine(int startX, int startY, int endX, int endY, int color) {
    float r = (float) (color >> 16 & 255) / 255.0F;
    float g = (float) (color >> 8 & 255) / 255.0F;
    float b = (float) (color & 255) / 255.0F;
    float a = (float) (color >> 24 & 255) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder BufferBuilder = tessellator.getBuffer();

    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.tryBlendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    GlStateManager.color(r, g, b, a);

    BufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
    BufferBuilder.pos(startX, startY, 0.0D).endVertex();
    BufferBuilder.pos(endX, endY, 0.0D).endVertex();
    tessellator.draw();

    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }

  public static void drawRect(int x, int y, int w, int h, int color) {
    GL11.glLineWidth(1.0f);
    Gui.drawRect(x, y, x + w, y + h, color);
  }

  public static void drawOutlinedRect(int x, int y, int w, int h, int color, float width) {
    float r = (float) (color >> 16 & 255) / 255.0F;
    float g = (float) (color >> 8 & 255) / 255.0F;
    float b = (float) (color & 255) / 255.0F;
    float a = (float) (color >> 24 & 255) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder BufferBuilder = tessellator.getBuffer();

    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.tryBlendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    GlStateManager.color(r, g, b, a);

    GL11.glLineWidth(width);

    BufferBuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
    BufferBuilder.pos(x, y, 0.0D).endVertex();
    BufferBuilder.pos(x, (double) y + h, 0.0D).endVertex();
    BufferBuilder.pos((double) x + w, (double) y + h, 0.0D).endVertex();
    BufferBuilder.pos((double) x + w, y, 0.0D).endVertex();
    tessellator.draw();

    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }

  public static void drawOutlinedRect(int x, int y, int w, int h, int color) {
    drawOutlinedRect(x, y, w, h, color, 1.f);
  }

  public static void drawTexturedRect(
      int x, int y, int textureX, int textureY, int width, int height, int zLevel) {
    float f = 0.00390625F;
    float f1 = 0.00390625F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder BufferBuilder = tessellator.getBuffer();
    BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    BufferBuilder.pos(x + 0, y + height, zLevel)
        .tex(
                (float) (textureX + 0) * 0.00390625F,
                (float) (textureY + height) * 0.00390625F)
        .endVertex();
    BufferBuilder.pos(x + width, y + height, zLevel)
        .tex(
                (float) (textureX + width) * 0.00390625F,
                (float) (textureY + height) * 0.00390625F)
        .endVertex();
    BufferBuilder.pos(x + width, y + 0, zLevel)
        .tex(
                (float) (textureX + width) * 0.00390625F,
                (float) (textureY + 0) * 0.00390625F)
        .endVertex();
    BufferBuilder.pos(x + 0, y + 0, zLevel)
        .tex(
                (float) (textureX + 0) * 0.00390625F,
                (float) (textureY + 0) * 0.00390625F)
        .endVertex();
    tessellator.draw();
  }

  public static void drawText(String msg, int x, int y, int color) {
    MC.fontRenderer.drawString(msg, x, y, color);
  }

  public static void drawTextShadow(String msg, int x, int y, int color) {
    MC.fontRenderer.drawStringWithShadow(msg, x, y, color);
  }

  public static void drawText(String msg, int x, int y, int color, double scale, boolean shadow) {
    GlStateManager.pushMatrix();
    GlStateManager.disableDepth();
    GlStateManager.scale(scale, scale, scale);
    MC.fontRenderer.drawString(
        msg, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color, shadow);
    GlStateManager.enableDepth();
    GlStateManager.popMatrix();
  }

  public static void drawText(String msg, int x, int y, int color, double scale) {
    drawText(msg, x, y, color, scale, false);
  }

  public static void drawTextShadow(String msg, int x, int y, int color, double scale) {
    drawText(msg, x, y, color, scale, true);
  }

  public static int getTextWidth(String text, double scale) {
    return (int) (MC.fontRenderer.getStringWidth(text) * scale);
  }

  public static int getTextWidth(String text) {
    return getTextWidth(text, 1.D);
  }

  public static int getTextHeight() {
    return MC.fontRenderer.FONT_HEIGHT;
  }

  public static int getTextHeight(double scale) {
    return (int) (MC.fontRenderer.FONT_HEIGHT * scale);
  }

  public static void drawItem(ItemStack item, int x, int y) {
    GlStateManager.pushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.enableLighting();
    MC.getRenderItem().zLevel = 100.f;
    MC.getRenderItem().renderItemAndEffectIntoGUI(item, x, y);
    MC.getRenderItem().zLevel = 0.f;
    GlStateManager.popMatrix();
    GlStateManager.disableLighting();
    GlStateManager.enableDepth();
    GlStateManager.color(1.f, 1.f, 1.f, 1.f);
  }

  public static void drawItemWithOverlay(ItemStack item, int x, int y) {
    GlStateManager.pushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.enableLighting();
    MC.getRenderItem().zLevel = 100.f;
    MC.getRenderItem().renderItemAndEffectIntoGUI(item, x, y);
    MC.getRenderItem().renderItemOverlays(MC.fontRenderer, item, x, y);
    MC.getRenderItem().zLevel = 0.f;
    GlStateManager.popMatrix();
    GlStateManager.disableLighting();
    GlStateManager.enableDepth();
    GlStateManager.color(1.f, 1.f, 1.f, 1.f);
  }

  public static void drawPotionEffect(PotionEffect potion, int x, int y) {
    int index = potion.getPotion().getStatusIconIndex();
    GlStateManager.pushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.enableLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.color(1.f, 1.f, 1.f, 1.f);
    MC.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
    drawTexturedRect(x, y, index % 8 * 18, 198 + index / 8 * 18, 18, 18, 100);
    potion.getPotion().renderHUDEffect(x, y, potion, MC, 255);
    GlStateManager.disableLighting();
    GlStateManager.enableDepth();
    GlStateManager.color(1.f, 1.f, 1.f, 1.f);
    GlStateManager.popMatrix();
  }

  public static void drawHead(ResourceLocation skinResource, int x, int y, float scale) {
    GlStateManager.pushMatrix();
    MC.renderEngine.bindTexture(skinResource);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.F);
    GlStateManager.scale(scale, scale, scale);
    Gui.drawScaledCustomSizeModalRect(
        (int) (x * (1 / scale)), (int) (y * (1 / scale)), 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
    Gui.drawScaledCustomSizeModalRect(
        (int) (x * (1 / scale)), (int) (y * (1 / scale)), 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
    GlStateManager.popMatrix();
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
}
