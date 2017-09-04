package com.matt.forgehax.mods;

import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.mobtypes.MobTypeEnum;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.math.Plane;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

/**
 * Created on 8/6/2017 by fr1kin
 */
@RegisterMod
public class Tracers extends ToggleMod {
    public Tracers() {
        super(Category.RENDER, "Tracers", false, "See where other players are");
    }

    @SubscribeEvent
    public void onDrawScreen(Render2DEvent event) {
        final double center = 0.D;
        final double cx = MC.displayWidth / 4.f;
        final double cy = MC.displayHeight / 4.f;
        getWorld().loadedEntityList.stream()
                .filter(entity -> !Objects.equals(entity, getLocalPlayer()))
                .filter(entity -> entity instanceof EntityLivingBase)
                .filter(entity -> !EntityUtils.getRelationship(entity).equals(MobTypeEnum.INVALID))
                .sorted((o1, o2) ->
                {
                    MobTypeEnum r1 = EntityUtils.getRelationship(o1);
                    MobTypeEnum r2 = EntityUtils.getRelationship(o2);
                    return r2.compareTo(r1);
                })
                .forEach(entity -> {
                    Vec3d pos3d = EntityUtils.getInterpolatedEyePos(entity, MC.getRenderPartialTicks());
                    Plane pos = VectorUtils.toScreen(pos3d);
                    if(!pos.isVisible()) {
                        // get position on ellipse

                        // dimensions of the ellipse
                        final double dx = cx - 2;
                        final double dy = cy - 20;

                        // ellipse = x^2/a^2 + y^2/b^2 = 1
                        // e = (pos - C) / d
                        //  C = center vector
                        //  d = dimensions
                        double ex = (pos.getX() - cx) / dx;
                        double ey = (pos.getY() - cy) / dy;

                        // normalize
                        // n = u/|u|
                        double m = Math.abs(Math.sqrt(ex*ex + ey*ey));
                        double nx = ex / m;
                        double ny = ey / m;

                        // scale
                        // p = C + dot(n,d)
                        double x = cx + nx * dx;
                        double y = cy + ny * dy;

                        // --------------------
                        // now rotate triangle

                        // point - center
                        // w = <px - cx, py - cy>
                        double wx = x - cx;
                        double wy = y - cy;

                        // u = <w, 0>
                        double ux = MC.displayWidth / 2.f;
                        double uy = 0.D;

                        // |u|
                        double mu = Math.sqrt(ux*ux + uy*uy);
                        // |w|
                        double mw = Math.sqrt(wx*wx + wy*wy);

                        // theta = dot(u,w)/(|u|*|w|)
                        double ang = Math.toDegrees(Math.acos((ux*wx + uy*wy)/(mu*mw)));

                        // don't allow NaN angles
                        if(ang == Float.NaN) ang = 0;

                        // invert
                        if(y < cy) ang *= -1;

                        // normalize
                        ang = (float) AngleHelper.normalizeInDegrees(ang);

                        // --------------------

                        int color;
                        int size;
                        switch (EntityUtils.getRelationship(entity)) {
                            case PLAYER:
                                color = Utils.Colors.YELLOW;
                                size = 8;
                                break;
                            case HOSTILE:
                                color = Utils.Colors.RED;
                                size = 5;
                                break;
                            case NEUTRAL:
                                color = Utils.Colors.BLUE;
                                size = 5;
                                break;
                            default:
                                color = Utils.Colors.GREEN;
                                size = 5;
                                break;
                        }

                        event.getSurfaceBuilder().clear()
                                .push()
                                .task(SurfaceBuilder::preBlend)
                                .task(SurfaceBuilder::preRenderTexture2D)
                                .task(() -> GL11.glEnable(GL11.GL_POLYGON_SMOOTH))
                                .color(color)
                                .translate(x, y, 0.D)
                                .rotate(ang, 0.D, 0.D, size / 2.D)
                                .apply()
                                .begin(GL11.GL_TRIANGLES)
                                .vertex(0, 0)
                                .vertex(-size, -size)
                                .vertex(-size, size)
                                .end()
                                .task(SurfaceBuilder::postBlend)
                                .task(SurfaceBuilder::postRenderTexture2D)
                                .task(() -> GL11.glDisable(GL11.GL_POLYGON_SMOOTH))
                                .pop();

                        /*
                        if (EntityUtils.isPlayer(entity)) {
                            ResourceLocation resourceLocation = AbstractClientPlayer.getLocationSkin(entity.getName());
                            AbstractClientPlayer.getDownloadImageSkin(resourceLocation, entity.getName());
                            SurfaceHelper.drawHead(resourceLocation, (int)x - 6, (int)y - 6, 1);
                        }
                        else {
                            SurfaceHelper.drawTriangle((int) x, (int) y, size, (float) ang, color);
                        }*/
                    }
                });
    }
}
