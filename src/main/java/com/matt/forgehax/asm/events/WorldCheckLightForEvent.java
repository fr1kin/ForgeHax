package com.matt.forgehax.asm.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Created on 2/10/2018 by fr1kin */
@Cancelable
public class WorldCheckLightForEvent extends Event {
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
