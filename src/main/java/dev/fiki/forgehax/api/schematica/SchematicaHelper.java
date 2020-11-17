package dev.fiki.forgehax.api.schematica;


import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.proxy.ClientProxy;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public enum SchematicaHelper {
  ;
  
  public static boolean isSchematicaPresent() {
    try {
      Class.forName(Schematica.class.getName());
      return true;
    } catch (ClassNotFoundException | NoClassDefFoundError ex) {
      return false;
    }
  }
  
  public static Optional<Tuple<Schematic, BlockPos>> getOpenSchematic() {
    return Optional.ofNullable(ClientProxy.schematic)
        .map(world -> new Tuple<>(new SchematicAdapter(world), world.position));
  }
  
}
