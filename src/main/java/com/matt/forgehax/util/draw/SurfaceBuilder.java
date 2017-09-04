package com.matt.forgehax.util.draw;

import com.matt.forgehax.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.Helper.getLocalPlayer;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created on 9/2/2017 by fr1kin
 */
public class SurfaceBuilder {
    private static final double[] EMPTY_VECTOR3D = new double[] {0.D, 0.D, 0.D};
    private static final double[] EMPTY_VECTOR4D = new double[] {0.D, 0.D, 0.D, 0.D};

    private static final SurfaceBuilder INSTANCE = new SurfaceBuilder();

    public static SurfaceBuilder getBuilder() {
        return INSTANCE;
    }

    // --------------------

    private double[] color4d = EMPTY_VECTOR4D; // 0-3 = rgba
    private double[] scale3d = EMPTY_VECTOR3D; // 0-2 = xyz
    private double[] translate3d = EMPTY_VECTOR3D; // 0-2 = xyz
    private double[] rotated4d = EMPTY_VECTOR4D; // 0 = angle, 1-3 = xyz

    private MinecraftFontRenderer fontRenderer = null;

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

    public SurfaceBuilder apply() {
        if(color4d != EMPTY_VECTOR4D) glColor4d(color4d[0], color4d[1], color4d[2], color4d[3]);
        if(scale3d != EMPTY_VECTOR3D) glScaled(scale3d[0], scale3d[1], scale3d[2]);
        if(translate3d != EMPTY_VECTOR3D) glTranslated(translate3d[0], translate3d[1], translate3d[2]);
        if(rotated4d != EMPTY_VECTOR4D) glRotated(rotated4d[0], rotated4d[1], rotated4d[2], rotated4d[3]);
        return this;
    }
    
    public SurfaceBuilder clear() {
        // reset to what should be the original values... they might not be though
        if(color4d != EMPTY_VECTOR4D) {
            glColor4d(1.D, 1.D, 1.D, 1.D);
            color4d = EMPTY_VECTOR4D;
        }
        if(scale3d != EMPTY_VECTOR3D) {
            glScaled(1.D, 1.D, 1.D);
            scale3d = EMPTY_VECTOR3D;
        }
        if(translate3d != EMPTY_VECTOR3D) {
            glTranslated(0.D, 0.D, 0.D);
            translate3d = EMPTY_VECTOR3D;
        }
        if(rotated4d != EMPTY_VECTOR4D) {
            glRotated(0.D, 0.D, 0.D, 0.D);
            rotated4d = EMPTY_VECTOR4D;
        }
        fontRenderer = null;
        return this;
    }

    public SurfaceBuilder push() {
        GlStateManager.pushMatrix();
        return this;
    }

    public SurfaceBuilder pop() {
        GlStateManager.popMatrix();
        return this;
    }

    public SurfaceBuilder color(double r, double g, double b, double a) {
        color4d = new double[] {r, g, b, a};
        return this;
    }
    public SurfaceBuilder color(int buffer) {
        return color(
                (buffer >> 16 & 255) / 255.D,
                (buffer >> 8 & 255) / 255.D,
                (buffer & 255) / 255.D,
                (buffer >> 24 & 255) / 255.D
        );
    }
    public SurfaceBuilder color(int r, int g, int b, int a) {
        return color(r / 255.D, g / 255.D, b / 255.D, a / 255.D);
    }

    public SurfaceBuilder scale(double x, double y, double z) {
        scale3d = new double[] {Math.max(x, 0), Math.max(y, 0), Math.max(z, 0)};
        return this;
    }
    public SurfaceBuilder scale(double s) {
        return scale(s, s, s);
    }
    public SurfaceBuilder scale() {
        return scale(0.D);
    }

    public SurfaceBuilder translate(double x, double y, double z) {
        translate3d = new double[] {x, y, z};
        return this;
    }

    public SurfaceBuilder rotate(double angle, double x, double y, double z) {
        rotated4d = new double[] {angle, x, y, z};
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
        return vertex(x, y, 0.D);
    }

    public SurfaceBuilder line(double startX, double startY, double endX, double endY) {
        return vertex(startX, startY)
                .vertex(endX, endY);
    }

    public SurfaceBuilder rectangle(double x, double y, double w, double h) {
        return vertex(x, y)
                .vertex(x, y + h)
                .vertex(x + w, y + h)
                .vertex(x + w, y);
    }

    public SurfaceBuilder fontRenderer(MinecraftFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
        return this;
    }

    private SurfaceBuilder text(String text, double x, double y, boolean shadow) {
        if(this.fontRenderer != null) // use custom font renderer
            this.fontRenderer.drawString(text, x, y, Utils.toRGBA(color4d), shadow);
        else // use default minecraft font
            MC.fontRenderer.drawString(text, Math.round(x), Math.round(y), Utils.toRGBA(color4d), shadow);
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
        SurfaceHelper.renderItemAndEffectIntoGUI(getLocalPlayer(), stack, x, y, scale3d != EMPTY_VECTOR3D ? scale3d[0] : 16.D);
        MC.getRenderItem().zLevel = 0.f;
        return this;
    }

    public SurfaceBuilder itemOverlay(ItemStack stack, double x, double y) {
        SurfaceHelper.renderItemOverlayIntoGUI(MC.fontRenderer, stack, x, y, null, scale3d != EMPTY_VECTOR3D ? scale3d[0] : 16.D);
        return this;
    }

    public SurfaceBuilder head(ResourceLocation resource, double x, double y) {
        MC.renderEngine.bindTexture(resource);
        double scale = scale3d != EMPTY_VECTOR3D ? scale3d[0] : 12.D;
        SurfaceHelper.drawScaledCustomSizeModalRect((x * (1 / scale)), (y * (1 / scale)),
                8.0F, 8.0F,
                8, 8,
                12, 12,
                64.0F, 64.0F
        );
        SurfaceHelper.drawScaledCustomSizeModalRect((x * (1 / scale)), (y * (1 / scale)),
                40.0F, 8.0F,
                8, 8,
                12, 12,
                64.0F, 64.0F
        );
        return this;
    }

    public int getFontWidth(String text) {
        return fontRenderer != null ? fontRenderer.getStringWidth(text) : MC.fontRenderer.getStringWidth(text);
    }

    public int getFontHeight() {
        return fontRenderer != null ? fontRenderer.getHeight() : MC.fontRenderer.FONT_HEIGHT;
    }
    public int getFontHeight(String text) {
        return getFontHeight();
    }

    private double _getScaled(int index, double p) {
        return p * (1.D / scale3d[index]);
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

    public static void preRenderTexture2D() {
        GlStateManager.disableTexture2D();
    }

    public static void postRenderTexture2D() {
        GlStateManager.enableTexture2D();
    }

    public static void preBlend() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void postBlend() {
        GlStateManager.disableBlend();
    }

    public static void preFontRender() {
        GlStateManager.disableDepth();
    }

    public static void postFontRender() {
        GlStateManager.enableDepth();
    }

    public static void preItemRender() {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableLighting();
    }

    public static void postItemRender() {
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
    }
}
