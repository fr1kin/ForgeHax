package dev.fiki.forgehax.api.schematica;

import com.github.lunatrius.schematica.client.world.SchematicWorld;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;


public final class SchematicAdapter implements Schematic {
  
  private final SchematicWorld schematic;
  
  public SchematicAdapter(SchematicWorld schematicWorld) {
    this.schematic = schematicWorld;
  }
  
  
  @Override
  public BlockState desiredState(BlockPos pos) {
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
