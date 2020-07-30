package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

@RegisterMod
@SuppressWarnings("MethodCallSideOnly")
public class PacketFlyMod extends ToggleMod {

  private boolean zoomies = true;

  public PacketFlyMod() {
    super(Category.PLAYER, "PacketFly", false, "Enables flying");
  }

  @Override
  public void onDisabled() {
    if (Objects.nonNull(Common.getLocalPlayer())) {
      Common.getLocalPlayer().noClip = false;
    }
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    double[] dir = moveLooking(0);
    double xDir = dir[0];
    double zDir = dir[1];

    if ((Common.getGameSettings().keyBindForward.isKeyDown()
        || Common.getGameSettings().keyBindLeft.isKeyDown()
        || Common.getGameSettings().keyBindRight.isKeyDown()
        || Common.getGameSettings().keyBindBack.isKeyDown())
        && !Common.getGameSettings().keyBindJump.isKeyDown()) {
      Vector3d vel = Common.getLocalPlayer().getMotion();
      Common.getLocalPlayer().setMotion(xDir * 0.26, vel.getY(), zDir * 0.26);
    }

    double posX = Common.getLocalPlayer().getPosX() + Common.getLocalPlayer().getMotion().getX();
    double posY =
        Common.getLocalPlayer().getPosY()
            + (Common.getGameSettings().keyBindJump.isKeyDown() ? (zoomies ? 0.0625 : 0.0624) : 0.00000001)
            - (Common.getGameSettings().keyBindSneak.isKeyDown()
            ? (zoomies ? 0.0625 : 0.0624)
            : 0.00000002);
    double posZ = Common.getLocalPlayer().getPosZ() + Common.getLocalPlayer().getMotion().getZ();
    Common.getNetworkManager()
        .sendPacket(
            new CPlayerPacket.PositionRotationPacket(
                Common.getLocalPlayer().getPosX() + Common.getLocalPlayer().getMotion().getX(),
                Common.getLocalPlayer().getPosY()
                    + (Common.getGameSettings().keyBindJump.isKeyDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000001)
                    - (Common.getGameSettings().keyBindSneak.isKeyDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000002),
                Common.getLocalPlayer().getPosZ() + Common.getLocalPlayer().getMotion().getZ(),
                Common.getLocalPlayer().rotationYaw,
                Common.getLocalPlayer().rotationPitch,
                false));
    Common.getNetworkManager()
        .sendPacket(
            new CPlayerPacket.PositionRotationPacket(
                Common.getLocalPlayer().getPosX() + Common.getLocalPlayer().getMotion().getX(),
                1337 + Common.getLocalPlayer().getPosY(),
                Common.getLocalPlayer().getPosZ() + Common.getLocalPlayer().getMotion().getZ(),
                Common.getLocalPlayer().rotationYaw,
                Common.getLocalPlayer().rotationPitch,
                true));
    Common.getNetworkManager().sendPacket(new CEntityActionPacket(Common.getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    Common.getLocalPlayer().setPosition(posX, posY, posZ);

    zoomies = !zoomies;

    Common.getLocalPlayer().setMotion(0.D, 0.D, 0.D);
    Common.getLocalPlayer().noClip = true;
  }

  public double[] moveLooking(int ignored) {
    return new double[]{Common.getLocalPlayer().rotationYaw * 360 / 360 * 180 / 180, 0};
  }

  @SubscribeEvent
  public void onOutgoingPacketSent(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      event.setCanceled(true);
    }
  }
}
