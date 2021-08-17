package dev.fiki.forgehax.main.mods.combat;

import dev.fiki.forgehax.api.cmd.settings.*;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.PlayerRotationEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.key.BindingHelper;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.projectile.Projectile;
import dev.fiki.forgehax.main.services.TickRateService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Comparator;
import java.util.Objects;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "Aimbot",
    description = "Automatically attack entities and players",
    category = Category.COMBAT
)
@RequiredArgsConstructor
@ExtensionMethod({Objects.class, LocalPlayerEx.class, EntityEx.class, VectorEx.class})
public class Aimbot extends ToggleMod {
  enum Selector {
    CROSSHAIR,
    DISTANCE,
  }

  private final BooleanSetting silent = newBooleanSetting()
      .name("silent")
      .description("Wont look at target when aiming")
      .defaultTo(true)
      .build();

  private final BooleanSetting autoAttack = newBooleanSetting()
      .name("auto-attack")
      .description("Automatically attack when target found")
      .defaultTo(true)
      .build();

  private final BooleanSetting holdTarget = newBooleanSetting()
      .name("hold-target")
      .description("Keep first caught target until it becomes no longer valid")
      .defaultTo(false)
      .build();

  private final BooleanSetting visCheck = newBooleanSetting()
      .name("trace")
      .description("Check if the target is visible before acquiring")
      .defaultTo(false)
      .build();

  private final BooleanSetting targetPlayers = newBooleanSetting()
      .name("target-players")
      .description("Target players")
      .defaultTo(true)
      .build();

  private final BooleanSetting targetHostile = newBooleanSetting()
      .name("target-hostile-mobs")
      .description("Target hostile mobs")
      .defaultTo(true)
      .build();

  private final BooleanSetting targetFriendly = newBooleanSetting()
      .name("target-friendly-mobs")
      .description("Target friendly mobs")
      .defaultTo(false)
      .build();

  private final BooleanSetting lagCompensation = newBooleanSetting()
      .name("lag-compensation")
      .description("Compensate for server lag")
      .defaultTo(true)
      .build();

  private final IntegerSetting fov = newIntegerSetting()
      .name("fov")
      .description("Aimbot field of view")
      .defaultTo(180)
      .min(0)
      .max(180)
      .build();

  private final DoubleSetting range = newDoubleSetting()
      .name("range")
      .description("Aimbot range")
      .defaultTo(4.5D)
      .build();

  private final FloatSetting cooldownPercent = newFloatSetting()
      .name("cooldown_percent")
      .description("Minimum cooldown percent for next strike")
      .defaultTo(100F)
      .min(0F)
      .build();

  private final BooleanSetting projectileAimbot = newBooleanSetting()
      .name("proj-aimbot")
      .description("Projectile aimbot")
      .defaultTo(false)
      .build();

  private final BooleanSetting projectileAutoAttack = newBooleanSetting()
      .name("proj-auto-attack")
      .description("Automatically attack when target found for projectile weapons")
      .defaultTo(true)
      .build();

  private final BooleanSetting projectileTraceCheck = newBooleanSetting()
      .name("projectile-trace")
      .description("Check the trace of each target if holding a weapon that fires a projectile")
      .defaultTo(true)
      .build();

  private final DoubleSetting projectileRange = newDoubleSetting()
      .name("projectile-range")
      .description("Projectile aimbot range")
      .defaultTo(100D)
      .build();

  private final EnumSetting<Selector> selector = newEnumSetting(Selector.class)
      .name("selector")
      .description("The method used to select a target from a group")
      .defaultTo(Selector.CROSSHAIR)
      .build();

  private final TickRateService tickRateService;

  @Getter
  @Setter
  private Entity target = null;

  private double getLagComp() {
    if (lagCompensation.getValue()) {
      return -(20.D - tickRateService.getTickrate());
    } else {
      return 0.D;
    }
  }

  private boolean canAttack(ClientPlayerEntity localPlayer, Entity target) {
    final float cdRatio = cooldownPercent.getValue() / 100F;
    final float cdOffset = cdRatio <= 1F ? 0F : -(localPlayer.getLuck() * (cdRatio - 1F));
    return localPlayer.getAttackStrengthScale((float) getLagComp() + cdOffset)
        >= (Math.min(1F, cdRatio))
        && (autoAttack.getValue() || getGameSettings().keyAttack.isDown()); // need to work on this
  }

  private Projectile getHeldProjectile() {
    return Projectile.getProjectileByItemStack(getLocalPlayer().getItemInHand(Hand.MAIN_HAND));
  }

