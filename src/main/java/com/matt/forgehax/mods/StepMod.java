package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class StepMod extends ToggleMod {
    public final static float DEFAULT_STEP_HEIGHT = 0.6f;

    public StepMod() {
        super("Step", false, "Step up blocks");
    }

    @Override
    public void onDisabled() {
        if(WRAPPER.getLocalPlayer() != null) {
            WRAPPER.getLocalPlayer().stepHeight = DEFAULT_STEP_HEIGHT;
        }
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = (EntityPlayer)event.getEntityLiving();
        if(localPlayer.onGround) {
            localPlayer.stepHeight = 1.f;
        } else {
            localPlayer.stepHeight = DEFAULT_STEP_HEIGHT;
        }
    }

    private CPacketPlayer previousPositionPacket = null;

    @SubscribeEvent
    public void onPacketSending(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer.Position ||
                event.getPacket() instanceof CPacketPlayer.PositionRotation) {
            CPacketPlayer packetPlayer = (CPacketPlayer)event.getPacket();
            if(previousPositionPacket != null &&
                    !Utils.OUTGOING_PACKET_IGNORE_LIST.contains(event.getPacket())) {
                double diffY = packetPlayer.getY(0.f) - previousPositionPacket.getY(0.f);
                // y difference must be positive
                // greater than 1, but less than 1.5
                if(diffY > DEFAULT_STEP_HEIGHT &&
                        diffY <= 1.2491870787) {
                    List<Packet> sendList = Lists.newArrayList();
                    // if this is true, this must be a step
                    // now to send additional packets to get around NCP
                    double x = previousPositionPacket.getX(0.D);
                    double y = previousPositionPacket.getY(0.D);
                    double z = previousPositionPacket.getZ(0.D);
                    sendList.add(new CPacketPlayer.Position(
                            x,
                            y + 0.4199999869D,
                            z,
                            true
                    ));
                    sendList.add(new CPacketPlayer.Position(
                            x,
                            y + 0.7531999805D,
                            z,
                            true
                    ));
                    sendList.add(new CPacketPlayer.Position(
                            packetPlayer.getX(0.f),
                            packetPlayer.getY(0.f),
                            packetPlayer.getZ(0.f),
                            packetPlayer.isOnGround()
                    ));
                    for(Packet toSend : sendList) {
                        Utils.OUTGOING_PACKET_IGNORE_LIST.add(toSend);
                        WRAPPER.getNetworkManager().sendPacket(toSend);
                    }
                    event.setCanceled(true);
                }
            }
            previousPositionPacket = (CPacketPlayer)event.getPacket();
        }
    }
}
