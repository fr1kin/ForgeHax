package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.events.RenderEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.projectile.Projectile;
import dev.fiki.forgehax.main.util.projectile.SimulationResult;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static dev.fiki.forgehax.main.Common.getGameRenderer;
import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "Trajectory",
    description = "Draws projectile trajectory",
    category = Category.RENDER
)
public class TrajectoryMod extends ToggleMod {

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    Projectile projectile =
        Projectile.getProjectileByItemStack(getLocalPlayer().getHeldItemMainhand());
    if (!projectile.isNull()) {
      SimulationResult result =
          projectile.getSimulatedTrajectoryFromEntity(
              getLocalPlayer(),
              LocalPlayerUtils.getViewAngles(),
              projectile.getForce(
                  getLocalPlayer().getHeldItemMainhand().getUseDuration()
                      - getLocalPlayer().getItemInUseCount()),
              0);
      if (result == null) {
        return;
      }

      if (result.getPathTraveled().size() > 1) {
        pushMatrix();
        enableDepthTest();
        lineWidth(2.0f);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        BufferBuilderEx buffer = event.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        Vector3d pos = getGameRenderer().getActiveRenderInfo().getRenderViewEntity().getEyePosition(1.f);
        event.getBuffer().setTranslation(pos.scale(-1d));

        Iterator<Vector3d> it = result.getPathTraveled().iterator();
        Vector3d previous = it.next();
        while (it.hasNext()) {
          Vector3d next = it.next();
          buffer.pos(previous.getX(), previous.getY(), previous.getZ())
              .color(255, 255, 255, 255)
              .endVertex();
          buffer.pos(next.x, next.y, next.z)
              .color(255, 255, 255, 255)
              .endVertex();
          previous = next;
        }


        buffer.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        lineWidth(1.0f);
        disableDepthTest();

        popMatrix();
      }
    }
  }
}
