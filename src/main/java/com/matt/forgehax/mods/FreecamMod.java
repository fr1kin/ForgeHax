package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static com.matt.forgehax.Wrapper.*;

/**
 * Created on 9/3/2016 by fr1kin
 */

@RegisterMod
public class FreecamMod extends ToggleMod {
    public Property speed;

    private double posX, posY, posZ;
    private float pitch, yaw;

    private EntityOtherPlayerMP clonedPlayer;

    public FreecamMod() {
        super("Freecam", false, "Freecam mode");
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

            clonedPlayer = new EntityOtherPlayerMP(MC.world, MC.getSession().getProfile());
            clonedPlayer.clonePlayer(localPlayer, false);
            clonedPlayer.copyLocationAndAnglesFrom(localPlayer);
            clonedPlayer.rotationYawHead = localPlayer.rotationYawHead;
            MC.world.addEntityToWorld(-100, clonedPlayer);
            MC.player.capabilities.isFlying = true;
            MC.player.capabilities.setFlySpeed((float)speed.getDouble());
            MC.player.noClip = true;
        }
    }

    @Override
    public void onDisabled() {
        EntityPlayer localPlayer = getLocalPlayer();
        if(localPlayer != null) {
            MC.player.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
            MC.world.removeEntityFromWorld(-100);
            clonedPlayer = null;
            posX = posY = posZ = 0.D;
            pitch = yaw = 0.f;
            MC.player.capabilities.isFlying = getModManager().getMod("ElytraFlight").getProperty("enabled").getBoolean();
            MC.player.capabilities.setFlySpeed(0.05f);
            MC.player.noClip = false;
            MC.player.motionX = MC.player.motionY = MC.player.motionZ = 0.f;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        MC.player.capabilities.isFlying = true;
        MC.player.capabilities.setFlySpeed((float)speed.getDouble());
        MC.player.noClip = true;
        MC.player.onGround = false;
        MC.player.fallDistance = 0;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer) {
            event.setCanceled(true);
        }
    }
}
