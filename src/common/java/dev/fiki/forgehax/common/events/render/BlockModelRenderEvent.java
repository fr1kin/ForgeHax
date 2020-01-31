package dev.fiki.forgehax.common.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created on 5/5/2017 by fr1kin
 */
@Getter
@AllArgsConstructor
public class BlockModelRenderEvent extends Event {
  private final IBlockReader blockAccess;
  private final IBakedModel bakedModel;
  private final BlockState blockState;
  private final BlockPos blockPos;
  private final BufferBuilder buffer;
  private final boolean checkSides;
  private final long rand;
}
