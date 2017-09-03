package com.matt.forgehax.util.draw;

import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

import static com.matt.forgehax.Helper.getLocalPlayer;

/**
 * 2D rendering
 */
public class SurfaceHelper implements Globals {
    public static void drawLine(int startX, int startY, int endX, int endY, int color) {
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        float a = (float)(color >> 24 & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder BufferBuilder = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);

        BufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        BufferBuilder.pos((double)startX, (double)startY, 0.0D).endVertex();
        BufferBuilder.pos((double)endX, (double)endY, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRect(int x, int y, int w, int h, int color) {
        GL11.glLineWidth(1.0f);
        Gui.drawRect(x, y, x + w, y + h, color);
    }

    public static void drawOutlinedRect(int x, int y, int w, int h, int color, float width) {
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        float a = (float)(color >> 24 & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder BufferBuilder = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);

        GL11.glLineWidth(width);

        BufferBuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        BufferBuilder.pos((double)x, (double)y, 0.0D).endVertex();
        BufferBuilder.pos((double)x, (double)y + h, 0.0D).endVertex();
        BufferBuilder.pos((double)x + w, (double)y + h, 0.0D).endVertex();
        BufferBuilder.pos((double)x + w, (double)y, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawOutlinedRect(int x, int y, int w, int h, int color) {
        drawOutlinedRect(x, y, w, h, color, 1.f);
    }

    public static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height, int zLevel) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder BufferBuilder = tessellator.getBuffer();
        BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        BufferBuilder.pos((double)(x + 0), (double)(y + height), (double)zLevel).tex((double) ((float) (textureX + 0) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        BufferBuilder.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double) ((float) (textureX + width) * 0.00390625F), (double) ((float) (textureY + height) * 0.00390625F)).endVertex();
        BufferBuilder.pos((double)(x + width), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        BufferBuilder.pos((double)(x + 0), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    public static void drawTriangle(int x, int y, int size, float rotate, int color) {
        GlStateManager.pushMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);

        GlStateManager.translate(x, y, 0.f);
        GlStateManager.rotate(rotate, 0.f, 0.f, size / 2.f);

        int[] colors = Utils.toRGBAArray(color);
        GlStateManager.color(colors[0], colors[1], colors[2], colors[3]);

        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);

        builder.pos(0, 0, 0).endVertex();
        builder.pos(-size, -size, 0).endVertex();
        builder.pos(-size, size, 0).endVertex();

        tessellator.draw();

        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
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
        MC.fontRenderer.drawString(msg, (int)(x * (1/scale)), (int)(y * (1/scale)), color, shadow);
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
        return (int)(MC.fontRenderer.getStringWidth(text) * scale);
    }

    public static int getTextWidth(String text) {
        return getTextWidth(text, 1.D);
    }

    public static int getTextHeight() {
        return MC.fontRenderer.FONT_HEIGHT;
    }

    public static int getTextHeight(double scale) {
        return (int)(MC.fontRenderer.FONT_HEIGHT * scale);
    }

    public static void drawItem(ItemStack item, double x, double y) {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableLighting();
        MC.getRenderItem().zLevel = 100.f;
        renderItemAndEffectIntoGUI(getLocalPlayer(), item, x, y);
        MC.getRenderItem().zLevel = 0.f;
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.color(1.f, 1.f, 1.f, 1.f);
    }

    public static void drawItemWithOverlay(ItemStack item, double x, double y) {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableLighting();
        MC.getRenderItem().zLevel = 100.f;
        renderItemAndEffectIntoGUI(getLocalPlayer(), item, x, y);
        renderItemOverlayIntoGUI(MC.fontRenderer, item, x, y, null);
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
        Gui.drawScaledCustomSizeModalRect((int) (x * (1 / scale)), (int) (y * (1 / scale)),
                8.0F, 8.0F,
                8, 8,
                12, 12,
                64.0F, 64.0F
        );
        Gui.drawScaledCustomSizeModalRect((int) (x * (1 / scale)), (int) (y * (1 / scale)),
                40.0F, 8.0F,
                8, 8,
                12, 12,
                64.0F, 64.0F
        );
        GlStateManager.popMatrix();
    }

    protected static void renderItemAndEffectIntoGUI(@Nullable EntityLivingBase living, final ItemStack stack, double x, double y) {
        if(!stack.isEmpty()) {
            MC.getRenderItem().zLevel += 50.f;
            try {
                renderItemModelIntoGUI(stack, x, y, MC.getRenderItem().getItemModelWithOverrides(stack, null, living));
            } catch (Throwable t) {
                Helper.handleThrowable(t);
            } finally {
                MC.getRenderItem().zLevel -= 50.f;
            }
        }
    }

    private static void renderItemModelIntoGUI(ItemStack stack, double x, double y, IBakedModel bakedmodel)
    {
        GlStateManager.pushMatrix();
        MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate(x, y, 100.0F + MC.getRenderItem().zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(16.0F, 16.0F, 16.0F);

        if (bakedmodel.isGui3d())
            GlStateManager.enableLighting();
        else
            GlStateManager.disableLighting();

        bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
        MC.getRenderItem().renderItem(stack, bakedmodel);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    protected static void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, double xPosition, double yPosition, @Nullable String text)
    {
        if (!stack.isEmpty())
        {
            SurfaceBuilder builder = new SurfaceBuilder();

            if (stack.getCount() != 1 || text != null)
            {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawStringWithShadow(s, (float)(xPosition + 19 - 2 - fr.getStringWidth(s)), (float)(yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                // Fixes opaque cooldown overlay a bit lower
                // TODO: check if enabled blending still screws things up down the line.
                GlStateManager.enableBlend();
            }

            if (stack.getItem().showDurabilityBar(stack))
            {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
                int i = Math.round(13.0F - (float)health * 13.0F);
                int j = rgbfordisplay;
                draw(builder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(builder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
            float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

            if (f3 > 0.0F)
            {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                draw(builder, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
    }

    private static void draw(SurfaceBuilder builder, double x, double y, double width, double height, int red, int green, int blue, int alpha)
    {
        SurfaceBuilder.getBuilder().clear()
                .push()
                .beginQuads()
                .color(red, green, blue, alpha)
                .apply()
                .rectangle(x, y, width, height)
                .end()
                .pop();
    }

    public static int getHeadWidth(float scale) {
        return (int)(scale * 12);
    }
    public static int getHeadWidth() {
        return getHeadWidth(1.f);
    }

    public static int getHeadHeight(float scale) {
        return (int)(scale * 12);
    }
    public static int getHeadHeight() {
        return getHeadWidth(1.f);
    }
}
