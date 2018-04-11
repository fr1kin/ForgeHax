package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getModManager;

/**
 * Created on 9/3/2016 by fr1kin
 */

@RegisterMod
public class FreecamMod extends ToggleMod {
    public final Setting<Double> speed = getCommandStub().builders().<Double>newSettingBuilder()
            .name("speed")
            .description("Movement speed")
            .defaultTo(0.05D)
            .build();

    private double posX, posY, posZ;
    private float pitch, yaw;

    private double startPosX, startPosY, startPosZ;
    private float startPitch, startYaw;


    private EntityOtherPlayerMP clonedPlayer;

    public FreecamMod() {
        super(Category.PLAYER, "Freecam", false, "Freecam mode");
    }

    private boolean isRidingEntity;
    private Entity ridingEntity;

    @Override
    public void onEnabled() {

        if(MC.player != null) {
            isRidingEntity = MC.player.getRidingEntity() != null;

            if (MC.player.getRidingEntity() == null) {
                posX = MC.player.posX;
                posY = MC.player.posY;
                posZ = MC.player.posZ;
            }
            else {
                ridingEntity = MC.player.getRidingEntity();
                MC.player.dismountRidingEntity();
            }

            pitch = MC.player.rotationPitch;
            yaw = MC.player.rotationYaw;

            clonedPlayer = new EntityOtherPlayerMP(MC.world, MC.getSession().getProfile());
            clonedPlayer.copyLocationAndAnglesFrom(MC.player);
            clonedPlayer.rotationYawHead = MC.player.rotationYawHead;
            MC.world.addEntityToWorld(-100, clonedPlayer);
            MC.player.capabilities.isFlying = true;
            MC.player.capabilities.setFlySpeed(speed.getAsFloat());
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
            getLocalPlayer().capabilities.isFlying = getModManager().get(ElytraFlight.class)
                    .map(BaseMod::isEnabled)
                    .orElse(false);
            MC.player.capabilities.setFlySpeed(0.05f);
            MC.player.noClip = false;
            MC.player.motionX = MC.player.motionY = MC.player.motionZ = 0.f;

            if (isRidingEntity) {
                MC.player.startRiding(ridingEntity, true);
            }
        }
    }
    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        MC.player.capabilities.isFlying = true;
        MC.player.capabilities.setFlySpeed(speed.getAsFloat());
        MC.player.noClip = true;
        MC.player.onGround = false;
        MC.player.fallDistance = 0;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketInput) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceived (PacketEvent.Incoming.Pre event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
            startPosX = packet.getX();
            startPosY = packet.getY();
            startPosZ = packet.getZ();
            startPitch = packet.getPitch();
            startYaw = packet.getYaw();
        }
    }

    @SubscribeEvent
    public void onWorldLoad (WorldEvent.Load event) {
        posX = startPosX;
        posY = startPosY;
        posZ = startPosZ;
        pitch = startPitch;
        yaw = startYaw;
    }

}