package dev.fiki.forgehax.api.projectile;

import net.minecraft.item.Item;

/**
 * Created on 6/21/2017 by fr1kin
 */
public interface IProjectile {
  
  Item getItem();
  
  default double getForce(int charge) {
    return 1.5D;
  }
  
  default double getMaxForce() {
    return 1.5D;
  }
  
  default double getMinForce() {
    return 1.5D;
  }
  
  default double getGravity() {
    return 0.03D;
  }
  
  default double getDrag() {
    return 0.99D;
  }
  
  default double getWaterDrag() {
    return 0.8D;
  }
  
  default double getProjectileSize() {
    return 0.25D;
  }
}
