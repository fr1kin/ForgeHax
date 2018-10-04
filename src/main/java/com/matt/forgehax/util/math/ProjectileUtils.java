package com.matt.forgehax.util.math;

import static com.matt.forgehax.Helper.*;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/** thx victormeriqui and k0bra :))))) */
public class ProjectileUtils implements Globals {
  private static final int SIMULATION_ITERATIONS = 150;
  private static final int BESTPOS_ITERATIONS = 10;

  public static final double PROJECTILE_SHOOTPOS_OFFSET = 0.10000000149011612;

  /** The position the bow is fired at TODO: Fix up code */
  public static Vec3d getFiringPos(Entity entity) {
    return entity.getPositionVector(); // EntityUtils.getEyePos(entity);
  }

  /** Check if the item is a bow, snowball, egg, fishing rod, or ender pearl */
  public static boolean isThrowable(ItemStack itemStack) {
    if (itemStack == null) return false;
    switch (Item.getIdFromItem(itemStack.getItem())) {
      case 261: // bow
      case 332: // snowball
      case 344: // egg
      case 346: // fishing rod
      case 368: // ender pearl
        return true;
      default:
        return false;
    }
  }

  /** Check if the item is a bow */
  public static boolean isBow(ItemStack itemStack) {
    return itemStack != null && Item.getIdFromItem(itemStack.getItem()) == 261;
  }

  /** Check if the item can be used to attack */
  public static boolean isAttackableThrowable(ItemStack itemStack) {
    if (itemStack == null) return false;
    switch (Item.getIdFromItem(itemStack.getItem())) {
      case 261: // bow
      case 332: // snowball
      case 344: // egg
      case 346: // fishing rod
        return true;
      default:
        return false;
    }
  }

  /** Get the initial velocity of the item */
  public static double getForce(ItemStack itemStack) {
    double force = 0.D;
    switch (Item.getIdFromItem(itemStack.getItem())) {
      case 261: // bow
        {
          int duration = itemStack.getMaxItemUseDuration() - MC.player.getItemInUseCount();
          force = (double) duration / 20.0F;
          //   force = (force * force + force * 2.0F) / 3.0F;
          if (force > 1.0F) force = 1.0F;
          force *= 2d * 1.5d;
          break;
        }
      case 332: // snowball
      case 344: // egg
      case 346: // fishing rod
      case 368: // ender pearl
        force = 1.5;
        break;
    }
    return force;
  }

  public static double getMinForce(ItemStack itemStack) {
    switch (Item.getIdFromItem(itemStack.getItem())) {
      case 261: // bow
        return 0.15D;
      case 332: // snowball
      case 344: // egg
      case 346: // fishing rod
      case 368: // ender pearl
        return 1.5D;
    }
    return 0D;
  }

  public static double getMaxForce(ItemStack itemStack) {
    switch (Item.getIdFromItem(itemStack.getItem())) {
      case 261: // bow
        return 3.0D;
      case 332: // snowball
      case 344: // egg
      case 346: // fishing rod
      case 368: // ender pearl
        return 1.5D;
    }
    return 0D;
  }

  /** Get the acceleration (aka gravity) of the item */
  public static Vec3d getGravity(ItemStack itemStack) {
    Vec3d gravity = new Vec3d(0, 0, 0);
    switch (Item.getIdFromItem(itemStack.getItem())) {
      case 261:
        gravity = new Vec3d(0.0d, -0.05d, 0.0d);
        break;
      case 332:
      case 344:
      case 368:
        gravity = new Vec3d(0.0d, -0.03d, 0.0d);
        break;
      case 346:
        gravity = new Vec3d(0.0d, -0.03999999910593033D, 0.0d);
        break;
    }
    return gravity;
  }

  /** Get the air resistance the item will encounter */
  public static Vec3d getAirResistance(ItemStack itemStack) {
    Vec3d ar = new Vec3d(0, 0, 0);
    switch (Item.getIdFromItem(itemStack.getItem())) {
      case 332:
      case 344:
      case 368:
      case 261:
        ar = new Vec3d(0.99, 0.99, 0.99);
        break;
      case 346:
        ar = new Vec3d(0.92, 0.92, 0.92);
        break;
    }
    return ar;
  }

