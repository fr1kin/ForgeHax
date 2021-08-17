package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Objects;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "PacketFly",
    description = "Enables flying",
    category = Category.PLAYER
)
@SuppressWarnings("MethodCallSideOnly")
public class PacketFlyMod extends ToggleMod {

  private boolean zoomies = true;

  @Override
  public void onDisabled() {
    if (Objects.nonNull(getLocalPlayer())) {
      getLocalPlayer().noPhysics = false;
    }
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    double[] dir = moveLooking(0);
    double xDir = dir[0];
    double zDir = dir[1];

    if ((getGameSettings().keyUp.isDown()
        || getGameSettings().keyLeft.isDown()
        || getGameSettings().keyRight.isDown()
        || getGameSettings().keyDown.isDown())
        && !getGameSettings().keyJump.isDown()) {
      Vector3d vel = getLocalPlayer().getDeltaMovement();
      getLocalPlayer().setDeltaMovement(xDir * 0.26, vel.y(), zDir * 0.26);
    }

    double posX = getLocalPlayer().getX() + getLocalPlayer().getDeltaMovement().x();
    double posY =
        getLocalPlayer().getY()
            + (getGameSettings().keyJump.isDown() ? (zoomies ? 0.0625 : 0.0624) : 0.00000001)
            - (getGameSettings().keyShift.isDown()
            ? (zoomies ? 0.0625 : 0.0624)
            : 0.00000002);
    double posZ = getLocalPlayer().getZ() + getLocalPlayer().getDeltaMovement().z();
    getNetworkManager()
        .send(
            new CPlayerPacket.PositionRotationPacket(
                getLocalPlayer().getX() + getLocalPlayer().getDeltaMovement().x(),
                getLocalPlayer().getY()
                    + (getGameSettings().keyJump.isDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000001)
                    - (getGameSettings().keyShift.isDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000002),
                getLocalPlayer().getZ() + getLocalPlayer().getDeltaMovement().z(),
                getLocalPlayer().yRot,
                getLocalPlayer().xRot,
                false));
    getNetworkManager()
        .send(
            new CPlayerPacket.PositionRotationPacket(
                getLocalPlayer().getX() + getLocalPlayer().getDeltaMovement().x(),
                1337 + getLocalPlayer().getY(),
                getLocalPlayer().getZ() + getLocalPlayer().getDeltaMovement().z(),
                getLocalPlayer().yRot,
                getLocalPlayer().xRot,
                true));
    getNetworkManager().send(new CEntityActionPacket(getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    getLocalPlayer().moveTo(posX, posY, posZ);

    zoomies = !zoomies;

    getLocalPlayer().setDeltaMovement(0.D, 0.D, 0.D);
    getLocalPlayer().noPhysics = true;
  }

  public double[] moveLooking(int ignored) {
    return new double[]{getLocalPlayer().yRot * 360 / 360 * 180 / 180, 0};
  }

  @SubscribeListener
  public void onOutgoingPacketSent(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      event.setCanceled(true);
    }
  }
}
