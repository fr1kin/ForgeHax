package com.matt.forgehax.util.draw;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.Helper.getLocalPlayer;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_POLYGON;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4d;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex3d;

import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.draw.font.MinecraftFontRenderer;
import java.util.Stack;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * Created on 9/2/2017 by fr1kin
 */
public class SurfaceBuilder {
  
  public static final int COLOR = 1;
  public static final int SCALE = 2;
  public static final int TRANSLATION = 4;
  public static final int ROTATION = 8;
  public static final int ALL = 15;
  
  private static final SurfaceBuilder INSTANCE = new SurfaceBuilder();
  
  public static SurfaceBuilder getBuilder() {
    return INSTANCE;
  }
  
  // --------------------
  
  private final Stack<RenderSettings> settings = new Stack<>();
  private final RenderSettings DEFAULT_SETTINGS = new RenderSettings();
  
  private RenderSettings current() {
    return !settings.isEmpty() ? settings.peek() : DEFAULT_SETTINGS;
  }
  
  public SurfaceBuilder begin(int mode) {
    glBegin(mode);
    return this;
  }
  
  public SurfaceBuilder beginLines() {
    return begin(GL_LINES);
  }
  
  public SurfaceBuilder beginLineLoop() {
    return begin(GL_LINE_LOOP);
  }
  
  public SurfaceBuilder beginQuads() {
    return begin(GL_QUADS);
  }
  
  public SurfaceBuilder beginPolygon() {
    return begin(GL_POLYGON);
  }
  
  public SurfaceBuilder end() {
    glEnd();
    return this;
  }
  
  public SurfaceBuilder autoApply(boolean enabled) {
    current().setAutoApply(enabled);
    return this;
  }
  
  public SurfaceBuilder apply() {
    return apply(ALL);
  }
  
  public SurfaceBuilder apply(int flags) {
    RenderSettings current = current();
    if ((flags & COLOR) == COLOR) {
      current.applyColor();
    }
    if ((flags & SCALE) == SCALE) {
      current.applyScale();
    }
    if ((flags & TRANSLATION) == TRANSLATION) {
      current.applyTranslation();
    }
    if ((flags & ROTATION) == ROTATION) {
      current.applyRotation();
    }
    return this;
  }
  
  public SurfaceBuilder reset() {
    return reset(ALL);
  }
  
  public SurfaceBuilder reset(int flags) {
    RenderSettings current = current();
    if ((flags & COLOR) == COLOR) {
      current.resetColor();
    }
    if ((flags & SCALE) == SCALE) {
      current.resetScale();
    }
    if ((flags & TRANSLATION) == TRANSLATION) {
      current.resetTranslation();
    }
    if ((flags & ROTATION) == ROTATION) {
      current.resetRotation();
    }
    return this;
  }
  
  public SurfaceBuilder push() {
    GlStateManager.pushMatrix();
    settings.push(new RenderSettings());
    return this;
  }
  
  public SurfaceBuilder pop() {
    if (!settings.isEmpty()) {
      settings.pop();
    }
    GlStateManager.popMatrix();
    return this;
  }
  
  public SurfaceBuilder color(double r, double g, double b, double a) {
    current()
      .setColor4d(
        new double[]{
          MathHelper.clamp(r, 0.D, 1.D),
          MathHelper.clamp(g, 0.D, 1.D),
          MathHelper.clamp(b, 0.D, 1.D),
          MathHelper.clamp(a, 0.D, 1.D)
        });
    return this;
  }
  
  public SurfaceBuilder color(int buffer) {
    return color(
      (buffer >> 16 & 255) / 255.D,
      (buffer >> 8 & 255) / 255.D,
      (buffer & 255) / 255.D,
      (buffer >> 24 & 255) / 255.D);
  }
  
  public SurfaceBuilder color(int r, int g, int b, int a) {
    return color(r / 255.D, g / 255.D, b / 255.D, a / 255.D);
  }
  
  public SurfaceBuilder scale(double x, double y, double z) {
    current().setScale3d(new double[]{x, y, z});
    return this;
  }
  
  public SurfaceBuilder scale(double s) {
    return scale(s, s, s);
  }
  
  public SurfaceBuilder scale() {
    return scale(0.D);
  }
  
  public SurfaceBuilder translate(double x, double y, double z) {
    current().setTranslate3d(new double[]{x, y, z});
    return this;
  }
  
  public SurfaceBuilder translate(double x, double y) {
    return translate(x, y, 0.D);
  }
  
  public SurfaceBuilder rotate(double angle, double x, double y, double z) {
    current().setRotated4d(new double[]{angle, x, y, z});
    return this;
  }
  
