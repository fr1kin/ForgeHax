package com.matt.forgehax.util.schematica;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;


public final class SchematicAdapter implements Schematic {
  
  private final SchematicWorld schematic;
  
  public SchematicAdapter(SchematicWorld schematicWorld) {
    this.schematic = schematicWorld;
  }
  
  
  @Override
  public IBlockState desiredState(BlockPos pos) {
    return schematic.getSchematic().getBlockState(pos);
  }
  
  @Override
  public int widthX() {
    return schematic.getSchematic().getWidth();
  }
  
  @Override
  public int heightY() {
    return schematic.getSchematic().getHeight();
  }
  
  @Override
  public int lengthZ() {
    return schematic.getSchematic().getLength();
  }
}
