package dev.fiki.forgehax.main.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.util.tesselation.BufferBuilderEx;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created on 5/5/2017 by fr1kin
 */
@Getter
public class RenderEvent extends Event {

  private final MatrixStack matrixStack;
  private final Tessellator tessellator;
  private final Vec3d projectionPos;
  private final float partialTicks;

  @Getter(AccessLevel.NONE)
  private BufferBuilderEx bufferBuilderEx;

  public RenderEvent(MatrixStack matrixStack, Tessellator tessellator, Vec3d projectionPos, float partialTicks) {
    this.matrixStack = matrixStack;
    this.tessellator = tessellator;
    this.projectionPos = projectionPos;
    this.partialTicks = partialTicks;
  }

  public BufferBuilderEx getBuffer() {
    if(bufferBuilderEx == null) {
      bufferBuilderEx = new BufferBuilderEx(tessellator.getBuffer());
    }
    return bufferBuilderEx;
  }
}
