package com.matt.forgehax.util;

import com.matt.forgehax.ForgeHaxBase;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils extends ForgeHaxBase {
    public static EntityPlayerSP me() {
        return MC.thePlayer;
    }

    public static boolean isMcValid() {
        return me() != null && MC.theWorld != null;
    }

    public static void setViewAngles(Angle angles) {
        setViewAngles(angles.p, angles.y);
    }
    public static void setViewAngles(double p, double y) {
        if(isMcValid()) {
            me().rotationYaw = (float)y;
            me().rotationPitch = (float)p;
        }
    }

    public static Angle getViewAngles() {
        return new Angle(me().rotationPitch, me().rotationYaw);
    }

    public static void correctMovement(Angle viewAngles) {
        if(isMcValid()) {
            Angle mcView = new Angle(MathHelper.wrapDegrees(me().rotationPitch), MathHelper.wrapDegrees(me().rotationYaw));
            Vec3d move = new Vec3d(me().moveForward, 0, me().moveStrafing);
            Angle angle = VectorUtils.vectorToAngle(move.normalize()).add(viewAngles.sub(mcView));
            Vec3d forward = angle.forward().scale(move.lengthVector());
            me().moveForward = (float)forward.xCoord;
            me().moveStrafing = (float)forward.zCoord;
        }
    }
}
