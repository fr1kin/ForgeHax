package com.matt.forgehax.mods;

import static com.matt.forgehax.Globals.*;
import static com.matt.forgehax.util.entity.EntityUtils.isAboveWater;
import static com.matt.forgehax.util.entity.EntityUtils.isInWater;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.events.AddCollisionBoxToListEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Babbaj on 8/29/2017.
 */
@RegisterMod
public class Jesus extends ToggleMod {
  
  private static final AxisAlignedBB WATER_WALK_AA =
      new AxisAlignedBB(0.D, 0.D, 0.D, 1.D, 0.99D, 1.D);
  
  public Jesus() {
    super(Category.PLAYER, "Jesus", false, "Walk on water");
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!getModManager().get(FreecamMod.class).map(BaseMod::isEnabled).orElse(false)) {
      if (isInWater(getLocalPlayer()) && !getLocalPlayer().isCrouching()) {
        double velY = 0.1;
        if (getLocalPlayer().getRidingEntity() != null
            && !(getLocalPlayer().getRidingEntity() instanceof BoatEntity)) {
          velY = 0.3;
        }
        Vec3d vel = getLocalPlayer().getMotion();
        getLocalPlayer().setMotion(vel.getX(), velY, vel.getZ());
      }
    }
  }
  
  @SubscribeEvent
  public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
    if (getLocalPlayer() != null
        && (getWorld().getBlockState(event.getPos()).getMaterial().isLiquid())
        && (EntityUtils.isDrivenByPlayer(event.getEntity())
        || EntityUtils.isLocalPlayer(event.getEntity()))
        && !(event.getEntity() instanceof BoatEntity)
        && !getLocalPlayer().isCrouching()
        && getLocalPlayer().fallDistance < 3
        && !isInWater(getLocalPlayer())
        && (isAboveWater(getLocalPlayer(), false) || isAboveWater(getMountedEntity(), false))
        && isAboveBlock(getLocalPlayer(), event.getPos())) {
      AxisAlignedBB axisalignedbb = WATER_WALK_AA.offset(event.getPos());
      if (event.getEntityBox().intersects(axisalignedbb)) {
        event.getCollidingBoxes().add(axisalignedbb);
      }
      // cancel event, which will stop it from calling the original code
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPlayerPacket) {
      if (isAboveWater(getLocalPlayer(), true)
          && !isInWater(getLocalPlayer())
          && !isAboveLand(getLocalPlayer())) {
        int ticks = getLocalPlayer().ticksExisted % 2;
        double y = FastReflection.Fields.CPacketPlayer_y.get(event.getPacket());
        if (ticks == 0) {
          FastReflection.Fields.CPacketPlayer_y.set(event.getPacket(), y + 0.02D);
        }
      }
    }
  }
  
  @SuppressWarnings("deprecation")
  private static boolean isAboveLand(Entity entity) {
    if (entity == null) {
      return false;
    }
    
    double y = entity.getPosY() - 0.01;
    
    for (int x = MathHelper.floor(entity.getPosX()); x < MathHelper.ceil(entity.getPosX()); x++) {
      for (int z = MathHelper.floor(entity.getPosZ()); z < MathHelper.ceil(entity.getPosZ()); z++) {
        BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
        if (VoxelShapes.fullCube().equals(getWorld().getBlockState(pos).getCollisionShape(getWorld(), pos))) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  private static boolean isAboveBlock(Entity entity, BlockPos pos) {
    return entity.getPosY() >= pos.getY();
  }
}
