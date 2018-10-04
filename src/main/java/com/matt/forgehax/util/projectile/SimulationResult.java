package com.matt.forgehax.util.projectile;

import java.util.List;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/** Created on 6/22/2017 by fr1kin */
public class SimulationResult {
  private final List<Vec3d> points;
  private final double distanceTraveledSq;
  private final Entity hitEntity;

  public SimulationResult(List<Vec3d> points, double distanceTraveledSq, Entity hitEntity) {
    this.points = points;
    this.distanceTraveledSq = distanceTraveledSq;
    this.hitEntity = hitEntity;
  }

  public Vec3d getShootPos() {
    try {
      return points.get(0);
    } catch (Throwable t) {
      return null;
    }
  }

  public Vec3d getHitPos() {
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
    Vec3d start = getShootPos();
    Vec3d hit = getHitPos();
    if (start != null && hit != null) {
      return start.squareDistanceTo(hit);
    } else return 0.D;
  }

  public List<Vec3d> getPathTraveled() {
    return points;
  }
}
