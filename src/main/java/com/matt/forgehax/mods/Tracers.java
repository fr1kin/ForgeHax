package com.matt.forgehax.mods;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.util.vector.Vector2f;

import java.util.Objects;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

/**
 * Created on 8/6/2017 by fr1kin
 */
@RegisterMod
public class Tracers extends ToggleMod {
    public Tracers() {
        super("Tracers", false, "See where other players are");
    }

    @SubscribeEvent
    public void onDrawScreen(RenderGameOverlayEvent.Text event) {
        final Vector2f origin = new Vector2f(0.f, 0.f);
        final Vector2f center = new Vector2f(MC.displayWidth / 4.f, MC.displayHeight / 4.f);
        getWorld().loadedEntityList.stream()
                .filter(entity -> !Objects.equals(entity, getLocalPlayer()))
                .filter(entity -> entity instanceof EntityLiving)
                .forEach(entity -> {
                    Vec3d pos3d = EntityUtils.getInterpolatedEyePos(entity, MC.getRenderPartialTicks());
                    VectorUtils.ScreenPos pos = VectorUtils.toScreen(pos3d);
                    if(!pos.isVisible) {
                        double ex = (pos.x - center.x) / center.x;
                        double ey = (pos.y - center.y) / center.y;

                        double m = Math.abs(Math.sqrt(ex*ex + ey*ey));
                        double nx = ex / m;
                        double ny = ey / m;

                        double x = center.x + nx * center.x;
                        double y = center.y + ny * center.y;

                        // point - center
                        // w = <px - cx, py - cy>
                        Vector2f w = new Vector2f((float) x, (float) y);
                        w.x -= center.x;
                        w.y -= center.y;

                        // u = <w, h/2>
                        Vector2f u = new Vector2f(MC.displayWidth / 2.f, origin.y);

                        // |u|
                        float mu = u.length();
                        // |w|
                        float mw = w.length();

                        // theta = dot(u,w)/(|u|*|w|)
                        float ang = (float) Math.toDegrees(Math.acos((u.x*w.x + u.y*w.y)/(mu*mw)));

                        // invert if above
                        if(y < center.y) ang *= -1;

                        // normalize
                        ang = (float) AngleHelper.normalizeInDegrees(ang);

                        SurfaceUtils.drawTriangle((int) x, (int) y, 5, ang, Utils.Colors.GREEN);
                    }
                });
    }
}