  private boolean isHoldingProjectileItem() {
    return !getHeldProjectile().isNull();
  }

  private boolean isProjectileAimbotActivated() {
//    return projectileAimbot.getValue() && isHoldingProjectileItem();
    return false;
  }

  private boolean isVisible(Entity target) {
    if (isProjectileAimbotActivated() && projectileTraceCheck.getValue()) {
      return getHeldProjectile().canHitEntity(getLocalPlayer().getEyePos(), target);
    } else {
      return !visCheck.getValue() || getLocalPlayer().canSee(target);
    }
  }

  private Vector3d getAttackPosition(Entity entity) {
    return entity.getInterpolatedEyePos(1);
//    return entity.getEyePos();
  }

  /**
   * Check if the entity is a valid target to acquire
   */
  private boolean filterTarget(Vector3d pos, Vector3d viewNormal, Angle angles, Entity entity) {
    final Vector3d aimingPos = getAttackPosition(entity);
    return entity.nonNull()
        && entity.showVehicleHealth()
        && entity.isReallyAlive()
        && entity.isValidEntity()
        && !entity.isLocalPlayer()
        && isFiltered(entity)
        && isInRange(aimingPos, pos)
        && isInFov(angles, aimingPos.subtract(pos))
        && isVisible(entity);
  }

  private boolean isFiltered(Entity entity) {
    switch (entity.getPlayerRelationship()) {
      case PLAYER:
        return targetPlayers.getValue();
      case FRIENDLY:
      case NEUTRAL:
        return targetFriendly.getValue();
      case HOSTILE:
        return targetHostile.getValue();
      case INVALID:
      default:
        return false;
    }
  }

  private boolean isInRange(Vector3d from, Vector3d to) {
    double dist = isProjectileAimbotActivated() ? projectileRange.getValue() : range.getValue();
    return dist <= 0 || from.distanceTo(to) <= dist;
  }

  private boolean isInFov(Angle angle, Vector3d pos) {
    double fov = this.fov.getValue();
    if (fov >= 180) {
      return true;
    } else {
      Angle look = pos.getAngleFacingInDegrees();
      Angle diff = angle.sub(look).normalize();
      return Math.abs(diff.getPitch()) <= fov && Math.abs(diff.getYaw()) <= fov;
    }
  }

  private double selecting(final Vector3d pos, final Vector3d viewNormal, final Angle angles, final Entity entity) {
    switch (selector.getValue()) {
      case DISTANCE:
        return getAttackPosition(entity).subtract(pos).lengthSqr();
      case CROSSHAIR:
      default:
        return getAttackPosition(entity)
            .subtract(pos)
            .normalize()
            .subtract(viewNormal)
            .lengthSqr();
    }
  }

  private Entity findTarget(final Vector3d pos, final Vector3d viewNormal, final Angle angles) {
    return worldEntities()
        .filter(entity -> filterTarget(pos, viewNormal, angles, entity))
        .min(Comparator.comparingDouble(entity -> selecting(pos, viewNormal, angles, entity)))
        .orElse(null);
  }

  @Override
  protected void onEnabled() {
    BindingHelper.disableContextHandler(getGameSettings().keyAttack);
  }

  @Override
  public void onDisabled() {
    BindingHelper.restoreContextHandler(getGameSettings().keyAttack);
  }

  @SubscribeListener(priority = PriorityEnum.HIGHEST)
  public void onLocalPlayerMovementUpdate(PlayerRotationEvent event) {
    val lp = getLocalPlayer();
    Vector3d pos = lp.getEyePos();
    Vector3d look = lp.getForward();
    Angle angles = look.getAngleFacingInDegrees();

    Entity t = getTarget();
    if (!holdTarget.getValue()
        || t == null
        || !filterTarget(pos, look.normalize(), angles, getTarget())) {
      setTarget(t = findTarget(pos, look.normalize(), angles));
    }

    if (t == null) {
      return;
    }

    final Entity tar = t;
    Projectile projectile = getHeldProjectile();

    if (projectile.isNull() || projectileAimbot.isDisabled()) {
      // melee aimbot
      Angle va = lp.getLookAngles(getAttackPosition(tar));
      event.setViewAngles(va);
      event.setSilent(silent.isEnabled());

      if (canAttack(lp, tar)) {
        event.onFocusGained(() -> {
          lp.attackEntity(tar);
          lp.swing(Hand.MAIN_HAND);
        });
      }
    }
  }
}
