package com.matt.forgehax.mods;

import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.util.math.AngleN;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.projectile.Projectile;
import com.matt.forgehax.util.projectile.SimulationResult;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class TrajectoryMod extends ToggleMod {
    public TrajectoryMod() {
        super(Category.RENDER, "Trajectory", false, "Draws projectile trajectory");
    }

    @SubscribeEvent
    public void onRender(RenderEvent event) {
        Projectile projectile = Projectile.getProjectileByItemStack(getLocalPlayer().getHeldItemMainhand());
        if(!projectile.isNull()) {
            SimulationResult result = projectile.getSimulatedTrajectoryFromEntity(getLocalPlayer(),
                    PositionRotationManager.getState().getActiveServerViewAngles(),
                    projectile.getForce(getLocalPlayer().getHeldItemMainhand().getMaxItemUseDuration() - getLocalPlayer().getItemInUseCount()),
                    0
            );
            if(result == null) return;

            if(result.getPathTraveled().size() > 1) {
                event.setTranslation(getLocalPlayer().getPositionVector());

                GlStateManager.enableDepth();
                GlStateManager.glLineWidth(2.0f);

                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

                Iterator<Vec3d> it = result.getPathTraveled().iterator();
                Vec3d previous = it.next();
                while (it.hasNext()) {
                    Vec3d next = it.next();
                    event.getBuffer()
                            .pos(previous.x, previous.y, previous.z)
                            .color(255, 255, 255, 255)
                            .endVertex();
                    event.getBuffer()
                            .pos(next.x, next.y, next.z)
                            .color(255, 255, 255, 255)
                            .endVertex();
                    previous = next;
                }

                event.getTessellator().draw();
                GL11.glDisable(GL11.GL_LINE_SMOOTH);

                GlStateManager.glLineWidth(1.0f);
                GlStateManager.disableDepth();

                event.resetTranslation();
            }
        }
    }
}
