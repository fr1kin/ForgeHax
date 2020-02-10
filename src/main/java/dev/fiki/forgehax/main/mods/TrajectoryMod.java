package dev.fiki.forgehax.main.mods;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.projectile.Projectile;
import dev.fiki.forgehax.main.util.projectile.SimulationResult;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager;

import java.util.Iterator;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class TrajectoryMod extends ToggleMod {

  public TrajectoryMod() {
    super(Category.RENDER, "Trajectory", false, "Draws projectile trajectory");
  }

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

        BufferBuilder buffer = event.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        Vec3d pos = getGameRenderer().getActiveRenderInfo().getRenderViewEntity().getEyePosition(1.f);
        event.getBuffer().setTranslation(pos.scale(-1d));

        Iterator<Vec3d> it = result.getPathTraveled().iterator();
        Vec3d previous = it.next();
        while (it.hasNext()) {
          Vec3d next = it.next();
          buffer.pos(previous.getX(), previous.getY(), previous.getZ())
              .color(255, 255, 255, 255)
              .endVertex();
          buffer.pos(next.x, next.y, next.z)
              .color(255, 255, 255, 255)
              .endVertex();
          previous = next;
        }


        event.getTessellator().draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        lineWidth(1.0f);
        disableDepthTest();

        popMatrix();
      }
    }
  }
}
