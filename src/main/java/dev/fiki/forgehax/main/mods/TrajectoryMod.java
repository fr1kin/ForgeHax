package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.projectile.Projectile;
import dev.fiki.forgehax.main.util.projectile.SimulationResult;
import dev.fiki.forgehax.main.mods.managers.PositionRotationManager;

import java.util.Iterator;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static com.mojang.blaze3d.systems.RenderSystem.*;

@RegisterMod
public class TrajectoryMod extends ToggleMod {
  
  public TrajectoryMod() {
    super(Category.RENDER, "Trajectory", false, "Draws projectile trajectory");
  }
  
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    Projectile projectile =
        Projectile.getProjectileByItemStack(Globals.getLocalPlayer().getHeldItemMainhand());
    if (!projectile.isNull()) {
      SimulationResult result =
          projectile.getSimulatedTrajectoryFromEntity(
              Globals.getLocalPlayer(),
              PositionRotationManager.getState().getRenderServerViewAngles(),
              projectile.getForce(
                  Globals.getLocalPlayer().getHeldItemMainhand().getUseDuration()
                      - Globals.getLocalPlayer().getItemInUseCount()),
              0);
      if (result == null) {
        return;
      }
      
      if (result.getPathTraveled().size() > 1) {
        event.setTranslation(Globals.getLocalPlayer().getPositionVector());
        
        enableDepthTest();
        lineWidth(2.0f);
        
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        
        Iterator<Vec3d> it = result.getPathTraveled().iterator();
        Vec3d previous = it.next();
        while (it.hasNext()) {
          Vec3d next = it.next();
          event
              .getBuffer()
              .pos(previous.x, previous.y, previous.z)
              .color(255, 255, 255, 255)
              .endVertex();
          event.getBuffer().pos(next.x, next.y, next.z).color(255, 255, 255, 255).endVertex();
          previous = next;
        }
        
        event.getTessellator().draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        
        lineWidth(1.0f);
        disableDepthTest();
        
        event.resetTranslation();
      }
    }
  }
}
