package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Objects;

import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import static com.matt.forgehax.Globals.*;

@RegisterMod
@SuppressWarnings("MethodCallSideOnly")
public class FlyMod extends ToggleMod {
  
  private boolean zoomies = true;
  
  public FlyMod() {
    super(Category.PLAYER, "Fly", false, "Enables flying");
  }
  
  @Override
  public void onDisabled() {
    if (Objects.nonNull(getLocalPlayer())) {
      getLocalPlayer().noClip = false;
    }
  }
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    double[] dir = moveLooking(0);
    double xDir = dir[0];
    double zDir = dir[1];

    if ((getGameSettings().keyBindForward.isKeyDown()
        || getGameSettings().keyBindLeft.isKeyDown()
        || getGameSettings().keyBindRight.isKeyDown()
        || getGameSettings().keyBindBack.isKeyDown())
        && !getGameSettings().keyBindJump.isKeyDown()) {
      Vec3d vel = getLocalPlayer().getMotion();
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
  
  @SubscribeEvent
  public void onOutgoingPacketSent(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      event.setCanceled(true);
    }
  }
}
