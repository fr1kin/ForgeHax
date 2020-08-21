package dev.fiki.forgehax.main.util.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraftforge.eventbus.api.Event;

import static dev.fiki.forgehax.main.Common.getBufferProvider;

/**
 * Created on 9/2/2017 by fr1kin
 */
@Getter
@AllArgsConstructor
public class Render2DEvent extends Event {
  private final float partialTicks;
  private final MatrixStack matrixStack;

  public BufferBuilderEx getBuffer() {
    return getBufferProvider().getDefaultBuffer();
  }

  public float getPartialTicks() {
    return partialTicks;
  }
  
  public int getScreenWidth() {
    return Common.getScreenWidth();
  }
  
  public int getScreenHeight() {
    return Common.getScreenHeight();
  }
}
