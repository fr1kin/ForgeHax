package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.ApplyCollisionMotionEvent;
import com.matt.forgehax.asm.events.WaterMovementEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.events.WebMotionEvent;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiKnockbackMod extends ToggleMod {
    private Property modifierX;
    private Property modifierY;
    private Property modifierZ;

    public AntiKnockbackMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                modifierX = configuration.get(getModName(),
                        "modifierX",
                        0.D,
                        "X motion modifier"
                ),
                modifierY = configuration.get(getModName(),
                        "modifierY",
                        0.D,
                        "Y motion modifier"
                ),
                modifierZ = configuration.get(getModName(),
                        "modifierZ",
                        0.D,
                        "Z motion modifier"
                )
        );
    }

    /**
     * Stops TNT and knockback velocity
     */
    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.ReceivedEvent.Pre event) {
        if(event.getPacket() instanceof SPacketExplosion) {
            ((SPacketExplosion) event.getPacket()).motionX *= modifierX.getDouble();
            ((SPacketExplosion) event.getPacket()).motionY *= modifierY.getDouble();
            ((SPacketExplosion) event.getPacket()).motionZ *= modifierZ.getDouble();
        }
        if(event.getPacket() instanceof SPacketEntityVelocity) {
            if(((SPacketEntityVelocity) event.getPacket()).getEntityID() == MC.thePlayer.getEntityId()) {
                ((SPacketEntityVelocity) event.getPacket()).motionX *= modifierX.getDouble();
                ((SPacketEntityVelocity) event.getPacket()).motionY *= modifierY.getDouble();
                ((SPacketEntityVelocity) event.getPacket()).motionZ *= modifierZ.getDouble();
            }
        }
    }

    /**
     * Stops velocity from water
     */
    @SubscribeEvent
    public void onWaterMovementEvent(WaterMovementEvent event) {
        if(event.getEntity().equals(MC.thePlayer)) {
            Vec3d moveDir = event.getMoveDir().normalize();
            event.getEntity().motionX += (moveDir.xCoord * 0.014D) * modifierX.getDouble();
            event.getEntity().motionY += (moveDir.yCoord * 0.014D) * modifierY.getDouble();
            event.getEntity().motionZ += (moveDir.zCoord * 0.014D) * modifierZ.getDouble();
            event.setCanceled(true);
        }
    }

    /**
     * Stops velocity from collision
     */
    @SubscribeEvent
    public void onApplyCollisionMotion(ApplyCollisionMotionEvent event) {
        if(event.getEntity().equals(MC.thePlayer)) {
            event.getEntity().addVelocity(
                    event.getMotionX() * modifierX.getDouble(),
                    event.getMotionY() * modifierY.getDouble(),
                    event.getMotionZ() * modifierZ.getDouble()
            );
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWebMotion(WebMotionEvent event) {
        if(event.getEntity().equals(MC.thePlayer)) {
            double modifier = 1;
            event.setX(event.getX() * (0.25D * modifier));
            event.setY(event.getY() * (0.05000000074505806D * modifier));
            event.setZ(event.getZ() * (0.25D * modifier));
            event.setCanceled(true);
        }
    }
}
