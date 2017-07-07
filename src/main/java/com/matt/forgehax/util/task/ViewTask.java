package com.matt.forgehax.util.task;

import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.mod.BaseMod;

/**
 * Created on 6/15/2017 by fr1kin
 */
public abstract class ViewTask extends Task {
    private float originalPitch = 0.f;
    private float originalYaw = 0.f;

    private float pitch = 0.f;
    private float yaw = 0.f;

    private boolean resetToOriginal = false;

    protected ViewTask(BaseMod parent, PriorityEnum priority) {
        super(parent, priority);
    }

    public void setOriginalViewAngles(float pitch, float yaw) {
        this.originalPitch = pitch;
        this.originalYaw = yaw;
    }

    public float getOriginalPitch() {
        return originalPitch;
    }

    public float getOriginalYaw() {
        return originalYaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setViewAngle(float pitch, float yaw) {
        setPitch(pitch);
        setYaw(yaw);
    }

    public void setReset(boolean b) {
        this.resetToOriginal = b;
    }

    public boolean shouldReset() {
        return resetToOriginal;
    }

    @Override
    public void onPreUpdate() {

    }

    @Override
    public void finished() {
        resetToOriginal = false;
        originalPitch = originalYaw = pitch = yaw = 0.f;
    }
}
