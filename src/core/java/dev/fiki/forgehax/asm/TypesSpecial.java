package dev.fiki.forgehax.asm;

import dev.fiki.forgehax.common.asmtype.ASMClass;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

/**
 * Created on 5/29/2017 by fr1kin
 */
public interface TypesSpecial {
  
  interface Classes {
    
    ASMClass SchematicPrinter =
      ASMClass.builder()
        .className("com/github/lunatrius/schematica/client/printer/SchematicPrinter")
        .build();
  }
  
  interface Fields {
  
  }
  
  interface Methods {
    ASMMethod SchematicPrinter_placeBlock =
      Classes.SchematicPrinter.newChildMethod()
        .name("placeBlock")
        .returns(boolean.class)
        .argument(TypesMc.Classes.ClientWorld)
        .argument(TypesMc.Classes.ClientPlayerEntity)
        .argument(TypesMc.Classes.ItemStack)
        .argument(TypesMc.Classes.BlockPos)
        .argument(TypesMc.Classes.Direction)
        .argument(TypesMc.Classes.Vec3d)
        .argument(TypesMc.Classes.Hand)
        .build();
  }
}
