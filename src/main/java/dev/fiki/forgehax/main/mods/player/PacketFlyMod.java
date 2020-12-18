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
      getLocalPlayer().noClip = false;
    }
  }

  @SubscribeListener
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    double[] dir = moveLooking(0);
    double xDir = dir[0];
    double zDir = dir[1];

    if ((getGameSettings().keyBindForward.isKeyDown()
        || getGameSettings().keyBindLeft.isKeyDown()
        || getGameSettings().keyBindRight.isKeyDown()
        || getGameSettings().keyBindBack.isKeyDown())
        && !getGameSettings().keyBindJump.isKeyDown()) {
      Vector3d vel = getLocalPlayer().getMotion();
      getLocalPlayer().setMotion(xDir * 0.26, vel.getY(), zDir * 0.26);
    }

    double posX = getLocalPlayer().getPosX() + getLocalPlayer().getMotion().getX();
    double posY =
        getLocalPlayer().getPosY()
            + (getGameSettings().keyBindJump.isKeyDown() ? (zoomies ? 0.0625 : 0.0624) : 0.00000001)
            - (getGameSettings().keyBindSneak.isKeyDown()
            ? (zoomies ? 0.0625 : 0.0624)
            : 0.00000002);
    double posZ = getLocalPlayer().getPosZ() + getLocalPlayer().getMotion().getZ();
    getNetworkManager()
        .sendPacket(
            new CPlayerPacket.PositionRotationPacket(
                getLocalPlayer().getPosX() + getLocalPlayer().getMotion().getX(),
                getLocalPlayer().getPosY()
                    + (getGameSettings().keyBindJump.isKeyDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000001)
                    - (getGameSettings().keyBindSneak.isKeyDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000002),
                getLocalPlayer().getPosZ() + getLocalPlayer().getMotion().getZ(),
                getLocalPlayer().rotationYaw,
                getLocalPlayer().rotationPitch,
                false));
    getNetworkManager()
        .sendPacket(
            new CPlayerPacket.PositionRotationPacket(
                getLocalPlayer().getPosX() + getLocalPlayer().getMotion().getX(),
                1337 + getLocalPlayer().getPosY(),
                getLocalPlayer().getPosZ() + getLocalPlayer().getMotion().getZ(),
                getLocalPlayer().rotationYaw,
                getLocalPlayer().rotationPitch,
                true));
    getNetworkManager().sendPacket(new CEntityActionPacket(getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    getLocalPlayer().setPosition(posX, posY, posZ);

    zoomies = !zoomies;

    getLocalPlayer().setMotion(0.D, 0.D, 0.D);
    getLocalPlayer().noClip = true;
  }

  public double[] moveLooking(int ignored) {
    return new double[]{getLocalPlayer().rotationYaw * 360 / 360 * 180 / 180, 0};
  }

  @SubscribeListener
  public void onOutgoingPacketSent(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      event.setCanceled(true);
    }
  }
}
