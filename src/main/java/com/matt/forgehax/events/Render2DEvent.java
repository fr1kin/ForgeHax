package com.matt.forgehax.events;

import com.matt.forgehax.util.draw.SurfaceHelper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.Event;

import static com.matt.forgehax.Globals.MC;

/**
 * Created on 9/2/2017 by fr1kin
 */
public class Render2DEvent extends Event {
    private final ScaledResolution resolution = new ScaledResolution(MC);
    private final SurfaceHelper surfaceBuilder = new SurfaceHelper();
    private final float partialTicks;

    public Render2DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public double getScreenWidth() {
        return resolution.getScaledWidth_double();
    }

    public double getScreenHeight() {
        return resolution.getScaledHeight_double();
    }

    public SurfaceHelper getSurfaceBuilder() {
        return surfaceBuilder;
    }
}
