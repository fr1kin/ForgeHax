package dev.fiki.forgehax.api.projectile;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.math.AngleUtil;
import lombok.experimental.ExtensionMethod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.getWorld;

/**
 * Created on 6/21/2017 by fr1kin
 */
@ExtensionMethod({VectorEx.class})
public enum Projectile implements IProjectile {
  NULL() {
    @Override
    public Item getItem() {
      return null;
    }
    
    @Override
    public double getForce(int charge) {
      return 0;
    }
    
    @Override
    public double getMaxForce() {
      return 0;
    }
    
    @Override
    public double getMinForce() {
      return 0;
    }
    
    @Override
    public double getGravity() {
      return 0;
    }
    
    @Override
    public double getDrag() {
      return 0;
    }
    
    @Override
    public double getWaterDrag() {
      return 0;
    }
    
    @Override
    public double getProjectileSize() {
      return 0;
    }
  },
  BOW() {
    @Override
    public Item getItem() {
      return Items.BOW;
    }
    
    @Override
    public double getForce(int charge) {
      double force = (double) charge / 20.0F;
      //   force = (force * force + force * 2.0F) / 3.0F;
      if (force > 1.0F) {
        force = 1.0F;
      }
      force *= 2d * 1.5d;
      return force;
    }
    
    @Override
    public double getMaxForce() {
      return 3.D;
    }
    
    @Override
    public double getMinForce() {
      return 0.15D;
    }
    
    @Override
    public double getGravity() {
      return 0.05D;
    }
    
    @Override
    public double getWaterDrag() {
      return 0.6D;
    }
    
    @Override
    public double getProjectileSize() {
      return 0.5D;
    }
  },
  SNOWBALL() {
    @Override
    public Item getItem() {
      return Items.SNOWBALL;
    }
  },
  EGG() {
    @Override
    public Item getItem() {
      return Items.EGG;
    }
  },
  FISHING_ROD() {
    @Override
    public Item getItem() {
      return Items.FISHING_ROD;
    }
    
    @Override
    public double getGravity() {
      return 0.03999999910593033D;
    }
    
    @Override
    public double getDrag() {
      return 0.92D;
    }
  },
  ENDER_PEARL() {
    @Override
    public Item getItem() {
      return Items.ENDER_PEARL;
    }
  };
  
  private static final int MAX_ITERATIONS =
      1000; // fail safe to prevent infinite loops. MUST be greater than 1
  private static final double SHOOT_POS_OFFSET = 0.10000000149011612D;
  
  public boolean isNull() {
    return getItem() == null;
  }
  
  @Nullable
  public SimulationResult getSimulatedTrajectory(
      Vector3d shootPos, Angle angle, double force, int factor) throws IllegalArgumentException {
    if (isNull()) {
      return null;
    }
    
    Entity hitEntity = null;
    
    double[] forward = angle.getForwardVector();
    Vector3d v = new Vector3d(forward[0], forward[1], forward[2]).normalize().scale(force);
    
    double velocityX = v.x;
    double velocityY = v.y;
    double velocityZ = v.z;
    
    double distanceTraveledSq = 0.D;
    
    EntityRayTraceResult trace;
    
    List<Vector3d> points = Lists.newArrayList();
    points.add(shootPos); // add the initial position
    
    Vector3d next = new Vector3d(shootPos.x, shootPos.y, shootPos.z);
    Vector3d previous = next;
    
    for (int index = points.size(), n = 0; index < MAX_ITERATIONS; index++) {
      next = next.add(velocityX, velocityY, velocityZ);
      
      AxisAlignedBB bb = getBoundBox(next);
      trace = rayTraceCheckEntityCollisions(previous, next, bb, velocityX, velocityY, velocityZ);
      
      if (trace != null) {
        hitEntity = trace.getEntity();
        distanceTraveledSq += previous.distanceToSqr(trace.getLocation());
        // add final vector even if index % factor != 0
        points.add(trace.getLocation());
        break;
      }
      // only add every nth entry
      if (n == factor) {
        points.add(next);
        n = 0;
      } else {
        n++;
      }
      
      distanceTraveledSq += previous.distanceToSqr(next);
      
      // in the void, stop
      if (next.y <= 0) {
        break;
      }
      
//      double d = getWorld().isMaterialInBB(bb, Material.WATER) ? getWaterDrag() : getDrag();
      double d = getDrag(); // TODO: 1.16 water drag

      velocityX = (velocityX * d);
      velocityY = (velocityY * d) - getGravity();
      velocityZ = (velocityZ * d);
      
      previous = next;
    }
    return new SimulationResult(points, distanceTraveledSq, hitEntity);
  }
  
  @Nullable
  public SimulationResult getSimulatedTrajectoryFromEntity(
      Entity shooter, Angle angle, double force, int factor) {
    angle = getAngleFacing(angle);
    return getSimulatedTrajectory(getShootPosFacing(shooter, angle), angle, force, factor);
  }
  
