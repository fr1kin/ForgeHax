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
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
    try {
      double[] dir = moveLooking(0);
      double xDir = dir[0];
      double zDir = dir[1];
      
      if ((MC.gameSettings.keyBindForward.isKeyDown()
          || MC.gameSettings.keyBindLeft.isKeyDown()
          || MC.gameSettings.keyBindRight.isKeyDown()
          || MC.gameSettings.keyBindBack.isKeyDown())
          && !MC.gameSettings.keyBindJump.isKeyDown()) {
        MC.player.motionX = xDir * 0.26;
        MC.player.motionZ = zDir * 0.26;
      }
      double posX = MC.player.posX + MC.player.motionX;
      double posY =
          MC.player.posY
              + (MC.gameSettings.keyBindJump.isKeyDown() ? (zoomies ? 0.0625 : 0.0624) : 0.00000001)
              - (MC.gameSettings.keyBindSneak.isKeyDown()
              ? (zoomies ? 0.0625 : 0.0624)
              : 0.00000002);
      double posZ = MC.player.posZ + MC.player.motionX;
      getNetworkManager()
          .sendPacket(
              new CPacketPlayer.PositionRotation(
                  MC.player.posX + MC.player.motionX,
                  MC.player.posY
                      + (MC.gameSettings.keyBindJump.isKeyDown()
                      ? (zoomies ? 0.0625 : 0.0624)
                      : 0.00000001)
                      - (MC.gameSettings.keyBindSneak.isKeyDown()
                      ? (zoomies ? 0.0625 : 0.0624)
                      : 0.00000002),
                  MC.player.posZ + MC.player.motionZ,
                  MC.player.rotationYaw,
                  MC.player.rotationPitch,
                  false));
      getNetworkManager()
          .sendPacket(
              new CPacketPlayer.PositionRotation(
                  MC.player.posX + MC.player.motionX,
                  1337 + MC.player.posY,
                  MC.player.posZ + MC.player.motionZ,
                  MC.player.rotationYaw,
                  MC.player.rotationPitch,
                  true));
      getNetworkManager().sendPacket(new CPacketEntityAction(MC.player, Action.START_FALL_FLYING));
      MC.player.setPosition(posX, posY, posZ);
      
      zoomies = !zoomies;
      
      MC.player.motionX = 0;
      MC.player.motionY = 0;
      MC.player.motionZ = 0;
      
      MC.player.noClip = true;
    } catch (Exception e) {
      Helper.printStackTrace(e);
    }
  }
  
  public double[] moveLooking(int ignored) {
    return new double[]{MC.player.rotationYaw * 360 / 360 * 180 / 180, 0};
  }
  
  @SubscribeEvent
  public void onOutgoingPacketSent(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketPlayerPosLook) {
      SPacketPlayerPosLook packet = event.getPacket();
      try {
        ObfuscationReflectionHelper.setPrivateValue(
            SPacketPlayerPosLook.class,
            packet,
            MC.player.rotationYaw,
            "yaw",
            "field_148936_d",
            "d");
        ObfuscationReflectionHelper.setPrivateValue(
            SPacketPlayerPosLook.class,
            packet,
            MC.player.rotationPitch,
            "pitch",
            "field_148937_e",
            "e");
      } catch (Exception e) {
      }
    }
  }
}
