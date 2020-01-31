package dev.fiki.forgehax.common.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created on 11/10/2016 by fr1kin
 */

@Getter
@AllArgsConstructor
public class BlockRenderEvent extends Event {
  private final BlockPos pos;
  private final BlockState state;
  private final IBlockReader access;
  private final BufferBuilder buffer;
}
