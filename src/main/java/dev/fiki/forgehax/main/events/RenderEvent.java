package dev.fiki.forgehax.main.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.Event;

import static dev.fiki.forgehax.main.Common.MC;

/**
 * Created on 5/5/2017 by fr1kin
 */
@Getter
public class RenderEvent extends Event {
  private final MatrixStack matrixStack;
  private final Vec3d projectedPos;
  private final float partialTicks;
  private final BufferBuilderEx buffer;

  public RenderEvent(MatrixStack matrixStack, Tessellator tessellator, Vec3d projectedPos, float partialTicks) {
    this.matrixStack = matrixStack;
    this.buffer = new BufferBuilderEx(tessellator);
    this.projectedPos = projectedPos;
    this.partialTicks = MC.getRenderPartialTicks();
  }

  public Tessellator getTessellator() {
    return buffer.getTessellator();
  }
}
