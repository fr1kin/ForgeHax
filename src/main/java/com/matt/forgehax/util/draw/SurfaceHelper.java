package com.matt.forgehax.util.draw;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

/** 2D rendering */
public class SurfaceHelper implements Globals {
  public static void drawString(
      @Nullable MinecraftFontRenderer fontRenderer,
      String text,
      double x,
      double y,
      int color,
      boolean shadow) {
    if (fontRenderer == null) {
      if (shadow)
        MC.fontRenderer.drawStringWithShadow(text, Math.round(x), Math.round(y), color);
      else
        MC.fontRenderer.drawString(text, Math.round(x), Math.round(y), color);

    } else {
      fontRenderer.drawString(text, x, y, color, shadow);
    }
  }

  public static double getStringWidth(@Nullable MinecraftFontRenderer fontRenderer, String text) {
    if (fontRenderer == null) return MC.fontRenderer.getStringWidth(text);
    else return fontRenderer.getStringWidth(text);
  }

  public static double getStringHeight(@Nullable MinecraftFontRenderer fontRenderer) {
    if (fontRenderer == null) return MC.fontRenderer.FONT_HEIGHT;
    else return fontRenderer.getHeight();
  }

  public static void drawRect(int x, int y, int w, int h, int color) {
    GL11.glLineWidth(1.0f);
    Gui.drawRect(x, y, x + w, y + h, color);
  }

  public static void drawOutlinedRect(int x, int y, int w, int h, int color) {
    drawOutlinedRect(x, y, w, h, color, 1.f);
  }

  public static void drawOutlinedRectShaded(
      int x, int y, int w, int h, int colorOutline, int shade, float width) {
    int shaded = (0x00FFFFFF & colorOutline) | ((shade & 255) << 24); // modify the alpha value
    // int shaded = Utils.toRGBA(255,255,255, 100);
    drawRect(x, y, w, h, shaded);
    drawOutlinedRect(x, y, w, h, colorOutline, width);
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
    GlStateManager.blendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    GlStateManager.color4f(r, g, b, a);

    GL11.glLineWidth(width);

    BufferBuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
    BufferBuilder.pos((double) x, (double) y, 0.0D).endVertex();
    BufferBuilder.pos((double) x, (double) y + h, 0.0D).endVertex();
    BufferBuilder.pos((double) x + w, (double) y + h, 0.0D).endVertex();
    BufferBuilder.pos((double) x + w, (double) y, 0.0D).endVertex();
    tessellator.draw();

    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }

  public static void drawTexturedRect(
      int x, int y, int textureX, int textureY, int width, int height, int zLevel) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder BufferBuilder = tessellator.getBuffer();
    BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    BufferBuilder.pos((double) (x + 0), (double) (y + height), (double) zLevel)
        .tex(
            (double) ((float) (textureX + 0) * 0.00390625F),
            (double) ((float) (textureY + height) * 0.00390625F))
        .endVertex();
    BufferBuilder.pos((double) (x + width), (double) (y + height), (double) zLevel)
        .tex(
            (double) ((float) (textureX + width) * 0.00390625F),
            (double) ((float) (textureY + height) * 0.00390625F))
        .endVertex();
    BufferBuilder.pos((double) (x + width), (double) (y + 0), (double) zLevel)
        .tex(
            (double) ((float) (textureX + width) * 0.00390625F),
            (double) ((float) (textureY + 0) * 0.00390625F))
        .endVertex();
    BufferBuilder.pos((double) (x + 0), (double) (y + 0), (double) zLevel)
        .tex(
            (double) ((float) (textureX + 0) * 0.00390625F),
            (double) ((float) (textureY + 0) * 0.00390625F))
        .endVertex();
    tessellator.draw();
  }

  public static void drawLine(int x1, int y1, int x2, int y2, int color, float width) {
    float r = (float) (color >> 16 & 255) / 255.0F;
    float g = (float) (color >> 8 & 255) / 255.0F;
    float b = (float) (color & 255) / 255.0F;
    float a = (float) (color >> 24 & 255) / 255.0F;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder BufferBuilder = tessellator.getBuffer();

    GlStateManager.enableBlend();
    GlStateManager.disableTexture2D();
    GlStateManager.blendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    GlStateManager.color4f(r, g, b, a);

    GL11.glLineWidth(width);

    BufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
    BufferBuilder.pos((double) x1, (double) y1, 0.0D).endVertex();
    BufferBuilder.pos((double) x2, (double) y2, 0.0D).endVertex();
    tessellator.draw();

    GlStateManager.color3f(1f, 1f, 1f);
    GlStateManager.enableTexture2D();
    GlStateManager.disableBlend();
  }

  public static void drawText(String msg, int x, int y, int color) {
    MC.fontRenderer.drawString(msg, x, y, color);
  }

  public static void drawTextShadow(String msg, int x, int y, int color) {
    MC.fontRenderer.drawStringWithShadow(msg, x, y, color);
  }

  public static void drawTextShadowCentered(String msg, float x, float y, int color) {
    float offsetX = getTextWidth(msg) / 2f;
    float offsetY = getTextHeight() / 2f;
    MC.fontRenderer.drawStringWithShadow(msg, x - offsetX, y - offsetY, color);
  }

  public static void drawText(String msg, int x, int y, int color, double scale, boolean shadow) {
    GlStateManager.pushMatrix();
    GlStateManager.disableDepthTest();
    GlStateManager.scaled(scale, scale, scale);
    MC.fontRenderer.drawString(msg, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color);
    if (shadow)
      MC.fontRenderer.drawStringWithShadow(msg, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color);
    else
      MC.fontRenderer.drawString(msg, (int) (x * (1 / scale)), (int) (y * (1 / scale)), color);
    GlStateManager.enableDepthTest();
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
    MC.getItemRenderer().renderItemAndEffectIntoGUI(item, x, y);
  }

  public static void drawItemOverlay(ItemStack stack, int x, int y) {
    MC.getItemRenderer().renderItemOverlayIntoGUI(MC.fontRenderer, stack, x, y, null);
  }

  public static void drawItem(ItemStack item, double x, double y) {
    GlStateManager.pushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.enableLighting();
    MC.getItemRenderer().zLevel = 100.f;
    renderItemAndEffectIntoGUI(getLocalPlayer(), item, x, y, 16.D);
    MC.getItemRenderer().zLevel = 0.f;
    GlStateManager.popMatrix();
    GlStateManager.disableLighting();
    GlStateManager.enableDepthTest();
    GlStateManager.color4f(1.f, 1.f, 1.f, 1.f);
  }

  public static void drawItemWithOverlay(ItemStack item, double x, double y, double scale) {
    GlStateManager.pushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.enableLighting();
    MC.getItemRenderer().zLevel = 100.f;
    renderItemAndEffectIntoGUI(getLocalPlayer(), item, x, y, 16.D);
    renderItemOverlayIntoGUI(MC.fontRenderer, item, x, y, null, scale);
    MC.getItemRenderer().zLevel = 0.f;
    GlStateManager.popMatrix();
    GlStateManager.disableLighting();
    GlStateManager.enableDepthTest();
    GlStateManager.color4f(1.f, 1.f, 1.f, 1.f);
  }

  public static void drawPotionEffect(PotionEffect potion, Gui gui, int x, int y) {
    int index = potion.getPotion().getStatusIconIndex();
    GlStateManager.pushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.enableLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.color4f(1.f, 1.f, 1.f, 1.f);
    MC.getTextureManager().bindTexture(GuiContainer.INVENTORY_BACKGROUND);
    drawTexturedRect(x, y, index % 8 * 18, 198 + index / 8 * 18, 18, 18, 100);
    //potion.getPotion().renderHUDEffect(x, y, potion, MC, 255);
    potion.getPotion().renderHUDEffect(potion, gui, x, y, 1.f, 255); // TODO: this might not be correct
    GlStateManager.disableLighting();
    GlStateManager.enableDepthTest();
    GlStateManager.color4f(1.f, 1.f, 1.f, 1.f);
    GlStateManager.popMatrix();
  }

  public static void drawHead(ResourceLocation skinResource, double x, double y, float scale) {
    GlStateManager.pushMatrix();
    MC.textureManager.bindTexture(skinResource);
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.F);
    GlStateManager.scaled(scale, scale, scale);
    drawScaledCustomSizeModalRect(
        (x * (1 / scale)), (y * (1 / scale)), 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
    drawScaledCustomSizeModalRect(
        (x * (1 / scale)), (y * (1 / scale)), 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
    GlStateManager.popMatrix();
  }

  protected static void renderItemAndEffectIntoGUI(
      @Nullable EntityLivingBase living, final ItemStack stack, double x, double y, double scale) {
    if (!stack.isEmpty()) {
      MC.getItemRenderer().zLevel += 50.f;
      try {
        renderItemModelIntoGUI(
            stack, x, y, MC.getItemRenderer().getItemModelWithOverrides(stack, null, living), scale);
      } catch (Throwable t) {
        Helper.handleThrowable(t);
      } finally {
        MC.getItemRenderer().zLevel -= 50.f;
      }
    }
  }

  private static void renderItemModelIntoGUI(
          ItemStack stack, double x, double y, IBakedModel bakedmodel, double scale) {
    GlStateManager.pushMatrix();
    MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    MC.getTextureManager()
        .getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        .setBlurMipmap(false, false);
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableAlphaTest();
    GlStateManager.alphaFunc(516, 0.1F);
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(
        GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

    GlStateManager.translated(x, y, 100.0F + MC.getItemRenderer().zLevel);
    GlStateManager.translatef(8.0F, 8.0F, 0.0F);
    GlStateManager.scalef(1.0F, -1.0F, 1.0F);
    GlStateManager.scaled(scale, scale, scale);

    if (bakedmodel.isGui3d()) GlStateManager.enableLighting();
    else GlStateManager.disableLighting();

    bakedmodel =
        net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(
            bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
    MC.getItemRenderer().renderItem(stack, bakedmodel);
    GlStateManager.disableAlphaTest();
    GlStateManager.disableRescaleNormal();
    GlStateManager.disableLighting();
    GlStateManager.popMatrix();
    MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
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
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        GlStateManager.disableBlend();
        fr.drawStringWithShadow(
            s,
            (float) (xPosition + 19 - 2 - fr.getStringWidth(s)),
            (float) (yPosition + 6 + 3),
            16777215);
        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
        // Fixes opaque cooldown overlay a bit lower
        // TODO: check if enabled blending still screws things up down the line.
        GlStateManager.enableBlend();
      }

      if (stack.getItem().showDurabilityBar(stack)) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
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
        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
      }

      EntityPlayerSP entityplayersp = Minecraft.getInstance().player;
      float f3 =
          entityplayersp == null
              ? 0.0F
              : entityplayersp
                  .getCooldownTracker()
                  .getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());

      if (f3 > 0.0F) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        GlStateManager.disableTexture2D();
        draw(xPosition, yPosition + scale * (1.0F - f3), 16, scale * f3, 255, 255, 255, 127);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
      }
    }
  }

  private static void draw(
      double x, double y, double width, double height, int red, int green, int blue, int alpha) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder renderer = tessellator.getBuffer();
    renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    renderer
        .pos((double) (x + 0), (double) (y + 0), 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    renderer
        .pos((double) (x + 0), (double) (y + height), 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    renderer
        .pos((double) (x + width), (double) (y + height), 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    renderer
        .pos((double) (x + width), (double) (y + 0), 0.0D)
        .color(red, green, blue, alpha)
        .endVertex();
    Tessellator.getInstance().draw();
  }

  protected static void drawScaledCustomSizeModalRect(
      double x,
      double y,
      float u,
      float v,
      double uWidth,
      double vHeight,
      double width,
      double height,
      double tileWidth,
      double tileHeight) {
    double f = 1.0F / tileWidth;
    double f1 = 1.0F / tileHeight;
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    bufferbuilder
        .pos((double) x, (double) (y + height), 0.0D)
        .tex((double) (u * f), (double) ((v + (float) vHeight) * f1))
        .endVertex();
    bufferbuilder
        .pos((double) (x + width), (double) (y + height), 0.0D)
        .tex((double) ((u + (float) uWidth) * f), (double) ((v + (float) vHeight) * f1))
        .endVertex();
    bufferbuilder
        .pos((double) (x + width), (double) y, 0.0D)
        .tex((double) ((u + (float) uWidth) * f), (double) (v * f1))
        .endVertex();
    bufferbuilder
        .pos((double) x, (double) y, 0.0D)
        .tex((double) (u * f), (double) (v * f1))
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
}