  public SurfaceBuilder width(double width) {
    GlStateManager.glLineWidth((float) width);
    return this;
  }
  
  public SurfaceBuilder vertex(double x, double y, double z) {
    glVertex3d(x, y, z);
    return this;
  }
  
  public SurfaceBuilder vertex(double x, double y) {
    glVertex2d(x, y);
    return this;
  }
  
  public SurfaceBuilder line(double startX, double startY, double endX, double endY) {
    return vertex(startX, startY).vertex(endX, endY);
  }
  
  public SurfaceBuilder rectangle(double x, double y, double w, double h) {
    return vertex(x, y).vertex(x, y + h).vertex(x + w, y + h).vertex(x + w, y);
  }
  
  public SurfaceBuilder fontRenderer(MinecraftFontRenderer fontRenderer) {
    current().setFontRenderer(fontRenderer);
    return this;
  }
  
  private SurfaceBuilder text(String text, double x, double y, boolean shadow) {
    if (current().hasFontRenderer()) // use custom font renderer
    {
      current()
        .getFontRenderer()
        .drawString(
          text,
          x,
          y + 1 /*TTF font renderer needs to be offset by 1*/,
          Color.of(current().getColor4d()).toBuffer(),
          shadow);
    } else {
      // use default minecraft font
      GlStateManager.pushMatrix();
      GlStateManager.translate(x, y, 0.D);
  
      MC.fontRenderer.drawString(text, 0, 0, Color.of(current().getColor4d()).toBuffer(), shadow);
  
      GlStateManager.popMatrix();
    }
    return this;
  }
  
  public SurfaceBuilder text(String text, double x, double y) {
    return text(text, x, y, false);
  }
  
  public SurfaceBuilder textWithShadow(String text, double x, double y) {
    return text(text, x, y, true);
  }
  
  public SurfaceBuilder task(Runnable task) {
    task.run();
    return this;
  }
  
  public SurfaceBuilder item(ItemStack stack, double x, double y) {
    MC.getRenderItem().zLevel = 100.f;
    SurfaceHelper.renderItemAndEffectIntoGUI(
      getLocalPlayer(), stack, x, y, current().hasScale() ? current().getScale3d()[0] : 16.D);
    MC.getRenderItem().zLevel = 0.f;
    return this;
  }
  
  public SurfaceBuilder itemOverlay(ItemStack stack, double x, double y) {
    SurfaceHelper.renderItemOverlayIntoGUI(
      MC.fontRenderer,
      stack,
      x,
      y,
      null,
      current().hasScale() ? current().getScale3d()[0] : 16.D);
    return this;
  }
  