  /** Find the position the arrows trajectory will hit */
  public static Vec3d getImpactPos(ItemStack itemStack, Vec3d initPos, Vec3d hitPos, Angle angle) {
    double force = getForce(itemStack);

    Angle initAngle = new Angle(-angle.getPitch(), angle.getYaw() + 90.D, 0);

    double fixX = Math.cos(initAngle.getYaw(true) - Math.PI / 2.0) * 0.16;
    double fixY = PROJECTILE_SHOOTPOS_OFFSET;
    double fixZ = Math.sin(initAngle.getYaw(true) - Math.PI / 2.0) * 0.16;

    initPos = initPos.subtract(fixX, fixY, fixZ);

    Vec3d velocity = initAngle.getCartesianCoords().normalize().scale(force);

    Vec3d acceleration = getGravity(itemStack);
    Vec3d airResistance = getAirResistance(itemStack);

    double bestDistance = -1;
    Vec3d startPos = VectorUtils.copy(initPos);
    Vec3d endPos = VectorUtils.copy(startPos);
    for (int i = 1; i < SIMULATION_ITERATIONS; i++) {
      // add velocity
      startPos = startPos.add(velocity);
      // add air resistance
      velocity = VectorUtils.multiplyBy(velocity, airResistance);
      // add gravity (acceleration)
      velocity = velocity.add(acceleration);

      double x = startPos.x - hitPos.x;
      double z = startPos.z - hitPos.z;

      double distance = x * x + z * z;
      if (distance == -1 || distance < bestDistance) bestDistance = distance;
      else break;
      endPos = VectorUtils.copy(startPos);
    }

    RayTraceResult trace = MC.world.rayTraceBlocks(startPos, endPos);
    if (trace != null && trace.typeOfHit.equals(RayTraceResult.Type.BLOCK)) return trace.hitVec;
    else return initPos;
  }

  /** Find the angle required to hit the targets position */
  public static Angle getShootAngle(
      ItemStack itemStack, Vec3d startPos, Vec3d targetPos, double force) {
    Angle angle = new Angle();

    // start at targets position
    Vec3d initPos = startPos.subtract(0, PROJECTILE_SHOOTPOS_OFFSET, 0);
    // subtract starting position and PROJECTILE_SHOOTPOS_OFFSET
    initPos = initPos.subtract(targetPos);

    // yaw is simply just aiming at the target position, so we can reuse
    // math from getLookAtAngles(targetPos)
    angle.setYaw(Utils.getLookAtAngles(targetPos).getYaw(false));

    Vec3d acceleration = getGravity(itemStack);
    Vec3d airResistance = getAirResistance(itemStack);

    // to find the pitch we use this equation
    // https://en.wikipedia.org/wiki/Trajectory_of_a_projectile#Angle_.7F.27.22.60UNIQ--postMath-00000010-QINU.60.22.27.7F_required_to_hit_coordinate_.28x.2Cy.29

    // calculate air resistance in the acceleration
    force *= airResistance.y;

    // magnitude of a 2d vector
    double x = Math.sqrt(initPos.x * initPos.x + initPos.z * initPos.z);
    double g = acceleration.y;

    double root =
        Math.pow(force, 4) - g * (g * Math.pow(x, 2) + 2 * initPos.y * Math.pow(force, 2));

    // if the root is negative then we will get a non-real result
    if (root < 0) return null;

    // there are two possible solutions
    // +root and -root
    double A = (Math.pow(force, 2) + Math.sqrt(root)) / (g * x);
    double B = (Math.pow(force, 2) - Math.sqrt(root)) / (g * x);

    // use the lowest pitch
    angle.setPitch(Math.toDegrees(Math.atan(Math.max(A, B))));

    return angle.normalize();
  }

  public static Angle getShootAngle(ItemStack itemStack, Vec3d startPos, Vec3d targetPos) {
    return getShootAngle(itemStack, startPos, targetPos, getForce(itemStack));
  }

