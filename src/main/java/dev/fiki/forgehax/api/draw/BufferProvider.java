package dev.fiki.forgehax.api.draw;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;

import java.util.Map;

@Getter
public class BufferProvider {
  private final IRenderTypeBuffer.Impl bufferSource;
  private final BufferBuilder defaultBuffer = Tessellator.getInstance().getBuilder();

  public BufferProvider() {
    BufferMap buffers = new BufferMap()
        .add(RenderTypeEx.glLines())
        .add(RenderTypeEx.glTriangle())
        .add(RenderTypeEx.glQuads())
        .add(RenderTypeEx.blockTranslucentCull())
        .add(RenderTypeEx.blockCutout())
        .add(RenderType.glint())
        .add(RenderType.entityGlint())
        ;

    this.bufferSource = IRenderTypeBuffer.immediateWithBuffers(buffers.build(), defaultBuffer);
  }

  public BufferBuilder getBuffer(RenderType renderType) {
    return (BufferBuilder) getBufferSource().getBuffer(renderType);
  }

  private static class BufferMap {
    Map<RenderType, BufferBuilder> buffers = new Object2ObjectLinkedOpenHashMap<>();

    public BufferMap add(RenderType type) {
      buffers.put(type, new BufferBuilder(type.bufferSize()));
      return this;
    }

    public Map<RenderType, BufferBuilder> build() {
      return buffers;
    }
  }
}
