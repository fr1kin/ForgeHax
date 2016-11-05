package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 9/3/2016 by fr1kin
 */
public class FreecamMod extends ToggleMod {
    private double posX, posY, posZ;
    private float pitch, yaw;

    private EntityOtherPlayerMP clonedPlayer;

    public FreecamMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
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
            try {
                Property fly = SETTINGS.get("fly-enabled");
                Property noclip = SETTINGS.get("noclip-enabled");
                fly.set(true);
                noclip.set(true);
                MOD.mods.get("fly").update();
                MOD.mods.get("noclip").update();
            } catch (Exception e) {
                MOD.printStackTrace(e);
            }
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
            try {
                Property fly = SETTINGS.get("fly-enabled");
                Property noclip = SETTINGS.get("noclip-enabled");
                fly.set(false);
                noclip.set(false);
                MOD.mods.get("fly").update();
                MOD.mods.get("noclip").update();
            } catch (Exception e) {
                MOD.printStackTrace(e);
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer) {
            event.setCanceled(true);
        }
    }
}