  /** Finds the pitch degree that yields the furthest distance */
  public static double getBestPitch(ItemStack itemStack, Vec3d hitPos) {
    EntityPlayer localPlayer = MC.player;
    Vec3d initPos = EntityUtils.getEyePos(localPlayer);

    Angle angle = new Angle();
    angle.setYaw(LocalPlayerUtils.getViewAngles().getYaw());

    double minAngle = localPlayer.rotationPitch;
    double maxAngle = minAngle - 45.D;

    double bestOffset = -1;
    double bestDistance = -1;
    for (int i = 0; i < BESTPOS_ITERATIONS; i++) {
      double offset = Utils.scale(0.5D, 0, 1, minAngle, maxAngle);
      angle.setPitch(offset);
      Vec3d pos = getImpactPos(itemStack, initPos, hitPos, angle);
      double distance = pos.distanceTo(hitPos);
      if (bestDistance == -1 || distance < bestDistance) {
        bestDistance = distance;
        bestOffset = offset;
      }
      if ((pos.y - hitPos.y) < 0) minAngle = offset;
      else maxAngle = offset;
    }
    return bestOffset;
  }

  /** Will simulate a shot made from an angle to see if it hits our target */
  public static boolean projectileTrajectoryHitsEntity(
      Entity target, Vec3d shootPos, Vec3d targetPos, ProjectileTraceResult result) {
    // gg fps
    EntityPlayer localPlayer = MC.player;
    Vec3d selfPos = localPlayer.getPositionVector();
    ItemStack heldItem = localPlayer.getHeldItemMainhand();
    // work backwards
    // this is actually just a coincidence that the
    // sequence and min value are same
    // im just abusing it so that I can get it to work with other projectile items
    double min = getMinForce(heldItem);
    double max = getMaxForce(heldItem);
    for (double force = max; force >= min; force -= min) {
      Angle angle = getShootAngle(heldItem, shootPos, targetPos, force);

      if (angle == null) continue;

      Angle initAngle = new Angle(-angle.getPitch(), angle.getYaw() + 90.D, 0.D);

      double fixX = Math.cos(initAngle.getYaw(true) - Math.PI / 2.0) * 0.16;
      double fixY = ProjectileUtils.PROJECTILE_SHOOTPOS_OFFSET;
      double fixZ = Math.sin(initAngle.getYaw(true) - Math.PI / 2.0) * 0.16;
      Vec3d initPos = new Vec3d(-fixX, localPlayer.getEyeHeight() - fixY, -fixZ);

      Vec3d acceleration = ProjectileUtils.getGravity(heldItem);
      Vec3d airResistance = ProjectileUtils.getAirResistance(heldItem);
      // convert polar coords to cartesian coords
      Vec3d velocity = initAngle.getCartesianCoords().normalize().scale(force);

      Vec3d startPos = initPos;
      Vec3d endPos = startPos;
      for (int i = 0; i < 100; i++) {
        // add velocity
        startPos = startPos.add(velocity);
        // add air resistance
        velocity = VectorUtils.multiplyBy(velocity, airResistance);
        // add gravity (acceleration)
        velocity = velocity.add(acceleration);

        Vec3d wrlStart = selfPos.add(startPos), wrlEnd = selfPos.add(endPos);

        RayTraceResult tr = getWorld().rayTraceBlocks(wrlStart, wrlEnd);
        // if we have hit a block
        if (tr != null
            && !getWorld()
                .getBlockState(tr.getBlockPos())
                .getBlock()
                .isPassable(getWorld(), tr.getBlockPos())) {
          break;
        }

        // if we have hit our target
        tr = target.getEntityBoundingBox().calculateIntercept(wrlStart, wrlEnd);
        if (tr != null) {
          if (result != null) {
            result.maxForce = force;
            result.shootAngle = angle;
          }
          return true;
        }
        endPos = startPos;
      }
    }
    return false;
  }

  public static class ProjectileTraceResult {
    public double maxForce = 0;
    public Angle shootAngle = new Angle();
  }
}
