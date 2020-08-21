package dev.fiki.forgehax.main.util.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import lombok.Getter;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.Event;

import static dev.fiki.forgehax.main.Common.MC;
import static dev.fiki.forgehax.main.Common.getBufferProvider;

/**
 * Created on 5/5/2017 by fr1kin
 */
@Getter
public class RenderEvent extends Event {
  private final MatrixStack matrixStack;
  private final Vector3d projectedPos;
  private final float partialTicks;

  public RenderEvent(MatrixStack matrixStack, Vector3d projectedPos, float partialTicks) {
    this.matrixStack = matrixStack;
    this.projectedPos = projectedPos;
    this.partialTicks = MC.getRenderPartialTicks();
  }

  public BufferBuilderEx getBuffer() {
    return getBufferProvider().getDefaultBuffer();
  }
}
