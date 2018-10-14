package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getPlayerController;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.mods.managers.PositionRotationManager.RotationState;
import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.AngleHelper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.projectile.Projectile;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

@RegisterMod
public class Aimbot extends ToggleMod implements PositionRotationManager.MovementUpdateListener {
  private static Entity target = null;

  public static void setTarget(Entity target) {
    Aimbot.target = target;
  }

  public static Entity getTarget() {
    return target;
  }

  enum Selector {
    CROSSHAIR,
    DISTANCE,
  }

  public final Setting<Boolean> silent =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("silent")
          .description("Wont look at target when aiming")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> auto_attack =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("auto-attack")
          .description("Automatically attack when target found")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> hold_target =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("hold-target")
          .description("Keep first caught target until it becomes no longer valid")
          .defaultTo(false)
          .build();

  public final Setting<Boolean> vis_check =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("trace")
          .description("Check if the target is visible before acquiring")
          .defaultTo(false)
          .build();

  public final Setting<Boolean> target_players =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("target-players")
          .description("Target players")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> target_mobs_hostile =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("target-hostile-mobs")
          .description("Target hostile mobs")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> target_mobs_friendly =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("target-friendly-mobs")
          .description("Target friendly mobs")
          .defaultTo(false)
          .build();

  public final Setting<Boolean> lag_compensation =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("lag-compensation")
          .description("Compensate for server lag")
          .defaultTo(true)
          .build();

  public final Setting<Integer> fov =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("fov")
          .description("Aimbot field of view")
          .defaultTo(180)
          .min(0)
          .max(180)
          .build();

  public final Setting<Double> range =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("range")
          .description("Aimbot range")
          .defaultTo(4.5D)
          .build();

  public final Setting<Double> cooldown_percent =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("cooldown_percent")
          .description("Minimum cooldown percent for next strike")
          .defaultTo(100D)
          .build();

  public final Setting<Boolean> projectile_aimbot =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("proj-aimbot")
          .description("Projectile aimbot")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> projectile_auto_attack =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("proj-auto-attack")
          .description("Automatically attack when target found for projectile weapons")
          .defaultTo(true)
          .build();

  public final Setting<Boolean> projectile_trace_check =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("projectile-trace")
          .description("Check the trace of each target if holding a weapon that fires a projectile")
          .defaultTo(true)
          .build();

  public final Setting<Double> projectile_range =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("projectile-range")
          .description("Projectile aimbot range")
          .defaultTo(100D)
          .build();

  public final Setting<Selector> selector =
      getCommandStub()
          .builders()
          .<Selector>newSettingEnumBuilder()
          .name("selector")
          .description("The method used to select a target from a group")
          .defaultTo(Selector.CROSSHAIR)
          .build();

  public Aimbot() {
    super(Category.COMBAT, "Aimbot", false, "Automatically attack entities and players");
  }

  private double getLagComp() {
    if (lag_compensation.get()) {
      return -(20.D - TickRateService.getTickData().getPoint().getAverage());
    } else return 0.D;
  }

  private boolean canAttack(EntityPlayer localPlayer, Entity target) {
    return localPlayer.getCooledAttackStrength((float) getLagComp())
            >= (cooldown_percent.get() / 100.D)
        && (auto_attack.get() || Bindings.attack.getBinding().isKeyDown()); // need to work on this
  }

  private Projectile getHeldProjectile() {
    return Projectile.getProjectileByItemStack(getLocalPlayer().getHeldItem(EnumHand.MAIN_HAND));
  }

  private boolean isHoldingProjectileItem() {
    return !getHeldProjectile().isNull();
  }

  private boolean isProjectileAimbotActivated() {
    return projectile_aimbot.get() && isHoldingProjectileItem();
  }

  private boolean isVisible(Entity target) {
    if (isProjectileAimbotActivated() && projectile_trace_check.get()) {
      return getHeldProjectile().canHitEntity(EntityUtils.getEyePos(getLocalPlayer()), target);
    } else return !vis_check.get() || getLocalPlayer().canEntityBeSeen(target);
  }

