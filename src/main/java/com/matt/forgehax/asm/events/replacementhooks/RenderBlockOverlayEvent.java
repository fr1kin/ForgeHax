package com.matt.forgehax.asm.events.replacementhooks;

import static com.matt.forgehax.Globals.MC;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class RenderBlockOverlayEvent extends Event implements Cancelable {

    public enum OverlayType {
        FIRE, BLOCK, WATER
    }

    private final float renderPartialTicks;
    private final OverlayType overlayType;
    private final IBlockState blockForOverlay;


    public RenderBlockOverlayEvent(OverlayType type, float renderPartialTicks, IBlockState block)
    {
        this.renderPartialTicks = renderPartialTicks;
        this.overlayType = type;
        this.blockForOverlay = block;
    }


    public float getRenderPartialTicks() { return renderPartialTicks; }
    /**
     * The type of overlay to occur
     */
    public OverlayType getOverlayType() { return overlayType; }
    /**
     * If the overlay type is BLOCK, then this is the block which the overlay is getting it's icon from
     */
    public IBlockState getBlockForOverlay() { return blockForOverlay; }
}