package dev.fiki.forgehax.main.mods.render;

import com.mojang.blaze3d.platform.GlStateManager;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.entity.RelationState;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.math.AngleUtil;
import dev.fiki.forgehax.api.math.ScreenPos;
import dev.fiki.forgehax.api.math.VectorUtil;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.Common;
import lombok.experimental.ExtensionMethod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.Objects;
import java.util.stream.StreamSupport;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;

@RegisterMod(
    name = "Tracers",
    description = "See where other players are",
    category = Category.RENDER
)
@ExtensionMethod({EntityEx.class})
public class Tracers extends ToggleMod implements Colors {

  enum Mode {
    ARROWS,
    LINES,
    BOTH,
    ;
  }

  public final EnumSetting<Mode> mode = newEnumSetting(Mode.class)
      .name("mode")
      .description("Tracer drawing mode")
      .defaultTo(Mode.ARROWS)
      .build();

  public final IntegerSetting alpha = newIntegerSetting()
      .name("alpha")
      .description("Alpha value of the tracer color")
      .defaultTo(255)
      .min(0)
      .max(255)
      .build();

  public final BooleanSetting antialias = newBooleanSetting()
      .name("antialias")
      .description("Makes lines and triangles more smooth, but may hurt performance")
      .defaultTo(true)
      .build();

  public final BooleanSetting players = newBooleanSetting()
      .name("players")
      .description("trace players")
      .defaultTo(true)
      .build();

  public final BooleanSetting hostile = newBooleanSetting()
      .name("hostile")
      .description("trace hostile mobs")
      .defaultTo(true)
      .build();

  public final BooleanSetting neutral = newBooleanSetting()
      .name("neutral")
      .description("trace neutral mobs")
      .defaultTo(true)
      .build();

  public final BooleanSetting friendly = newBooleanSetting()
      .name("friendly")
      .description("trace friendly mobs")
      .defaultTo(true)
      .build();

  @SubscribeListener
  public void onDrawScreen(RenderPlaneEvent.Back event) {

    enableBlend();
    blendFuncSeparate(
        GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    disableTexture();

    if (antialias.getValue()) {
      GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    final Mode dm = mode.getValue();

    final double cx = event.getScreenWidth() / 2.f;
    final double cy = event.getScreenHeight() / 2.f;

    StreamSupport.stream(Common.getWorld().entitiesForRendering().spliterator(), false)
        .filter(entity -> !Objects.equals(entity, Common.getLocalPlayer()))
        .filter(LivingEntity.class::isInstance)
        .map(EntityRelations::new)
        .filter(er -> !er.getRelationship().equals(RelationState.INVALID))
        .filter(EntityRelations::isOptionEnabled)
        .forEach(
            er -> {
              Entity entity = er.getEntity();
              RelationState relationship = er.getRelationship();

              Vector3d entityPos = entity.getInterpolatedEyePos(MC.getDeltaFrameTime());
              ScreenPos screenPos = VectorUtil.toScreen(entityPos);

              Color color = er.getColor().setAlpha(alpha.getValue());
              color4f(color.getRedAsFloat(),
                  color.getGreenAsFloat(),
                  color.getBlueAsFloat(),
                  color.getAlphaAsFloat());

              translatef(0, 0, er.getDepth());

              if (dm.equals(Mode.BOTH) || dm.equals(Mode.ARROWS)) {
                if (!screenPos.isVisible()) {
                  // get position on ellipse

                  // dimensions of the ellipse
                  final double dx = cx - 2;
                  final double dy = cy - 20;

                  // ellipse = x^2/a^2 + y^2/b^2 = 1
                  // e = (pos - C) / d
                  //  C = center vector
                  //  d = dimensions
                  double ex = (screenPos.getX() - cx) / dx;
                  double ey = (screenPos.getY() - cy) / dy;

                  // normalize
                  // n = u/|u|
                  double m = Math.abs(Math.sqrt(ex * ex + ey * ey));
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
                  double ux = event.getScreenWidth();
                  double uy = 0.D;

                  // |u|
                  double mu = Math.sqrt(ux * ux + uy * uy);
                  // |w|
                  double mw = Math.sqrt(wx * wx + wy * wy);

                  // theta = dot(u,w)/(|u|*|w|)
                  double ang = Math.toDegrees(Math.acos((ux * wx + uy * wy) / (mu * mw)));

                  // don't allow NaN angles
                  if (ang == Float.NaN) {
                    ang = 0;
                  }

                  // invert
                  if (y < cy) {
                    ang *= -1;
                  }

                  // normalize
                  ang = (float) AngleUtil.normalizeInDegrees(ang);

                  // --------------------

                  int size = relationship.equals(RelationState.PLAYER) ? 8 : 5;

                  pushMatrix();

                  translated(x, y, 0);
                  rotatef((float) ang, 0.f, 0.f, size / 2.f);

                  color4f(
                      color.getRedAsFloat(),
                      color.getGreenAsFloat(),
                      color.getBlueAsFloat(),
                      color.getAlphaAsFloat());

                  glBegin(GL11.GL_TRIANGLES);
                  {
                    GL11.glVertex2d(0, 0);
                    GL11.glVertex2d(-size, -size);
                    GL11.glVertex2d(-size, size);
                  }
                  glEnd();

                  popMatrix();
                }
              }

              if (dm.equals(Mode.BOTH) || dm.equals(Mode.LINES)) {
                glBegin(GL11.GL_LINES);
                {
                  GL11.glVertex2d(cx, cy);
                  GL11.glVertex2d(screenPos.getX(), screenPos.getY());
                }
                glEnd();
              }

              translated(0, 0, -er.getDepth());
            });

    enableTexture();
    disableBlend();

    GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
    GL11.glDisable(GL11.GL_LINE_SMOOTH);

    color4f(1.f, 1.f, 1.f, 1.f);
  }

  private class EntityRelations implements Comparable<EntityRelations> {

    private final Entity entity;
    private final RelationState relationship;

    public EntityRelations(Entity entity) {
      Objects.requireNonNull(entity);
      this.entity = entity;
      this.relationship = entity.getPlayerRelationship();
    }

    public Entity getEntity() {
      return entity;
    }

    public RelationState getRelationship() {
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

    public float getDepth() {
      switch (relationship) {
        case PLAYER:
          return 15.f;
        case HOSTILE:
          return 10.f;
        case NEUTRAL:
          return 5.f;
        default:
          return 0.f;
      }
    }

    public boolean isOptionEnabled() {
      switch (relationship) {
        case PLAYER:
          return players.getValue();
        case HOSTILE:
          return hostile.getValue();
        case NEUTRAL:
          return neutral.getValue();
        default:
          return friendly.getValue();
      }
    }

    @Override
    public int compareTo(EntityRelations o) {
      return getRelationship().compareTo(o.getRelationship());
    }
  }
}
