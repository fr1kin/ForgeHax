package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ConcurrentLinkedDeque;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.printMessage;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Coded by LoganDark on 8/22/2019
 */
@RegisterMod
public class BlinkMod extends ToggleMod {
  private static final int GHOST_ID = -101;

  public BlinkMod() {
    super(Category.PLAYER, "Blink", false, "Simulate lag to teleport short distances");
  }

  private ConcurrentLinkedDeque<Packet<?>> packets;
  private EntityOtherPlayerMP ghost;
  private boolean disablePlease = false;

  private boolean setGhostPlayer() {
    EntityPlayer player = getLocalPlayer();
    WorldClient world = getWorld();
    if (isNull(player) || isNull(world)) return false;

    ghost = new EntityOtherPlayerMP(world, MC.getSession().getProfile());
    ghost.copyLocationAndAnglesFrom(player);
    ghost.rotationYawHead = player.rotationYawHead;
    ghost.inventory = player.inventory;
    ghost.inventoryContainer = player.inventoryContainer;
    world.addEntityToWorld(GHOST_ID, ghost);

    return true;
  }

  @Override
  protected void onEnabled() {
    super.onEnabled();

    if (MC.isSingleplayer()) {
      printMessage("Sorry, Blink doesn't work in singleplayer at the moment...");
      disablePlease = true;
      return;
    }

    packets = new ConcurrentLinkedDeque<>();
    if (!setGhostPlayer()) disablePlease = true;
  }

  private void removeGhostPlayer() {
    ghost.world.removeEntity(ghost);
  }

  @Override
  protected void onDisabled() {
    if (nonNull(ghost)) {
      removeGhostPlayer();
      ghost = null;
    }

    if (nonNull(packets)) {
      packets.forEach(PacketHelper::ignoreAndSend);
      packets = null;
    }

    super.onDisabled();
  }

  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    if (nonNull(packets)) {
      event.setCanceled(true);
      packets.add(event.getPacket());
    }
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (disablePlease) {
      disablePlease = false;
      disable();
    }
  }
}
