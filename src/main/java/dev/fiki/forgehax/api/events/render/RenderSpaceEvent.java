package dev.fiki.forgehax.api.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.vector.Vector3d;

@Getter
@AllArgsConstructor
public class RenderSpaceEvent extends Event {
  private final MatrixStack stack;
  private final Vector3d renderPos;
  private final float partialTicks;

  public BufferBuilder getBuffer() {
    return Tessellator.getInstance().getBuilder();
  }

  public Vector3d getProjectedPos() {
    return renderPos; // TODO: find out waht this was
  }
}
