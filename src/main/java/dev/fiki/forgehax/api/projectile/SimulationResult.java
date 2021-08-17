package dev.fiki.forgehax.api.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Objects;

/**
 * Created on 6/22/2017 by fr1kin
 */
public class SimulationResult {
  
  private final List<Vector3d> points;
  private final double distanceTraveledSq;
  private final Entity hitEntity;
  
  public SimulationResult(List<Vector3d> points, double distanceTraveledSq, Entity hitEntity) {
    this.points = points;
    this.distanceTraveledSq = distanceTraveledSq;
    this.hitEntity = hitEntity;
  }
  
  public Vector3d getShootPos() {
    try {
      return points.get(0);
    } catch (Throwable t) {
      return null;
    }
  }
  
  public Vector3d getHitPos() {
    try {
      return points.get(points.size() - 1);
    } catch (Throwable t) {
      return null;
    }
  }
  
  public Entity getHitEntity() {
    return hitEntity;
  }
  
  public boolean hasTraveled() {
    return !Objects.equals(getShootPos(), getHitPos());
  }
  
  public double getDistanceTraveledSq() {
    return distanceTraveledSq;
  }
  
  public double getDistanceApartSq() {
    Vector3d start = getShootPos();
    Vector3d hit = getHitPos();
    if (start != null && hit != null) {
      return start.distanceToSqr(hit);
    } else {
      return 0.D;
    }
  }
  
  public List<Vector3d> getPathTraveled() {
    return points;
  }
}
