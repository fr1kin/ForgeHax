package com.matt.forgehax.asm.events;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Babbaj on 9/20/2017.
 */
public class SchematicaPlaceBlockEvent extends Event {
  
  private ItemStack item;
  private BlockPos pos;
  private Vec3d vec;
  private EnumFacing side;
  
  public SchematicaPlaceBlockEvent(ItemStack itemIn, BlockPos posIn, Vec3d vecIn, EnumFacing sideIn) {
    this.item = itemIn;
    this.pos = posIn;
    this.vec = vecIn;
    this.side = sideIn;
  }
  
  public ItemStack getItem() {
    return this.item;
  }
  
  public BlockPos getPos() {
    return this.pos;
  }
  
  public Vec3d getVec() {
    return this.vec;
  }

  public EnumFacing getSide() {
    return this.side;
  }
}
