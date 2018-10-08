package com.matt.forgehax.asm.events;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

/**
 * Created on 2/10/2018 by fr1kin
 */
public class WorldCheckLightForEvent extends Event implements Cancelable {
    private final EnumSkyBlock enumSkyBlock;
    private final BlockPos pos;

    public WorldCheckLightForEvent(EnumSkyBlock enumSkyBlock, BlockPos pos) {
        this.enumSkyBlock = enumSkyBlock;
        this.pos = pos;
    }

    public EnumSkyBlock getEnumSkyBlock() {
        return enumSkyBlock;
    }

    public BlockPos getPos() {
        return pos;
    }
}
