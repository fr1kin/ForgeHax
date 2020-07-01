package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class Criticals extends ToggleMod {

  private final Setting<Boolean> legit =
  getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("legit")
      .description("Make your player jump rather than sending packet")
      .defaultTo(false)
      .build();
      
  
  public Criticals() {
    super(Category.COMBAT, "Criticals", false, "Make all your hits criticals");
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onOutgoingPacket(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketUseEntity) {
      final CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
      if (packet.getAction() == CPacketUseEntity.Action.ATTACK) {
        if (getLocalPlayer().onGround &&
            !MC.gameSettings.keyBindJump.isKeyDown() &&
            packet.getEntityFromWorld(getWorld()) instanceof EntityLivingBase) {
          if (legit.get()) {
            MC.player.jump();
          } else {
            getNetworkManager().sendPacket(
                new CPacketPlayer.Position(getLocalPlayer().posX, getLocalPlayer().posY + 0.1F, getLocalPlayer().posZ, false));
            getNetworkManager().sendPacket(
                new CPacketPlayer.Position(getLocalPlayer().posX, getLocalPlayer().posY, getLocalPlayer().posZ, false));
          }
        }
      }
    }
  }
}