  private Vec3d getAttackPosition(Entity entity) {
    return EntityUtils.getInterpolatedPos(entity, 1).addVector(0, entity.getEyeHeight() / 2, 0);
  }

  /** Check if the entity is a valid target to acquire */
  private boolean filterTarget(Vec3d pos, Vec3d viewNormal, Angle angles, Entity entity) {
    final Vec3d tpos = getAttackPosition(entity);
    return Optional.of(entity)
        .filter(EntityUtils::isLiving)
        .filter(EntityUtils::isAlive)
        .filter(EntityUtils::isValidEntity)
        .filter(ent -> !ent.equals(getLocalPlayer()))
        .filter(this::isFiltered)
        .filter(ent -> isInRange(tpos, pos))
        .filter(ent -> isInFov(angles, tpos.subtract(pos)))
        .filter(this::isVisible)
        .isPresent();
  }

  private boolean isFiltered(Entity entity) {
    switch (EntityUtils.getRelationship(entity)) {
      case PLAYER:
        return target_players.get();
      case FRIENDLY:
      case NEUTRAL:
        return target_mobs_friendly.get();
      case HOSTILE:
        return target_mobs_hostile.get();
      case INVALID:
      default:
        return false;
    }
  }

  private boolean isInRange(Vec3d from, Vec3d to) {
    double dist = isProjectileAimbotActivated() ? projectile_range.get() : range.get();
    return dist <= 0 || from.distanceTo(to) <= dist;
  }

  private boolean isInFov(Angle angle, Vec3d pos) {
    double fov = this.fov.get();
    if (fov >= 180) return true;
    else {
      Angle look = AngleHelper.getAngleFacingInDegrees(pos);
      Angle diff = angle.sub(look.getPitch(), look.getYaw()).normalize();
      return Math.abs(diff.getPitch()) <= fov && Math.abs(diff.getYaw()) <= fov;
    }
  }

  private double selecting(
      final Vec3d pos, final Vec3d viewNormal, final Angle angles, final Entity entity) {
    switch (selector.get()) {
      case DISTANCE:
        return getAttackPosition(entity).subtract(pos).lengthSquared();
      case CROSSHAIR:
      default:
        return getAttackPosition(entity)
            .subtract(pos)
            .normalize()
            .subtract(viewNormal)
            .lengthSquared();
    }
  }

  private Entity findTarget(final Vec3d pos, final Vec3d viewNormal, final Angle angles) {
    return getWorld()
        .loadedEntityList
        .stream()
        .filter(entity -> filterTarget(pos, viewNormal, angles, entity))
        .min(Comparator.comparingDouble(entity -> selecting(pos, viewNormal, angles, entity)))
        .orElse(null);
  }

  @Override
  protected void onEnabled() {
    PositionRotationManager.getManager().register(this, PriorityEnum.HIGHEST);
  }

  @Override
  public void onDisabled() {
    PositionRotationManager.getManager().unregister(this);
  }

  @Override
  public void onLocalPlayerMovementUpdate(RotationState.Local state) {
    Vec3d pos = EntityUtils.getEyePos(getLocalPlayer());
    Vec3d look = getLocalPlayer().getLookVec();
    Angle angles = AngleHelper.getAngleFacingInDegrees(look);

    Entity t = getTarget();
    if (!hold_target.get()
        || t == null
        || !filterTarget(pos, look.normalize(), angles, getTarget()))
      setTarget(t = findTarget(pos, look.normalize(), angles));

    if (t == null) return;

    final Entity tar = t;
    Projectile projectile = getHeldProjectile();

    if (projectile.isNull() || !projectile_aimbot.get()) {
      // melee aimbot
      Angle va = Utils.getLookAtAngles(t).normalize();
      state.setViewAngles(va, silent.get());

      if (canAttack(getLocalPlayer(), tar))
        state.invokeLater(
            rs -> {
              InvItem previous = AutoTool.getInstance().selectBestWeapon();

              getPlayerController().attackEntity(getLocalPlayer(), tar);
              getLocalPlayer().swingArm(EnumHand.MAIN_HAND);

              LocalPlayerInventory.setSelected(previous);
            });
    }
  }
}
