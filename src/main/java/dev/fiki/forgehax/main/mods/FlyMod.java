package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import java.util.Objects;

import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
@SuppressWarnings("MethodCallSideOnly")
public class FlyMod extends ToggleMod {
  
  private boolean zoomies = true;
  
  public FlyMod() {
    super(Category.PLAYER, "Fly", false, "Enables flying");
  }
  
  @Override
  public void onDisabled() {
    if (Objects.nonNull(Globals.getLocalPlayer())) {
      Globals.getLocalPlayer().noClip = false;
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    double[] dir = moveLooking(0);
    double xDir = dir[0];
    double zDir = dir[1];

    if ((Globals.getGameSettings().keyBindForward.isKeyDown()
        || Globals.getGameSettings().keyBindLeft.isKeyDown()
        || Globals.getGameSettings().keyBindRight.isKeyDown()
        || Globals.getGameSettings().keyBindBack.isKeyDown())
        && !Globals.getGameSettings().keyBindJump.isKeyDown()) {
      Vec3d vel = Globals.getLocalPlayer().getMotion();
      Globals.getLocalPlayer().setMotion(xDir * 0.26, vel.getY(), zDir * 0.26);
    }

    double posX = Globals.getLocalPlayer().getPosX() + Globals.getLocalPlayer().getMotion().getX();
    double posY =
        Globals.getLocalPlayer().getPosY()
            + (Globals.getGameSettings().keyBindJump.isKeyDown() ? (zoomies ? 0.0625 : 0.0624) : 0.00000001)
            - (Globals.getGameSettings().keyBindSneak.isKeyDown()
            ? (zoomies ? 0.0625 : 0.0624)
            : 0.00000002);
    double posZ = Globals.getLocalPlayer().getPosZ() + Globals.getLocalPlayer().getMotion().getZ();
    Globals.getNetworkManager()
        .sendPacket(
            new CPlayerPacket.PositionRotationPacket(
                Globals.getLocalPlayer().getPosX() + Globals.getLocalPlayer().getMotion().getX(),
                Globals.getLocalPlayer().getPosY()
                    + (Globals.getGameSettings().keyBindJump.isKeyDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000001)
                    - (Globals.getGameSettings().keyBindSneak.isKeyDown()
                    ? (zoomies ? 0.0625 : 0.0624)
                    : 0.00000002),
                Globals.getLocalPlayer().getPosZ() + Globals.getLocalPlayer().getMotion().getZ(),
                Globals.getLocalPlayer().rotationYaw,
                Globals.getLocalPlayer().rotationPitch,
                false));
    Globals.getNetworkManager()
        .sendPacket(
            new CPlayerPacket.PositionRotationPacket(
                Globals.getLocalPlayer().getPosX() + Globals.getLocalPlayer().getMotion().getX(),
                1337 + Globals.getLocalPlayer().getPosY(),
                Globals.getLocalPlayer().getPosZ() + Globals.getLocalPlayer().getMotion().getZ(),
                Globals.getLocalPlayer().rotationYaw,
                Globals.getLocalPlayer().rotationPitch,
                true));
    Globals.getNetworkManager().sendPacket(new CEntityActionPacket(Globals.getLocalPlayer(), CEntityActionPacket.Action.START_FALL_FLYING));
    Globals.getLocalPlayer().setPosition(posX, posY, posZ);

    zoomies = !zoomies;

    Globals.getLocalPlayer().setMotion(0.D, 0.D, 0.D);
    Globals.getLocalPlayer().noClip = true;
  }
  
  public double[] moveLooking(int ignored) {
    return new double[]{Globals.getLocalPlayer().rotationYaw * 360 / 360 * 180 / 180, 0};
  }
  
  @SubscribeEvent
  public void onOutgoingPacketSent(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      event.setCanceled(true);
    }
  }
}
