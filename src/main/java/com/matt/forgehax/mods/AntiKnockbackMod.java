package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.ApplyCollisionMotionEvent;
import com.matt.forgehax.asm.events.WaterMovementEvent;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.events.WebMotionEvent;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiKnockbackMod extends ToggleMod {
    private Property multiplierX;
    private Property multiplierY;
    private Property multiplierZ;

    public AntiKnockbackMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                multiplierX = configuration.get(getModName(),
                        "multiplierX",
                        0.D,
                        "X motion multiplier"
                ),
                multiplierY = configuration.get(getModName(),
                        "multiplierY",
                        0.D,
                        "Y motion multiplier"
                ),
                multiplierZ = configuration.get(getModName(),
                        "multiplierZ",
                        0.D,
                        "Z motion multiplier"
                )
        );
    }

    /**
     * Stops TNT and knockback velocity
     */
    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.Received.Pre event) {
        if(event.getPacket() instanceof SPacketExplosion) {
            // for tnt knockback
            ((SPacketExplosion) event.getPacket()).motionX *= multiplierX.getDouble();
            ((SPacketExplosion) event.getPacket()).motionY *= multiplierY.getDouble();
            ((SPacketExplosion) event.getPacket()).motionZ *= multiplierZ.getDouble();
        }
        if(event.getPacket() instanceof SPacketEntityVelocity) {
            // for player knockback
            if(((SPacketEntityVelocity) event.getPacket()).getEntityID() == MC.thePlayer.getEntityId()) {
                double multiX = multiplierX.getDouble();
                double multiY = multiplierY.getDouble();
                double multiZ = multiplierZ.getDouble();
                if(multiX == 0 && multiY == 0 && multiZ == 0) {
                    event.setCanceled(true);
                } else {
                    ((SPacketEntityVelocity) event.getPacket()).motionX *= multiX;
                    ((SPacketEntityVelocity) event.getPacket()).motionY *= multiY;
                    ((SPacketEntityVelocity) event.getPacket()).motionZ *= multiZ;
                }
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
            event.getEntity().motionX += (moveDir.xCoord * 0.014D) * multiplierX.getDouble();
            event.getEntity().motionY += (moveDir.yCoord * 0.014D) * multiplierY.getDouble();
            event.getEntity().motionZ += (moveDir.zCoord * 0.014D) * multiplierZ.getDouble();
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
                    event.getMotionX() * multiplierX.getDouble(),
                    event.getMotionY() * multiplierY.getDouble(),
                    event.getMotionZ() * multiplierZ.getDouble()
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
