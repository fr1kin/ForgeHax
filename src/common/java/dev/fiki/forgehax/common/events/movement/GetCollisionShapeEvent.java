package dev.fiki.forgehax.common.events.movement;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created on 4/9/2017 by fr1kin
 */
@Getter
@AllArgsConstructor
@Cancelable
public class GetCollisionShapeEvent extends Event {
  private final Block block;
  private final BlockState state;
  private final IBlockReader reader;
  private final BlockPos pos;
}
