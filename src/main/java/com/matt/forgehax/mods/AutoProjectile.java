package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.math.ProjectileUtils;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoProjectile extends ToggleMod {
  public AutoProjectile() {
    super(Category.PLAYER, "AutoProjectile", false, "Automatically sets pitch to best trajectory");
  }

  @SubscribeEvent
  public void onSendingPacket(PacketEvent.Outgoing.Pre event) {
    EntityPlayer localPlayer = MC.player;
    if (!LocalPlayerUtils.isProjectileTargetAcquired() && !LocalPlayerUtils.isFakeAnglesActive()) {
      if (event.getPacket() instanceof CPacketPlayerDigging
          && ((CPacketPlayerDigging) event.getPacket())
              .getAction()
              .equals(CPacketPlayerDigging.Action.RELEASE_USE_ITEM)
          && !PacketHelper.isIgnored(event.getPacket())) {
        ItemStack heldItem = localPlayer.getHeldItemMainhand();
        RayTraceResult trace = localPlayer.rayTrace(9999.D, 0.f);
        if (heldItem != null
            && getNetworkManager() != null
            && trace != null
            && ProjectileUtils.isBow(heldItem)) {
          Angle oldViewAngles = LocalPlayerUtils.getViewAngles();
          // send new angles
          LocalPlayerUtils.sendRotatePacket(
              ProjectileUtils.getBestPitch(heldItem, trace.hitVec), oldViewAngles.getYaw());
          // tell server we let go of bow
          Packet usePacket =
              new CPacketPlayerDigging(
                  CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
          // add to ignore list
          PacketHelper.isIgnored(usePacket);
          getNetworkManager().sendPacket(usePacket);
          // revert back to old angles
          LocalPlayerUtils.sendRotatePacket(oldViewAngles);
          event.setCanceled(true);
        }
      } else if (event.getPacket() instanceof CPacketPlayerTryUseItem
          && ((CPacketPlayerTryUseItem) event.getPacket()).getHand().equals(EnumHand.MAIN_HAND)
          && !PacketHelper.isIgnored(event.getPacket())) {
        ItemStack heldItem = localPlayer.getHeldItemMainhand();
        RayTraceResult trace = localPlayer.rayTrace(9999.D, 0.f);
        if (heldItem != null
            && trace != null
            && ProjectileUtils.isThrowable(heldItem)
            && !ProjectileUtils.isBow(heldItem)) {
          // send server our new view angles
          LocalPlayerUtils.sendRotatePacket(
              ProjectileUtils.getBestPitch(heldItem, trace.hitVec),
              LocalPlayerUtils.getViewAngles().getYaw());
          // tell server we let go of bow
          Packet usePacket =
              new CPacketPlayerTryUseItem(((CPacketPlayerTryUseItem) event.getPacket()).getHand());
          // add to ignore list
          PacketHelper.ignore(usePacket);
          getNetworkManager().sendPacket(usePacket);
          // revert back to the old view angles
          LocalPlayerUtils.sendRotatePacket(LocalPlayerUtils.getViewAngles());
          // cancel this event (wont send the packet)
          event.setCanceled(true);
        }
      }
    }
  }
}
