package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
@SuppressWarnings("MethodCallSideOnly")
public class FlyMod extends ToggleMod { // this mod is kind of gay and broken

  private boolean zoomies = true;

  public FlyMod() {
    super(Category.PLAYER, "Fly", false, "Enables flying");
  }

  @Override
  public void onDisabled() {
    if (Objects.nonNull(getLocalPlayer())) getLocalPlayer().noClip = false;
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    try {
      double[] dir = moveLooking(0);
      double xDir = dir[0];
      double zDir = dir[1];

      if ((MC.gameSettings.keyBindForward.isKeyDown()
              || MC.gameSettings.keyBindLeft.isKeyDown()
              || MC.gameSettings.keyBindRight.isKeyDown()
              || MC.gameSettings.keyBindBack.isKeyDown())
          && !MC.gameSettings.keyBindJump.isKeyDown()) {
        MC.player.setMotion(new Vec3d(xDir * 0.26, MC.player.getMotion().y, zDir * 0.26));
      }
      double posX = MC.player.posX + MC.player.getMotion().x;
      double posY =
          MC.player.posY
              + (MC.gameSettings.keyBindJump.isKeyDown() ? (zoomies ? 0.0625 : 0.0624) : 0.00000001)
              - (MC.gameSettings.keyBindSneak.isKeyDown()
                  ? (zoomies ? 0.0625 : 0.0624)
                  : 0.00000002);
      double posZ = MC.player.posZ + MC.player.getMotion().x;
      getNetworkManager()
          .sendPacket(
              new CPlayerPacket.PositionRotationPacket(
                  MC.player.posX + MC.player.getMotion().x,
                  MC.player.posY
                      + (MC.gameSettings.keyBindJump.isKeyDown()
                          ? (zoomies ? 0.0625 : 0.0624)
                          : 0.00000001)
                      - (MC.gameSettings.keyBindSneak.isKeyDown()
                          ? (zoomies ? 0.0625 : 0.0624)
                          : 0.00000002),
                  MC.player.posZ + MC.player.getMotion().z,
                  MC.player.rotationYaw,
                  MC.player.rotationPitch,
                  false));
      getNetworkManager()
          .sendPacket(
              new CPlayerPacket.PositionRotationPacket(
                  MC.player.posX + MC.player.getMotion().x,
                  1337 + MC.player.posY,
                  MC.player.posZ + MC.player.getMotion().z,
                  MC.player.rotationYaw,
                  MC.player.rotationPitch,
                  true));
      getNetworkManager().sendPacket(new CEntityActionPacket(MC.player, CEntityActionPacket.Action.START_FALL_FLYING));
      MC.player.setPosition(posX, posY, posZ);

      zoomies = !zoomies;

      MC.player.setMotion(Vec3d.ZERO);

      MC.player.noClip = true;
    } catch (Exception e) {
      Helper.printStackTrace(e);
    }
  }

  public double[] moveLooking(int ignored) {
    return new double[] {MC.player.rotationYaw * 360 / 360 * 180 / 180, 0};
  }

  @SubscribeEvent
  public void onOutgoingPacketSent(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = event.getPacket();
      try {
        ObfuscationReflectionHelper.setPrivateValue(
            SPlayerPositionLookPacket.class,
            packet,
            MC.player.rotationYaw,
            //"yaw",
            "field_148936_d");
        ObfuscationReflectionHelper.setPrivateValue(
            SPlayerPositionLookPacket.class,
            packet,
            MC.player.rotationPitch,
            //"pitch",
            "field_148937_e");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
