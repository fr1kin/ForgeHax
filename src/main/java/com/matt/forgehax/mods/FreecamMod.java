package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 9/3/2016 by fr1kin
 */
public class FreecamMod extends ToggleMod {
    public Property speed;

    private double posX, posY, posZ;
    private float pitch, yaw;

    private EntityOtherPlayerMP clonedPlayer;

    public FreecamMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                speed = configuration.get(getModName(),
                        "speed",
                        0.05,
                        "Freecam speed"
                )
        );
    }

    @Override
    public void onEnabled() {
        EntityPlayer localPlayer = getLocalPlayer();
        if(localPlayer != null) {
            posX = localPlayer.posX;
            posY = localPlayer.posY;
            posZ = localPlayer.posZ;
            pitch = localPlayer.rotationPitch;
            yaw = localPlayer.rotationYaw;

            clonedPlayer = new EntityOtherPlayerMP(MC.theWorld, MC.getSession().getProfile());
            clonedPlayer.clonePlayer(localPlayer, false);
            clonedPlayer.copyLocationAndAnglesFrom(localPlayer);
            clonedPlayer.rotationYawHead = localPlayer.rotationYawHead;
            MC.theWorld.addEntityToWorld(-100, clonedPlayer);
            MC.thePlayer.capabilities.isFlying = true;
            MC.thePlayer.capabilities.setFlySpeed((float)speed.getDouble());
            MC.thePlayer.noClip = true;
        }
    }

    @Override
    public void onDisabled() {
        EntityPlayer localPlayer = getLocalPlayer();
        if(localPlayer != null) {
            MC.thePlayer.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
            MC.theWorld.removeEntityFromWorld(-100);
            clonedPlayer = null;
            posX = posY = posZ = 0.D;
            pitch = yaw = 0.f;
            MC.thePlayer.capabilities.isFlying = false;
            MC.thePlayer.capabilities.setFlySpeed(0.05f);
            MC.thePlayer.noClip = false;
            MC.thePlayer.motionX = MC.thePlayer.motionY = MC.thePlayer.motionZ = 0.f;
        }
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        MC.thePlayer.capabilities.isFlying = true;
        MC.thePlayer.capabilities.setFlySpeed((float)speed.getDouble());
        MC.thePlayer.noClip = true;
        MC.thePlayer.onGround = false;
        MC.thePlayer.fallDistance = 0;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer) {
            event.setCanceled(true);
        }
    }
}
