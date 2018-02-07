package com.matt.forgehax.mods;

import com.matt.forgehax.events.Render2DEvent;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.mobtypes.MobTypeEnum;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.math.Plane;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
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
public class Tracers extends ToggleMod implements Colors {
    public Tracers() {
        super(Category.RENDER, "Tracers", false, "See where other players are");
    }

    public final Setting<Boolean> drawArrows = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("drawArrows").description("draw arrows to off-screen entities")
            .defaultTo(true).build();

    public final Setting<Boolean> drawLines = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("drawLines").description("draw lines to entities")
            .defaultTo(false).build();

    public final Setting<Double> opacity = getCommandStub().builders().<Double>newSettingBuilder()
            .name("opacity").description("transparency of the tracers")
            .defaultTo(1D)
            .min(0D)
            .max(1D)
            .build();

    public final Setting<Boolean> players = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("players").description("trace players")
            .defaultTo(true).build();

    public final Setting<Boolean> hostile = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("hostile").description("trace hostile mobs")
            .defaultTo(true).build();

    public final Setting<Boolean> neutral = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("neutral").description("trace neutral mobs")
            .defaultTo(true).build();

    public final Setting<Boolean> friendly = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("friendly").description("trace friendly mobs")
            .defaultTo(true).build();

    @SubscribeEvent
    public void onDrawScreen(Render2DEvent event) {
        getWorld().loadedEntityList.stream()
                .filter(entity -> !Objects.equals(entity, getLocalPlayer()))
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(EntityRelations::new)
                .filter(w -> w.getRelationship().equals(MobTypeEnum.INVALID))
                .filter(EntityRelations::isOptionEnabled)
                .sorted()
                .forEach(w -> {
                    if (drawArrows.getAsBoolean()) drawArrow(event, w);
                    if (drawLines.getAsBoolean()) drawLine(event, w);
                });
    }

    private void drawArrow(Render2DEvent event, EntityRelations w) {
        final Entity entity = w.getEntity();
        final MobTypeEnum relationship = w.getRelationship();

        final double cx = MC.displayWidth / 4.f;
        final double cy = MC.displayHeight / 4.f;
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

            int size = 5;
            if (relationship == MobTypeEnum.PLAYER) {
                size = 8;
            }

            event.getSurfaceBuilder().reset()
                    .push()
                    .task(SurfaceBuilder::enableBlend)
                    .task(SurfaceBuilder::disableTexture2D)
                    .task(() -> GL11.glEnable(GL11.GL_POLYGON_SMOOTH))
                    .color(w.getColor().setAlpha((int)(255 * opacity.get())).toBuffer())
                    .translate(x, y, 0.D)
                    .rotate(ang, 0.D, 0.D, size / 2.D)
                    .begin(GL11.GL_TRIANGLES)
                    .vertex(0, 0)
                    .vertex(-size, -size)
                    .vertex(-size, size)
                    .end()
                    .task(SurfaceBuilder::disableBlend)
                    .task(SurfaceBuilder::enableTexture2D)
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
    }

    private void drawLine(Render2DEvent event, EntityRelations w) {
        final Entity entity = w.getEntity();
        final MobTypeEnum relation = w.getRelationship();

        final double cx = MC.displayWidth / 4.f;
        final double cy = MC.displayHeight / 4.f;
        Vec3d pos3d = EntityUtils.getInterpolatedEyePos(entity, MC.getRenderPartialTicks());
        Plane pos = VectorUtils.toScreen(pos3d);
        int size = 1;
        if (relation == MobTypeEnum.PLAYER) {
            size = 2;
        }
        SurfaceHelper.drawLine((int)cx, (int)cy, (int)pos.getX(), (int)pos.getY(), w.getColor().setAlpha((int)(255 * opacity.get())).toBuffer(), size);
    }

    private class EntityRelations implements Comparable<EntityRelations> {
        private final Entity entity;
        private final MobTypeEnum relationship;

        public EntityRelations(Entity entity) {
            Objects.requireNonNull(entity);
            this.entity = entity;
            this.relationship = EntityUtils.getRelationship(entity);
        }

        public Entity getEntity() {
            return entity;
        }

        public MobTypeEnum getRelationship() {
            return relationship;
        }

        public Color getColor() {
            switch (relationship) {
                case PLAYER:
                    return YELLOW;
                case HOSTILE:
                    return RED;
                case NEUTRAL:
                    return BLUE;
                default:
                    return GREEN;
            }
        }

        public boolean isOptionEnabled() {
            switch (relationship) {
                case PLAYER:
                    return players.get();
                case HOSTILE:
                    return hostile.get();
                case NEUTRAL:
                    return neutral.get();
                default:
                    return friendly.get();
            }
        }

        @Override
        public int compareTo(EntityRelations o) {
            return getRelationship().compareTo(o.getRelationship());
        }
    }
}
