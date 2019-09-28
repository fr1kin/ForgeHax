package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 5/5/2017 by fr1kin
 */
public class PrePostBlockModelRenderEvent extends Event {
  
  public enum State {
    PRE,
    POST,
    ;
  }
  
  private final RenderChunk renderChunk;
  private final BufferBuilder buffer;
  private final Vec3d pos;
  
  private final State state;
  
  public PrePostBlockModelRenderEvent(
    RenderChunk renderChunk, BufferBuilder BufferBuilder, State state, Vec3d pos) {
    this.renderChunk = renderChunk;
    this.buffer = BufferBuilder;
    this.state = state;
    this.pos = pos;
  }
  
  public PrePostBlockModelRenderEvent(
    RenderChunk renderChunk, BufferBuilder BufferBuilder, State state, BlockPos pos) {
    this(renderChunk, BufferBuilder, state, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
  }
  
  public PrePostBlockModelRenderEvent(
    RenderChunk renderChunk,
    BufferBuilder BufferBuilder,
    State state,
    float x,
    float y,
    float z) {
    this(renderChunk, BufferBuilder, state, new Vec3d(x, y, z));
  }
  
  public RenderChunk getRenderChunk() {
    return renderChunk;
  }
  
  public BufferBuilder getBuffer() {
    return buffer;
  }
  
  public State getState() {
    return state;
  }
  
  public Vec3d getPos() {
    return pos;
  }
}
