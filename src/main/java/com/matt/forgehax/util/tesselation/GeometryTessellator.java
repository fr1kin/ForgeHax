package com.matt.forgehax.util.tesselation;

/*
   The MIT License (MIT)

   Copyright (c) 2014-2016 Jadran "Lunatrius" Kotnik

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
*/

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class GeometryTessellator extends Tessellator {
  
  private static GeometryTessellator instance = null;
  
  private static double deltaS = 0;
  private double delta = 0;
  
  public GeometryTessellator() {
    this(0x200000);
  }
  
  public GeometryTessellator(final int size) {
    super(size);
  }
  
  public static GeometryTessellator getInstance() {
    if (instance == null) {
      instance = new GeometryTessellator();
    }
    
    return instance;
  }
  
  public void setTranslation(final double x, final double y, final double z) {
    getBuffer().setTranslation(x, y, z);
  }
  
  public void beginQuads() {
    begin(GL11.GL_QUADS);
  }
  
  public void beginLines() {
    begin(GL11.GL_LINES);
  }
  
  public void begin(final int mode) {
    getBuffer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
  }
  
  @Override
  public void draw() {
    super.draw();
  }
  
  public void setDelta(final double delta) {
    this.delta = delta;
  }
  
  public static void setStaticDelta(final double delta) {
    deltaS = delta;
  }
  
  public void drawCuboid(final BlockPos pos, final int sides, final int argb) {
    drawCuboid(pos, pos, sides, argb);
  }
  
  public void drawCuboid(
    final BlockPos begin, final BlockPos end, final int sides, final int argb) {
    drawCuboid(getBuffer(), begin, end, sides, argb, this.delta);
  }
  
  public static void drawCuboid(
    final BufferBuilder buffer, final BlockPos pos, final int sides, final int argb) {
    drawCuboid(buffer, pos, pos, sides, argb);
  }
  
  public static void drawCuboid(
    final BufferBuilder buffer,
    final BlockPos begin,
    final BlockPos end,
    final int sides,
    final int argb) {
    drawCuboid(buffer, begin, end, sides, argb, GeometryTessellator.deltaS);
  }
  
  private static void drawCuboid(
    final BufferBuilder buffer,
    final BlockPos begin,
    final BlockPos end,
    final int sides,
    final int argb,
    final double delta) {
    if (buffer.getDrawMode() == -1 || sides == 0) {
      return;
    }
    
    final double x0 = begin.getX() - delta;
    final double y0 = begin.getY() - delta;
    final double z0 = begin.getZ() - delta;
    final double x1 = end.getX() + 1 + delta;
    final double y1 = end.getY() + 1 + delta;
    final double z1 = end.getZ() + 1 + delta;
    
    switch (buffer.getDrawMode()) {
      case GL11.GL_QUADS:
        drawQuads(buffer, x0, y0, z0, x1, y1, z1, sides, argb);
        break;
      
      case GL11.GL_LINES:
        drawLines(buffer, x0, y0, z0, x1, y1, z1, sides, argb);
        break;
      
      default:
        throw new IllegalStateException("Unsupported mode!");
    }
  }
  
  public static void drawQuads(
    final BufferBuilder buffer,
    final double x0,
    final double y0,
    final double z0,
    final double x1,
    final double y1,
    final double z1,
    final int sides,
    final int argb) {
    final int a = (argb >>> 24) & 0xFF;
    final int r = (argb >>> 16) & 0xFF;
    final int g = (argb >>> 8) & 0xFF;
    final int b = argb & 0xFF;
    
    drawQuads(buffer, x0, y0, z0, x1, y1, z1, sides, a, r, g, b);
  }
  
  public static void drawQuads(
    final BufferBuilder buffer,
    final double x0,
    final double y0,
    final double z0,
    final double x1,
    final double y1,
    final double z1,
    final int sides,
    final int a,
    final int r,
    final int g,
    final int b) {
    if ((sides & GeometryMasks.Quad.DOWN) != 0) {
      buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Quad.UP) != 0) {
      buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Quad.NORTH) != 0) {
      buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
      buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Quad.WEST) != 0) {
      buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Quad.EAST) != 0) {
      buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
    }
  }
  
  public static void drawLines(
    final BufferBuilder buffer,
    final double x0,
    final double y0,
    final double z0,
    final double x1,
    final double y1,
    final double z1,
    final int sides,
    final int argb) {
    final int a = (argb >>> 24) & 0xFF;
    final int r = (argb >>> 16) & 0xFF;
    final int g = (argb >>> 8) & 0xFF;
    final int b = argb & 0xFF;
    
    drawLines(buffer, x0, y0, z0, x1, y1, z1, sides, a, r, g, b);
  }
  
  public static void drawLines(
    final BufferBuilder buffer,
    final double x0,
    final double y0,
    final double z0,
    final double x1,
    final double y1,
    final double z1,
    final int sides,
    final int a,
    final int r,
    final int g,
    final int b) {
    if ((sides & GeometryMasks.Line.DOWN_WEST) != 0) {
      buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.UP_WEST) != 0) {
      buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.DOWN_EAST) != 0) {
      buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.UP_EAST) != 0) {
      buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.DOWN_NORTH) != 0) {
      buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.UP_NORTH) != 0) {
      buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.DOWN_SOUTH) != 0) {
      buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.UP_SOUTH) != 0) {
      buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.NORTH_WEST) != 0) {
      buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.NORTH_EAST) != 0) {
      buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.SOUTH_WEST) != 0) {
      buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
    }
    
    if ((sides & GeometryMasks.Line.SOUTH_EAST) != 0) {
      buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
      buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
    }
  }
}
