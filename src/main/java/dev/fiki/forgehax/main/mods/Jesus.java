package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.entity.EntityUtils;
import dev.fiki.forgehax.main.util.mod.AbstractMod;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
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
    if (!Common.getModManager().get(FreecamMod.class).map(AbstractMod::isEnabled).orElse(false)) {
      if (EntityUtils.isInWater(Common.getLocalPlayer()) && !Common.getLocalPlayer().isCrouching()) {
        double velY = 0.1;
        if (Common.getLocalPlayer().getRidingEntity() != null
            && !(Common.getLocalPlayer().getRidingEntity() instanceof BoatEntity)) {
          velY = 0.3;
        }
        Vector3d vel = Common.getLocalPlayer().getMotion();
        Common.getLocalPlayer().setMotion(vel.getX(), velY, vel.getZ());
      }
    }
  }

  // TODO: 1.15
//  @SubscribeEvent
//  public void onAddCollisionBox(AddCollisionBoxToListEvent event) {
//    if (Globals.getLocalPlayer() != null
//        && (Globals.getWorld().getBlockState(event.getPos()).getMaterial().isLiquid())
//        && (EntityUtils.isDrivenByPlayer(event.getEntity())
//        || EntityUtils.isLocalPlayer(event.getEntity()))
//        && !(event.getEntity() instanceof BoatEntity)
//        && !Globals.getLocalPlayer().isCrouching()
//        && Globals.getLocalPlayer().fallDistance < 3
//        && !EntityUtils.isInWater(Globals.getLocalPlayer())
//        && (EntityUtils.isAboveWater(Globals.getLocalPlayer(), false) || EntityUtils.isAboveWater(Globals.getMountedEntity(), false))
//        && isAboveBlock(Globals.getLocalPlayer(), event.getPos())) {
//      AxisAlignedBB axisalignedbb = WATER_WALK_AA.offset(event.getPos());
//      if (event.getEntityBox().intersects(axisalignedbb)) {
//        event.getCollidingBoxes().add(axisalignedbb);
//      }
//      // cancel event, which will stop it from calling the original code
//      event.setCanceled(true);
//    }
//  }
  
  @SubscribeEvent
  public void onPacketSending(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CPlayerPacket) {
      if (EntityUtils.isAboveWater(Common.getLocalPlayer(), true)
          && !EntityUtils.isInWater(Common.getLocalPlayer())
          && !isAboveLand(Common.getLocalPlayer())) {
        int ticks = Common.getLocalPlayer().ticksExisted % 2;
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
        if (VoxelShapes.fullCube().equals(Common.getWorld().getBlockState(pos).getCollisionShape(Common.getWorld(), pos))) {
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
