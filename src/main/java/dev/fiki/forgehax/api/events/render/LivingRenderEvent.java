package dev.fiki.forgehax.api.events.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;

@Getter
@AllArgsConstructor
public class LivingRenderEvent<T extends LivingEntity, M extends EntityModel<T>> extends Event {
  private final LivingRenderer<T, M> renderer;
  private final float partialRenderTick;
  private final MatrixStack matrixStack;
  private final IRenderTypeBuffer buffers;
  private final int light;
  private final LivingEntity living;

  @Cancelable
  public static class Pre<T extends LivingEntity, M extends EntityModel<T>> extends LivingRenderEvent<T, M> {
    public Pre(LivingRenderer<T, M> renderer, float partialRenderTick, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, LivingEntity living) {
      super(renderer, partialRenderTick, matrixStack, buffers, light, living);
    }
  }

  public static class Post<T extends LivingEntity, M extends EntityModel<T>> extends LivingRenderEvent<T, M> {
    public Post(LivingRenderer<T, M> renderer,
        float partialRenderTick, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light, LivingEntity living) {
      super(renderer, partialRenderTick, matrixStack, buffers, light, living);
    }
  }
}
