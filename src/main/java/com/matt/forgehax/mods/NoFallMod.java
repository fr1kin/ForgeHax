package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.Utils;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFallMod extends ToggleMod {
    public NoFallMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    private float lastFallDistance = 0;

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Rotation) &&
                !Utils.OUTGOING_PACKET_IGNORE_LIST.contains(event.getPacket())) {
            CPacketPlayer packetPlayer = (CPacketPlayer)event.getPacket();
            if(packetPlayer.onGround && lastFallDistance >= 4) {
                CPacketPlayer packet = new CPacketPlayer.PositionRotation(
                        ((CPacketPlayer) event.getPacket()).getX(0),
                        1337 + ((CPacketPlayer) event.getPacket()).getY(0),
                        ((CPacketPlayer) event.getPacket()).getZ(0),
                        ((CPacketPlayer) event.getPacket()).getYaw(0),
                        ((CPacketPlayer) event.getPacket()).getPitch(0),
                        true
                );
                CPacketPlayer reposition = new CPacketPlayer.PositionRotation(
                        ((CPacketPlayer) event.getPacket()).getX(0),
                        ((CPacketPlayer) event.getPacket()).getY(0),
                        ((CPacketPlayer) event.getPacket()).getZ(0),
                        ((CPacketPlayer) event.getPacket()).getYaw(0),
                        ((CPacketPlayer) event.getPacket()).getPitch(0),
                        true
                );
                Utils.OUTGOING_PACKET_IGNORE_LIST.add(packet);
                Utils.OUTGOING_PACKET_IGNORE_LIST.add(reposition);
                WRAPPER.getNetworkManager().sendPacket(packet);
                WRAPPER.getNetworkManager().sendPacket(reposition);
                lastFallDistance = 0;
            } else {
                lastFallDistance = WRAPPER.getLocalPlayer().fallDistance;
            }
        }
    }
}
