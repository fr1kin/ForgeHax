package com.matt.forgehax.util.draw;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.entity.EntityUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderUtils implements Globals {
  
  public static Vec3d getRenderPos() {
    return new Vec3d(
        MC.player.lastTickPosX
            + (MC.player.posX - MC.player.lastTickPosX) * MC.getRenderPartialTicks(),
        MC.player.lastTickPosY
            + (MC.player.posY - MC.player.lastTickPosY) * MC.getRenderPartialTicks(),
        MC.player.lastTickPosZ
            + (MC.player.posZ - MC.player.lastTickPosZ) * MC.getRenderPartialTicks());
  }
  
  public static void drawLine(
      Vec3d startPos, Vec3d endPos, int color, boolean smooth, float width) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder BufferBuilder = tessellator.getBuffer();
    
    Vec3d endVecPos = endPos.subtract(startPos);
    
    float r = (float) (color >> 16 & 255) / 255.0F;
    float g = (float) (color >> 8 & 255) / 255.0F;
    float b = (float) (color & 255) / 255.0F;
    float a = (float) (color >> 24 & 255) / 255.0F;
    
    if (smooth) {
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }
    
    GL11.glLineWidth(width);
    
    GlStateManager.pushMatrix();
    GlStateManager.translate(startPos.x, startPos.y, startPos.z);
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    
    BufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    BufferBuilder.pos(0, 0, 0).color(r, g, b, a).endVertex();
    BufferBuilder.pos(endVecPos.x, endVecPos.y, endVecPos.z).color(r, g, b, a).endVertex();
    tessellator.draw();
    
    if (smooth) {
      GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
    
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }
  
  // thanks again Gregor
  public static void drawBox(
      Vec3d startPos, Vec3d endPos, int color, float width, boolean ignoreZ) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    
    Vec3d renderPos = EntityUtils.getInterpolatedPos(getLocalPlayer(), MC.getRenderPartialTicks());
    
    Vec3d min = startPos.subtract(renderPos);
    Vec3d max = endPos.subtract(renderPos);
    
    double minX = min.x, minY = min.y, minZ = min.z;
    double maxX = max.x, maxY = max.y, maxZ = max.z;
    
    float r = (float) (color >> 16 & 255) / 255.0F;
    float g = (float) (color >> 8 & 255) / 255.0F;
    float b = (float) (color & 255) / 255.0F;
    float a = (float) (color >> 24 & 255) / 255.0F;
    
    GlStateManager.pushMatrix();
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    GlStateManager.glLineWidth(width);
    
    if (ignoreZ) {
      GlStateManager.disableDepth();
    }
    
    GlStateManager.color(r, g, b, a);
    
    // GlStateManager.translate(startPos.xCoord, startPos.yCoord, startPos.zCoord);
    
    buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
    buffer.pos(minX, minY, minZ).endVertex();
    buffer.pos(maxX, minY, minZ).endVertex();
    buffer.pos(maxX, minY, maxZ).endVertex();
    buffer.pos(minX, minY, maxZ).endVertex();
    buffer.pos(minX, minY, minZ).endVertex();
    tessellator.draw();
    buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
    buffer.pos(minX, maxY, minZ).endVertex();
    buffer.pos(maxX, maxY, minZ).endVertex();
    buffer.pos(maxX, maxY, maxZ).endVertex();
    buffer.pos(minX, maxY, maxZ).endVertex();
    buffer.pos(minX, maxY, minZ).endVertex();
    tessellator.draw();
    buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
    buffer.pos(minX, minY, minZ).endVertex();
    buffer.pos(minX, maxY, minZ).endVertex();
    buffer.pos(maxX, minY, minZ).endVertex();
    buffer.pos(maxX, maxY, minZ).endVertex();
    buffer.pos(maxX, minY, maxZ).endVertex();
    buffer.pos(maxX, maxY, maxZ).endVertex();
    buffer.pos(minX, minY, maxZ).endVertex();
    buffer.pos(minX, maxY, maxZ).endVertex();
    tessellator.draw();
    
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.disableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
    GlStateManager.enableDepth();
    GlStateManager.enableCull();
    GlStateManager.popMatrix();
  }
  
  public static void drawBox(
      BlockPos startPos, BlockPos endPos, int color, float width, boolean ignoreZ) {
    drawBox(
        new Vec3d(startPos.getX(), startPos.getY(), startPos.getZ()),
        new Vec3d(endPos.getX(), endPos.getY(), endPos.getZ()),
        color,
        width,
        ignoreZ);
  }
}