  @Nullable
  public Angle getEstimatedImpactAngleInRadians(Vector3d shooterPos, Vector3d targetPos, double force) {
    if (isNull()) {
      return null;
    }
    Vector3d start = shooterPos.subtract(targetPos);
    
    double pitch;
    double yaw = targetPos.subtract(shooterPos).getAngleFacingInRadians().getYaw();
    
    // to find the pitch we use this equation
    // https://en.wikipedia.org/wiki/Trajectory_of_a_projectile#Angle_.7F.27.22.60UNIQ--postMath-00000010-QINU.60.22.27.7F_required_to_hit_coordinate_.28x.2Cy.29
    
    // calculate air resistance in the acceleration
    force *= getDrag();
    
    // magnitude of a 2d vector
    double x = Math.sqrt(start.x * start.x + start.z * start.z);
    double g = getGravity();
    
    double root = Math.pow(force, 4) - g * (g * Math.pow(x, 2) + 2 * start.y * Math.pow(force, 2));
    
    // if the root is negative then we will get a non-real result
    if (root < 0) {
      return null;
    }
    
    // there are two possible solutions
    // +root and -root
    double A = (Math.pow(force, 2) + Math.sqrt(root)) / (g * x);
    double B = (Math.pow(force, 2) - Math.sqrt(root)) / (g * x);
    
    // use the lowest pitch
    pitch = Math.atan(Math.max(A, B));
    
    return Angle.radians((float) pitch, (float) yaw).normalize();
  }
  
  @Nullable
  public Angle getEstimatedImpactAngleInRadiansFromEntity(
      Entity entity, Vector3d targetPos, double force) {
    return getEstimatedImpactAngleInRadians(getEntityShootPos(entity), targetPos, force);
  }
  
  public boolean canHitEntity(Vector3d shooterPos, Entity targetEntity) {
    if (isNull()) {
      return false;
    }
    
    Vector3d targetPos = EntityEx.getOBBCenter(targetEntity);
    
    double min = getMinForce();
    double max = getMaxForce();
    // work backwards
    // this is actually just a coincidence that the
    // sequence and min value are same
    // im just abusing it so that I can get it to work with other projectile items
    for (double force = max; force >= min; force -= min) {
      Angle shootAngle = getEstimatedImpactAngleInRadians(shooterPos, targetPos, force);
      if (shootAngle == null) {
        continue;
      }
      
      SimulationResult result = getSimulatedTrajectory(shooterPos, shootAngle, force, -1);
      if (result == null) {
        return false; // this shouldn't happen, but I put it here to stop intelliJ from complaining
      }
      
      // the trace has intercepted with our target
      if (Objects.equals(targetEntity, result.getHitEntity())) {
        return true;
      }
    }
    return false;
  }
  
  // ####################################################################################################
  
  private AxisAlignedBB getBoundBox(Vector3d pos) {
    double mp = getProjectileSize() / 2.D;
    return new AxisAlignedBB(
        pos.x - mp, pos.y - mp, pos.z - mp, pos.x + mp, pos.y + mp, pos.z + mp);
  }
  
  // ####################################################################################################
  
  private static EntityRayTraceResult rayTraceCheckEntityCollisions(
      Vector3d start, Vector3d end, AxisAlignedBB bb, double motionX, double motionY, double motionZ) {
//    RayTraceResult trace = getWorld().rayTraceBlocks(start, end, false, true, false);
//
//    if (trace != null) {
//      end = trace.hitVec;
//    }
//
//    // now check entity collisions
//    List<Entity> entities =
//        getWorld()
//            .getEntitiesWithinAABBExcludingEntity(
//                getLocalPlayer(), bb.expand(motionX, motionY, motionZ).grow(1.D));
//
//    double best = 0.D;
//    Vector3d hitPos = Vector3d.ZERO;
//    Entity hitEntity = null;
//
//    for (Entity entity : entities) {
//      if (entity.canBeCollidedWith()) {
//        float size = entity.getCollisionBorderSize();
//        AxisAlignedBB bbe = entity.getEntityBoundingBox().grow(size);
//        RayTraceResult tr = bbe.calculateIntercept(start, end);
//        if (tr != null) {
//          double distance = start.squareDistanceTo(tr.hitVec);
//          if (distance < best || hitEntity == null) {
//            best = distance;
//            hitPos = tr.hitVec;
//            hitEntity = entity;
//          }
//        }
//      }
//    }
//
//    if (hitEntity != null) {
//      trace = new RayTraceResult(hitEntity, hitPos);
//    }
//
//    return trace;
    return ProjectileHelper.getEntityHitResult(getWorld(), getLocalPlayer(), start, end,
        bb.inflate(motionX, motionY, motionZ).inflate(1.D), ent -> true);
  }
  
  private static Vector3d getEntityShootPos(Entity entity) {
    return EntityEx.getEyePos(entity).subtract(0.D, SHOOT_POS_OFFSET, 0.D);
  }
  
  private static Vector3d getShootPosFacing(Entity entity, Angle angleFacing) {
    return getEntityShootPos(entity)
        .subtract(
            Math.cos(angleFacing.inRadians().getYaw() - AngleUtil.HALF_PI) * 0.16D,
            0.D,
            Math.sin(angleFacing.inRadians().getYaw() - AngleUtil.HALF_PI) * 0.16D);
  }
  
  private static Angle getAngleFacing(Angle angle) {
    return Angle.radians(
        -angle.inRadians().getPitch(), (float) (angle.inRadians().getYaw() + (Math.PI / 2.D)));
  }
  
  public static Projectile getProjectileByItem(Item item) {
    if (item != null) {
      for (Projectile p : values()) {
        if (p.getItem() != null && p.getItem().equals(item)) {
          return p;
        }
      }
    }
    return NULL;
  }
  
  public static Projectile getProjectileByItemStack(ItemStack item) {
    return item == null ? NULL : getProjectileByItem(item.getItem());
  }
}
