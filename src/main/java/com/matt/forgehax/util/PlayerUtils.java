package com.matt.forgehax.util;

import com.matt.forgehax.ForgeHaxBase;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class PlayerUtils extends ForgeHaxBase {
    private static Entity targetEntity = null;

    public static EntityPlayerSP me() {
        return MC.thePlayer;
    }

    public static void setViewAngles(Angle angles) {
        setViewAngles(angles.p, angles.y);
    }
    public static void setViewAngles(double p, double y) {
        me().rotationYaw = (float)y;
        me().rotationPitch = (float)p;
    }

    public static Angle getViewAngles() {
        return new Angle(me().rotationPitch, me().rotationYaw);
    }

    /*
    public static void correctMovement(Angle viewAngles) {
        Angle mcView = new Angle(MathHelper.wrapDegrees(me().rotationPitch), MathHelper.wrapDegrees(me().rotationYaw));
        Vec3d move = new Vec3d(me().moveForward, 0, me().moveStrafing);
        Angle angle = VectorUtils.vectorToAngle(move.normalize()).add(viewAngles.sub(mcView));
        Vec3d forward = angle.forward().scale(move.lengthVector());
        me().moveForward = (float)forward.xCoord;
        me().moveStrafing = (float)forward.zCoord;
    }
    */

    public static Entity getTargetEntity() {
        return targetEntity;
    }

    public static void setTargetEntity(@Nullable Entity targetEntity) {
        PlayerUtils.targetEntity = targetEntity;
    }

    public static boolean isTargetEntity(Entity entity) {
        return targetEntity != null && targetEntity.equals(entity);
    }
}
