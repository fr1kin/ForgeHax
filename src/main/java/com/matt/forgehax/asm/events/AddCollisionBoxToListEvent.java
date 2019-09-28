package com.matt.forgehax.asm.events;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 4/9/2017 by fr1kin
 */
@Cancelable
public class AddCollisionBoxToListEvent extends Event {
  
  private final Block block;
  private final IBlockState state;
  private final World world;
  private final BlockPos pos;
  private final AxisAlignedBB entityBox;
  private final List<AxisAlignedBB> collidingBoxes;
  private final Entity entity;
  private final boolean bool;
  
  public AddCollisionBoxToListEvent(
    Block block,
    IBlockState state,
    World worldIn,
    BlockPos pos,
    AxisAlignedBB entityBox,
    List<AxisAlignedBB> collidingBoxes,
    Entity entityIn,
    boolean bool) {
    this.block = block;
    this.state = state;
    this.world = worldIn;
    this.pos = pos;
    this.entityBox = entityBox;
    this.collidingBoxes = collidingBoxes;
    this.entity = entityIn;
    this.bool = bool;
  }
  
  public Block getBlock() {
    return block;
  }
  
  public IBlockState getState() {
    return state;
  }
  
  public World getWorld() {
    return world;
  }
  
  public BlockPos getPos() {
    return pos;
  }
  
  public AxisAlignedBB getEntityBox() {
    return entityBox;
  }
  
  public List<AxisAlignedBB> getCollidingBoxes() {
    return collidingBoxes;
  }
  
  public Entity getEntity() {
    return entity;
  }
  
  public boolean isBool() {
    return bool;
  }
}
