package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.extension.EntityEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "Jesus",
    description = "Walk on water",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({EntityEx.class})
public class Jesus extends ToggleMod {
  private static final AxisAlignedBB WATER_WALK_AA =
      new AxisAlignedBB(0.D, 0.D, 0.D, 1.D, 0.99D, 1.D);

  private final FreecamMod freecam;
  private final ReflectionTools common;

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (!freecam.isEnabled()) {
      if (getLocalPlayer().isInWaterMotionState() && !getLocalPlayer().isCrouching()) {
        double velY = 0.1;
        if (getLocalPlayer().getVehicle() != null
            && !(getLocalPlayer().getVehicle() instanceof BoatEntity)) {
          velY = 0.3;
        }
        Vector3d vel = getLocalPlayer().getDeltaMovement();
        getLocalPlayer().setDeltaMovement(vel.x(), velY, vel.z());
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

  @SubscribeListener
  public void onPacketSending(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CPlayerPacket) {
      if (getLocalPlayer().isAboveWater(true)
          && !getLocalPlayer().isInWaterMotionState()
          && !getLocalPlayer().isAboveLand()) {
        int ticks = getLocalPlayer().tickCount % 2;
        double y = common.CPacketPlayer_y.get(event.getPacket());
        if (ticks == 0) {
          common.CPacketPlayer_y.set(event.getPacket(), y + 0.02D);
        }
      }
    }
  }
}
