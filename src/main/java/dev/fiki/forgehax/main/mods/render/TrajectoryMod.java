package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.projectile.Projectile;
import dev.fiki.forgehax.api.projectile.SimulationResult;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

import static dev.fiki.forgehax.main.Common.getGameRenderer;
import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "Trajectory",
    description = "Draws projectile trajectory",
    category = Category.RENDER
)
@ExtensionMethod({LocalPlayerEx.class, VectorEx.class, VertexBuilderEx.class})
public class TrajectoryMod extends ToggleMod {
  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    final ClientPlayerEntity lp = getLocalPlayer();
    final Projectile projectile = Projectile.getProjectileByItemStack(lp.getMainHandItem());
    if (!projectile.isNull()) {
      final SimulationResult result = projectile.getSimulatedTrajectoryFromEntity(
          lp, lp.getViewAngles(),
          projectile.getForce(lp.getMainHandItem().getUseDuration() - lp.getUseItemRemainingTicks()),
          0);
      if (result == null) {
        return;
      }

      if (result.getPathTraveled().size() > 1) {
        final MatrixStack stack = event.getStack();
        final BufferBuilder buffer = event.getBuffer();

        buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);

        final Vector3d pos = getGameRenderer().getMainCamera().getEntity().getEyePosition(1.f);
        stack.pushPose();
        stack.translateVec(pos.scale(-1d));

        final Iterator<Vector3d> it = result.getPathTraveled().iterator();
        Vector3d previous = it.next();
        while (it.hasNext()) {
          final Vector3d next = it.next();
          buffer.line(previous, next, Colors.WHITE, stack.getLastMatrix());
          previous = next;
        }

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        buffer.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableDepthTest();
        stack.popPose();
      }
    }
  }
}
