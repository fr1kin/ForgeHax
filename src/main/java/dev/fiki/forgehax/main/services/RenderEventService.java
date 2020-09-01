package dev.fiki.forgehax.main.services;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.main.util.events.Render2DEvent;
import dev.fiki.forgehax.main.util.events.RenderEvent;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionMethod;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static dev.fiki.forgehax.main.Common.getGameRenderer;

@RegisterMod
@RequiredArgsConstructor
public class RenderEventService extends ServiceMod {
  @MethodMapping(parentClass = GameRenderer.class, value = "hurtCameraEffect")
  private final ReflectionMethod<Void> GameRenderer_hurtCameraEffect;

  @SubscribeEvent
  public void onRenderWorld(RenderWorldLastEvent event) {
    final GameRenderer gameRenderer = getGameRenderer();
    final ActiveRenderInfo activeRenderInfo = gameRenderer.getActiveRenderInfo();
    final float partialTicks = MC.getRenderPartialTicks();

    MatrixStack stack = new MatrixStack();
    stack.getLast().getMatrix().mul(gameRenderer.getProjectionMatrix(activeRenderInfo, partialTicks, true));
    GameRenderer_hurtCameraEffect.invoke(gameRenderer, stack, partialTicks);

    Matrix4f projectionMatrix = stack.getLast().getMatrix();
    VectorUtils.setProjectionViewMatrix(projectionMatrix, event.getMatrixStack().getLast().getMatrix());

    RenderSystem.pushMatrix();
    RenderSystem.multMatrix(event.getMatrixStack().getLast().getMatrix());

    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.disableAlphaTest();
    RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    RenderSystem.shadeModel(GL11.GL_SMOOTH);
    RenderSystem.disableDepthTest();

    RenderSystem.lineWidth(1.f);

    Vector3d projectedView = activeRenderInfo.getProjectedView();

    RenderEvent e = new RenderEvent(event.getMatrixStack(), projectedView, event.getPartialTicks());
    MinecraftForge.EVENT_BUS.post(e);

    RenderSystem.lineWidth(1.f);

    RenderSystem.shadeModel(GL11.GL_FLAT);
    RenderSystem.disableBlend();
    RenderSystem.enableAlphaTest();
    RenderSystem.enableTexture();
    RenderSystem.enableDepthTest();
    RenderSystem.enableCull();

    RenderSystem.popMatrix();
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Text event) {
    if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
      MinecraftForge.EVENT_BUS.post(new Render2DEvent(event.getPartialTicks(), event.getMatrixStack()));
      RenderSystem.color4f(1.f, 1.f, 1.f, 1.f); // reset color
    }
  }
}
