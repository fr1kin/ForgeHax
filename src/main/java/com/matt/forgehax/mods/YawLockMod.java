package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class YawLockMod extends ToggleMod {
    public static YawLockMod INSTANCE;

    public static YawLockMod instance() {
        return INSTANCE;
    }

    public Property doOnce;
    public Property autoAngle;
    public Property customAngle;

    public YawLockMod() {
        super("YawLock", false, "Locks yaw to prevent moving into walls");
        INSTANCE = this;
    }

    public double getYawDirection() {
        return Math.round((LocalPlayerUtils.getViewAngles().getYaw() + 1.f) / 45.f) * 45.f;
    }

    @Override
    public void loadConfig(Configuration configuration) {
        super.loadConfig(configuration);
        addSettings(
                doOnce = configuration.get(getModName(), "once", false, "Will only fire update once"),
                autoAngle = configuration.get(getModName(), "auto", true, "Automatically finds angle to snap to based on the direction you're facing"),
                customAngle = configuration.get(getModName(), "angle", 0.0, "Custom angle to snap to", -180.D, 180.D)
        );
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        double yaw = getYawDirection();
        if(!autoAngle.getBoolean())
            yaw = customAngle.getDouble();
        LocalPlayerUtils.setViewAngles(event.getEntityLiving().rotationPitch, yaw);
        // disable after first set if set to do once
        if(isEnabled() && doOnce.getBoolean())
            toggle();
    }
}