  public SurfaceBuilder head(ResourceLocation resource, double x, double y) {
    MC.renderEngine.bindTexture(resource);
    double scale = current().hasScale() ? current().getScale3d()[0] : 12.D;
    SurfaceHelper.drawScaledCustomSizeModalRect(
      (x * (1 / scale)), (y * (1 / scale)), 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
    SurfaceHelper.drawScaledCustomSizeModalRect(
      (x * (1 / scale)), (y * (1 / scale)), 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
    return this;
  }
  
  public int getFontWidth(String text) {
    return current().hasFontRenderer()
      ? current().getFontRenderer().getStringWidth(text)
      : MC.fontRenderer.getStringWidth(text);
  }
  
  public int getFontHeight() {
    return current().hasFontRenderer()
      ? current().getFontRenderer().getHeight()
      : MC.fontRenderer.FONT_HEIGHT;
  }
  
  public int getFontHeight(String text) {
    return getFontHeight();
  }
  
  private double _getScaled(int index, double p) {
    return p * (1.D / current().getScale3d()[index]);
  }
  
  public double getScaledX(double x) {
    return _getScaled(0, x);
  }
  
  public double getScaledY(double y) {
    return _getScaled(1, y);
  }
  
  public double getScaledZ(double z) {
    return _getScaled(2, z);
  }
  
  public double getScaled(double p) {
    return getScaledX(p);
  }
  
  public double getItemSize() {
    return 16;
  }
  
  // --------------------
  
  public static void disableTexture2D() {
    GlStateManager.disableTexture2D();
  }
  
  public static void enableTexture2D() {
    GlStateManager.enableTexture2D();
  }
  
  public static void enableBlend() {
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(
      GlStateManager.SourceFactor.SRC_ALPHA,
      GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
      GlStateManager.SourceFactor.ONE,
      GlStateManager.DestFactor.ZERO);
  }
  
  public static void disableBlend() {
    GlStateManager.disableBlend();
  }
  
  public static void enableFontRendering() {
    GlStateManager.disableDepth();
  }
  
  public static void disableFontRendering() {
    GlStateManager.enableDepth();
  }
  
  public static void enableItemRendering() {
    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableColorMaterial();
    GlStateManager.enableLighting();
  }
  
  public static void disableItemRendering() {
    GlStateManager.disableLighting();
    GlStateManager.enableDepth();
  }
  
  public static void clearColor() {
    GlStateManager.color(1.f, 1.f, 1.f, 1.f);
  }
  
  private static class RenderSettings {
    
    private static final double[] EMPTY_VECTOR3D = new double[]{0.D, 0.D, 0.D};
    private static final double[] EMPTY_VECTOR4D = new double[]{0.D, 0.D, 0.D, 0.D};
    
    private double[] color4d = EMPTY_VECTOR4D; // 0-3 = rgba
    private double[] scale3d = EMPTY_VECTOR3D; // 0-2 = xyz
    private double[] translate3d = EMPTY_VECTOR3D; // 0-2 = xyz
    private double[] rotated4d = EMPTY_VECTOR4D; // 0 = angle, 1-3 = xyz
    
    private boolean autoApply = true;
    
    private MinecraftFontRenderer fontRenderer = null;
    
    public double[] getColor4d() {
      return color4d;
    }
    
    public void setColor4d(double[] color4d) {
      this.color4d = color4d;
      if (autoApply) {
        applyColor();
      }
    }
    
    public double[] getScale3d() {
      return scale3d;
    }
    
    public void setScale3d(double[] scale3d) {
      this.scale3d = scale3d;
      if (autoApply) {
        applyScale();
      }
    }
    
    public double[] getTranslate3d() {
      return translate3d;
    }
    
    public void setTranslate3d(double[] translate3d) {
      this.translate3d = translate3d;
      if (autoApply) {
        applyTranslation();
      }
    }
    
    public double[] getRotated4d() {
      return rotated4d;
    }
    
    public void setRotated4d(double[] rotated4d) {
      this.rotated4d = rotated4d;
      if (autoApply) {
        applyRotation();
      }
    }
    
    public MinecraftFontRenderer getFontRenderer() {
      return fontRenderer;
    }
    
    public void setFontRenderer(MinecraftFontRenderer fontRenderer) {
      this.fontRenderer = fontRenderer;
    }
    
    public void setAutoApply(boolean autoApply) {
      this.autoApply = autoApply;
    }
    
    public boolean hasColor() {
      return color4d != EMPTY_VECTOR4D;
    }
    
    public boolean hasScale() {
      return scale3d != EMPTY_VECTOR3D;
    }
    
    public boolean hasTranslation() {
      return translate3d != EMPTY_VECTOR3D;
    }
    
    public boolean hasRotation() {
      return rotated4d != EMPTY_VECTOR4D;
    }
    
    public boolean hasFontRenderer() {
      return fontRenderer != null;
    }
    
    public void applyColor() {
      if (hasColor()) {
        glColor4d(color4d[0], color4d[1], color4d[2], color4d[3]);
      }
    }
    
    public void applyScale() {
      if (hasScale()) {
        glScaled(scale3d[0], scale3d[1], scale3d[2]);
      }
    }
    
    public void applyTranslation() {
      if (hasTranslation()) {
        glTranslated(translate3d[0], translate3d[1], translate3d[2]);
      }
    }
    
    public void applyRotation() {
      if (hasRotation()) {
        glRotated(rotated4d[0], rotated4d[1], rotated4d[2], rotated4d[3]);
      }
    }
    
    public void clearColor() {
      color4d = EMPTY_VECTOR4D;
    }
    
    public void clearScale() {
      scale3d = EMPTY_VECTOR3D;
    }
    
    public void clearTranslation() {
      translate3d = EMPTY_VECTOR3D;
    }
    
    public void clearRotation() {
      rotated4d = EMPTY_VECTOR4D;
    }
    
    public void clearFontRenderer() {
      fontRenderer = null;
    }
    
    public void resetColor() {
      if (hasColor()) {
        clearColor();
        glColor4d(1.D, 1.D, 1.D, 1.D);
      }
    }
    
    public void resetScale() {
      if (hasScale()) {
        clearScale();
        glScaled(1.D, 1.D, 1.D);
      }
    }
    
    public void resetTranslation() {
      if (hasTranslation()) {
        clearTranslation();
        glTranslated(0.D, 0.D, 0.D);
      }
    }
    
    public void resetRotation() {
      if (hasRotation()) {
        clearRotation();
        glRotated(0.D, 0.D, 0.D, 0.D);
      }
    }
  }
}
