package com.matt.forgehax.util.draw;

import com.matt.forgehax.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created on 9/2/2017 by fr1kin
 */
public class SurfaceBuilder {
    private static final float[] EMPTY_COLOR = new float[] {0.f, 0.f, 0.f, 0.f};

    private static final SurfaceBuilder INSTANCE = new SurfaceBuilder();

    public static SurfaceBuilder getBuilder() {
        return INSTANCE;
    }

    // --------------------

    private final float[] color4f = Arrays.copyOf(EMPTY_COLOR, EMPTY_COLOR.length);

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

    public SurfaceBuilder push() {
        GlStateManager.pushMatrix();
        return this;
    }

    public SurfaceBuilder pop() {
        System.arraycopy(EMPTY_COLOR, 0, color4f, 0, color4f.length);
        GlStateManager.popMatrix();
        return this;
    }

    public SurfaceBuilder color(float r, float g, float b, float a) {
        color4f[0] = r;
        color4f[1] = g;
        color4f[2] = b;
        color4f[3] = a;
        glColor4f(r, g, b, a);
        return this;
    }
    public SurfaceBuilder color(int buffer) {
        return color(
                (buffer >> 16 & 255) / 255.0F,
                (buffer >> 8 & 255) / 255.0F,
                (buffer & 255) / 255.0F,
                (buffer >> 24 & 255) / 255.0F
        );
    }
    public SurfaceBuilder color(int r, int g, int b, int a) {
        return color(r / 255.f, g / 255.f, b / 255.f, a / 255.f);
    }

    public SurfaceBuilder scale(double x, double y, double z) {
        glScaled(Math.max(x, 0), Math.max(y, 0), Math.max(z, 0));
        return this;
    }
    public SurfaceBuilder scale(double s) {
        return scale(s, s, s);
    }
    public SurfaceBuilder scale() {
        return scale(0.D);
    }

    public SurfaceBuilder translate(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        return this;
    }

    public SurfaceBuilder rotate(double angle, double x, double y, double z) {
        glRotated(angle, x, y, z);
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

    private SurfaceBuilder text(MinecraftFontRenderer renderer, String text, double x, double y, boolean shadow) {
        renderer.drawString(text, x, y, Utils.toRGBA(color4f), shadow);
        return this;
    }
    private SurfaceBuilder text(MinecraftFontRenderer renderer, String text, double x, double y) {
        return text(renderer, text, x, y, false);
    }
    private SurfaceBuilder textWithShadow(MinecraftFontRenderer renderer, String text, double x, double y) {
        return text(renderer, text, x, y, true);
    }

    public SurfaceBuilder task(Runnable task) {
        task.run();
        return this;
    }

    // --------------------

    public static void preRenderSetup() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void postRenderSetup() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
